package com.monitor.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SentimentService {

    private static final Set<String> STRONG_POSITIVE = Set.of(
            "excellent", "amazing", "awesome", "fantastic", "best", "love", "great"
    );

    private static final Set<String> STRONG_NEGATIVE = Set.of(
            "worst", "terrible", "disaster", "danger", "threat", "failure", "scam"
    );

    private static final Set<String> WEAK_POSITIVE = Set.of(
            "good", "nice", "happy", "safe", "healthy", "success", "fast"
    );

    private static final Set<String> WEAK_NEGATIVE = Set.of(
            "bad", "issue", "problem", "risk", "damage", "loss"
    );

    private static final Set<String> POSITIVE_EMOJIS = Set.of(
            "😊", "😄", "😍", "🔥", "🎉", "💪", "👍", "❤️"
    );

    private static final Set<String> NEGATIVE_EMOJIS = Set.of(
            "😡", "😠", "😢", "😭", "💔", "👎", "⚠️", "🚫"
    );

    private static final Set<String> INTENSIFIERS = Set.of(
            "very", "extremely", "really", "super"
    );

    public String analyzeSentiment(String text) {

        if (text == null || text.trim().isEmpty()) {
            return "Neutral";
        }

        String lower = text.toLowerCase();
        String[] words = lower.split("\\s+");

        int score = 0;
        boolean negation = false;
        int multiplier = 1;

        for (int i = 0; i < words.length; i++) {

            String word = words[i];

            // 🔹 Negation handling
            if (word.equals("not") || word.equals("no") || word.equals("never")) {
                negation = true;
                continue;
            }

            // 🔹 Intensifiers
            if (INTENSIFIERS.contains(word)) {
                multiplier = 2;
                continue;
            }

            int tempScore = 0;

            if (STRONG_POSITIVE.contains(word)) {
                tempScore = 3;
            } else if (STRONG_NEGATIVE.contains(word)) {
                tempScore = -3;
            } else if (WEAK_POSITIVE.contains(word)) {
                tempScore = 1;
            } else if (WEAK_NEGATIVE.contains(word)) {
                tempScore = -1;
            }

            // Apply negation
            if (negation) {
                tempScore *= -1;
                negation = false;
            }

            score += tempScore * multiplier;
            multiplier = 1;
        }

        // 🔹 Emoji boost
        for (String emoji : POSITIVE_EMOJIS) {
            if (text.contains(emoji)) {
                score += 3;
            }
        }

        for (String emoji : NEGATIVE_EMOJIS) {
            if (text.contains(emoji)) {
                score -= 3;
            }
        }

        // 🔹 Hashtag sentiment
        for (String word : words) {
            if (word.startsWith("#")) {
                String tag = word.substring(1);

                if (tag.contains("happy") || tag.contains("success") || tag.contains("win")) {
                    score += 2;
                }

                if (tag.contains("fail") || tag.contains("loss") || tag.contains("problem")) {
                    score -= 2;
                }
            }
        }

        // 🔹 Context bias (VERY IMPORTANT)
        if (lower.contains("risk") || lower.contains("threat") || lower.contains("disease")) {
            score -= 2;
        }

        // 🔹 ALL CAPS boost
        int upper = 0, total = 0;
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                total++;
                if (Character.isUpperCase(c)) {
                    upper++;
                }
            }
        }
        if (total > 5 && ((double) upper / total) > 0.7) {
            score += 1;
        }

        // 🔥 FINAL DECISION (tuned)
        if (score >= 3) {
            return "Positive";
        }
        if (score <= -3) {
            return "Negative";
        }
        return "Neutral";
    }
}
