public class WordValidator {
    public static boolean isValidAlpha(String word) {
        return word.matches("[a-zA-Z]{4}");
    }

    public static boolean isValidDigit(String word) {
        return word.matches("\\d{5}");
    }
}

