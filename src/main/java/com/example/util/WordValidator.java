package com.example.util;

public class WordValidator {
    public static boolean isValidAlpha(String word) {
        return word.matches("[a-zA-Z]{4}");
    }

    public static boolean isValidDigit(String word) {
        return word.matches("\\d{5}");
    }

    public static boolean isValidBackspaceKey(String word) {
        return word.matches("[a-z#]+");
    }

    public static String applyBackspace(String key) {
        StringBuilder result = new StringBuilder();
        for (char c : key.toCharArray()) {
            if (c == '#') {
                if (!result.isEmpty()) result.deleteCharAt(result.length() - 1);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
