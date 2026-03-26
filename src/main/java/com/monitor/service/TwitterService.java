package com.monitor.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.model.Tweet;
import com.monitor.model.TwitterPost;
import com.monitor.repository.TwitterPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TwitterService {

    @Value("${twitter.apiKey}")
    private String apiKey;

    @Value("${twitter.host}")
    private String host;

    private final SentimentService sentimentService;
    private final TwitterPostRepository twitterPostRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Tweet> search(String hashtag) {
        List<Tweet> tweets = new ArrayList<>();
        String rawResponseBody = null;

        try {
            System.out.println("===== CALLING TWITTER API (Native HttpClient) =====");

            // Explicitly add the '#' symbol to the search query so Twitter returns accurate results
            String encodedHashtag = java.net.URLEncoder.encode("#" + hashtag, java.nio.charset.StandardCharsets.UTF_8);

            String urlString = "https://" + host + "/search?query=" + encodedHashtag + "&count=20&type=Top";

            System.out.println("REQUEST URL: " + urlString);
            System.out.println("API HOST: " + host);
            System.out.println("API KEY: " + (apiKey != null && apiKey.length() > 8 ? apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4) : "***"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("x-rapidapi-host", host)
                    .header("x-rapidapi-key", apiKey)
                    .GET()
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();

            for (int i = 1; i <= 3; i++) {
                try {
                    System.out.println("API Attempt: " + i);
                    HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (httpResponse.statusCode() == 200 && httpResponse.body() != null) {
                        System.out.println("✅ API SUCCESS");
                        rawResponseBody = httpResponse.body();
                        break;
                    } else {
                        System.out.println("❌ API RETURNED STATUS: " + httpResponse.statusCode());
                        System.out.println("BODY: " + httpResponse.body());
                        Thread.sleep(1500);
                    }
                } catch (Exception ex) {
                    System.out.println("❌ API FAILED: " + ex.getMessage());
                    Thread.sleep(2000);
                }
            }

            if (rawResponseBody == null) {
                System.out.println("🚨 API FAILED COMPLETELY - USING DB FALLBACK");

                List<TwitterPost> dbPosts = twitterPostRepository.findTop20ByOrderByIdDesc();

                for (TwitterPost post : dbPosts) {
                    String createdAtStr = post.getCreatedAt() != null ? post.getCreatedAt() : "";
                    tweets.add(new Tweet(
                            post.getUsername(),
                            post.getTweetText(),
                            post.getLikes(),
                            post.getComments(),
                            post.getRetweets(),
                            createdAtStr,
                            post.getSource(),
                            post.getSentiment()
                    ));
                }

                ensureNotEmpty(tweets);
                return tweets;
            }

            System.out.println("===== FULL API RESPONSE =====");

            JsonNode root = objectMapper.readTree(rawResponseBody);

            // ✅ FIX: Pass the hashtag down into the parser to do the strict text filtering
            extractTweetsRecursive(root, tweets, hashtag);

            System.out.println("TOTAL TWEETS PARSED: " + tweets.size());

            for (Tweet tweet : tweets) {
                try {
                    String sentiment = sentimentService.analyzeSentiment(tweet.getText());
                    tweet.setSentiment(sentiment);

                    TwitterPost post = new TwitterPost();
                    post.setUsername(tweet.getUsername());
                    post.setTweetText(tweet.getText());
                    post.setLikes(tweet.getLikes());
                    post.setComments(tweet.getComments());
                    post.setRetweets(tweet.getRetweets());

                    String createdAtStr = tweet.getCreatedAt();
                    if (createdAtStr != null && !createdAtStr.isEmpty()) {
                        post.setCreatedAt(createdAtStr);
                    } else {
                        post.setCreatedAt(java.time.LocalDateTime.now().toString());
                    }

                    post.setSentiment(sentiment);
                    post.setSource(tweet.getSource());

                    twitterPostRepository.save(post);

                } catch (Exception e) {
                    System.out.println("Error saving tweet to DB: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.out.println("🚨 CRITICAL ERROR:");
            e.printStackTrace();
        }

        ensureNotEmpty(tweets);
        return tweets;
    }

    private void ensureNotEmpty(List<Tweet> tweets) {
        if (tweets == null || tweets.isEmpty()) {
            System.out.println("🚨 No tweets found");
            // Do NOT throw exception
        }
    }

    // ✅ FIX: Method signature now expects the hashtag string
    private void extractTweetsRecursive(JsonNode node, List<Tweet> tweets, String hashtag) {
        if (node.isObject()) {
            if (node.has("legacy") && node.has("core") && node.get("legacy").has("full_text")) {
                JsonNode legacy = node.get("legacy");

                String text = legacy.get("full_text").asText("");

                // ✅ FIX: Strict Java-side filter. If the tweet doesn't have the hashtag, skip it!
                if (text == null || text.isEmpty()) {
                    return;
                }

                String createdAt = legacy.has("created_at") ? legacy.get("created_at").asText() : "";

                // 3-Day Filter
                if (!createdAt.isEmpty()) {
                    try {
                        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", java.util.Locale.ENGLISH);
                        java.time.ZonedDateTime tweetDate = java.time.ZonedDateTime.parse(createdAt, formatter);
                        java.time.ZonedDateTime threeDaysAgo = java.time.ZonedDateTime.now().minusDays(3);

                        if (tweetDate.isBefore(threeDaysAgo)) {
                            return;
                        }
                    } catch (Exception e) {
                        System.out.println("⚠️ Could not parse date for filtering: " + createdAt);
                    }
                }

                int likes = legacy.has("favorite_count") ? legacy.get("favorite_count").asInt() : 0;
                int comments = legacy.has("reply_count") ? legacy.get("reply_count").asInt() : 0;
                int retweets = legacy.has("retweet_count") ? legacy.get("retweet_count").asInt() : 0;

                String tweetUrl = "N/A";
                if (node.has("rest_id")) {
                    String id = node.get("rest_id").asText();
                    tweetUrl = "https://twitter.com/i/web/status/" + id;
                }

                String username = "Unknown";
                JsonNode core = node.get("core");

                if (core != null) {
                    JsonNode nameNode = core.findValue("name");
                    if (nameNode != null && !nameNode.isNull()) {
                        username = nameNode.asText();
                    }
                }

                tweets.add(new Tweet(
                        username,
                        text,
                        likes,
                        comments,
                        retweets,
                        createdAt,
                        tweetUrl,
                        "Neutral"
                ));
            } else {
                // ✅ FIX: Pass the hashtag into the recursive loops
                node.elements().forEachRemaining(child -> extractTweetsRecursive(child, tweets, hashtag));
            }
        } else if (node.isArray()) {
            // ✅ FIX: Pass the hashtag into the recursive loops
            node.elements().forEachRemaining(child -> extractTweetsRecursive(child, tweets, hashtag));
        }
    }

    public List<Tweet> getLatestSavedTweets() {
        List<TwitterPost> dbPosts = twitterPostRepository.findTop20ByOrderByIdDesc();
        List<Tweet> tweets = new ArrayList<>();
        for (TwitterPost post : dbPosts) {
            String createdAtStr = post.getCreatedAt() != null ? post.getCreatedAt() : "";
            tweets.add(new Tweet(
                    post.getUsername(),
                    post.getTweetText(),
                    post.getLikes(),
                    post.getComments(),
                    post.getRetweets(),
                    createdAtStr,
                    post.getSource(),
                    post.getSentiment()
            ));
        }
        return tweets;
    }
}
