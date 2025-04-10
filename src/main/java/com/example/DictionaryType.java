package com.example;

public enum DictionaryType {
    ALPHA,
    DIGIT,
    BACKSPACE;

    public static DictionaryType fromString(String value) {
        return switch (value.toLowerCase()) {
            case "alpha" -> ALPHA;
            case "digit" -> DIGIT;
            case "backspace" -> BACKSPACE;
            default -> throw new IllegalArgumentException("Invalid dictionary type: " + value);
        };
    }
}
