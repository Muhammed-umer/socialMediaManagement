package com.monitor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class AIService {

    @Value("${huggingface.apiKey:}")
    private String hfApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    // Switching to v0.2 as it's often more consistently available on the free Inference API tier
    private static final String HF_API_URL = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2";

    public Map<String, String> analyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return fallbackMock(text);
        }

        // Extremely specific prompt to force the model to behave with high reliability
        String prompt = "Task: Analyze the post for election monitoring in Erode, India.\n" +
                "Post: " + text + "\n\n" +
                "Format your response EXACTLY as follows:\n" +
                "Summary: [1-sentence summary of the election/political context]\n" +
                "Sentiment: [Positive/Negative/Neutral]\n" +
                "Constituency: [The Erode constituency mentioned or 'General Erode']\n" +
                "Alert Level: [Low/Medium/High]\n" +
                "Relevance: [Score from 1 to 10]\n\n" +
                "Analysis:";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (hfApiKey != null && !hfApiKey.trim().isEmpty()) {
                headers.set("Authorization", "Bearer " + hfApiKey);
            }

            Map<String, Object> body = new HashMap<>();
            body.put("inputs", prompt);
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("max_new_tokens", 250);
            parameters.put("return_full_text", false);
            parameters.put("temperature", 0.3);
            body.put("parameters", parameters);

            // CRITICAL: wait_for_model ensures we don't get 503 while model is loading
            Map<String, Object> options = new HashMap<>();
            options.put("wait_for_model", true);
            body.put("options", options);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            System.out.println(">>> Sending request to Hugging Face (v0.2)...");
            ResponseEntity<List> response = restTemplate.postForEntity(HF_API_URL, entity, List.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && !response.getBody().isEmpty()) {
                Map<String, Object> result = (Map<String, Object>) response.getBody().get(0);
                String generatedText = (String) result.get("generated_text");
                System.out.println(">>> HF Response received successfully.");
                return parseGeneratedText(generatedText, text);
            } else {
                Map<String, String> errMap = fallbackMock(text);
                errMap.put("aiSummary", "HF API Issue: " + response.getStatusCode());
                return errMap;
            }

        } catch (Exception e) {
            String detailedError = e.getMessage();
            if (e instanceof org.springframework.web.client.HttpStatusCodeException) {
                detailedError = ((org.springframework.web.client.HttpStatusCodeException)e).getResponseBodyAsString();
            }
            System.err.println(">>> HF API Error: " + detailedError);
            
            Map<String, String> errMap = fallbackMock(text);
            // Put the actual error in the summary so the user can see if it's a key issue or something else
            if (detailedError.contains("Authorization")) {
                errMap.put("aiSummary", "AI Error: Invalid Hugging Face API Key. Please check application.yml.");
            } else if (detailedError.contains("loading")) {
                errMap.put("aiSummary", "AI Model is currently loading. Please wait 30 seconds and search again.");
            } else {
                errMap.put("aiSummary", "AI unavailable (Technical Error). Review required for: " + (text.length() > 30 ? text.substring(0, 30) + "..." : text));
            }
            return errMap;
        }
    }

    private Map<String, String> parseGeneratedText(String generatedText, String originalText) {
        Map<String, String> analysis = new HashMap<>();
        String[] lines = generatedText.split("\n");
        for (String line : lines) {
            if (line.startsWith("Summary:")) {
                analysis.put("aiSummary", line.substring(8).trim());
            } else if (line.startsWith("Sentiment:")) {
                analysis.put("sentiment", line.substring(10).trim());
            } else if (line.startsWith("Constituency:")) {
                analysis.put("constituency", line.substring(13).trim());
            } else if (line.startsWith("Alert Level:")) {
                analysis.put("alertLevel", line.substring(12).trim());
            } else if (line.startsWith("Relevance:")) {
                String relStr = line.substring(10).trim().split("/")[0];
                analysis.put("relevance", relStr);
            }
        }
        
        // Ensure all required fields exist
        if(!analysis.containsKey("aiSummary")) analysis.put("aiSummary", "Unable to extract summary.");
        if(!analysis.containsKey("sentiment")) analysis.put("sentiment", "Neutral");
        if(!analysis.containsKey("constituency")) analysis.put("constituency", "General Erode");
        if(!analysis.containsKey("alertLevel")) analysis.put("alertLevel", "Low");
        if(!analysis.containsKey("relevance")) analysis.put("relevance", "1");
        
        return analysis;
    }

    private Map<String, String> fallbackMock(String text) {
        String lowerText = text.toLowerCase();
        Map<String, String> mock = new HashMap<>();
        
        String constituency = "General Erode";
        String[] consts = {"Erode (East)", "Erode (West)", "Modakkurichi", "Perundurai", "Bhavani", "Anthiyur", "Gobichettipalayam", "Bhavanisagar"};
        for(String c : consts) {
            if(lowerText.contains(c.toLowerCase().split("\\s")[0])) {
                constituency = c;
                break;
            }
        }
        
        String sentiment = "Neutral";
        String alertLevel = "Low";
        String relevance = String.valueOf(new Random().nextInt(4) + 1);
        
        if (lowerText.contains("protest") || lowerText.contains("cash") || lowerText.contains("violation") || lowerText.contains("violence")) {
            sentiment = "Negative";
            alertLevel = "High";
            relevance = String.valueOf(new Random().nextInt(3) + 8);
        } else if (lowerText.contains("campaign") || lowerText.contains("support") || lowerText.contains("candidate")) {
            sentiment = "Positive";
            alertLevel = "Medium";
            relevance = String.valueOf(new Random().nextInt(4) + 4);
        }

        mock.put("aiSummary", "Review required for post: " + (text.length() > 50 ? text.substring(0, 50) + "..." : text));
        mock.put("sentiment", sentiment);
        mock.put("constituency", constituency);
        mock.put("alertLevel", alertLevel);
        mock.put("relevance", relevance);

        return mock;
    }
}
