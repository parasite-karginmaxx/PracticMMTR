import java.io.IOException;
import java.util.Scanner;

public class DictionaryApp {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        DictionaryService alphaDict = new DictionaryService(DictionaryType.ALPHA, "dictionary_alpha.txt");
        DictionaryService digitDict = new DictionaryService(DictionaryType.DIGIT, "dictionary_digit.txt");

        DictionaryService currentDict = null;

        while (true) {
            System.out.println("\n=== МЕНЮ ===");
            System.out.println("1. Выбрать словарь (ALPHA или DIGIT)");
            System.out.println("2. Показать содержимое словаря");
            System.out.println("3. Добавить слово");
            System.out.println("4. Найти слово");
            System.out.println("5. Удалить слово");
            System.out.println("0. Выход");
            System.out.print("Выбор: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1" -> {
                    System.out.print("Введите ALPHA (4 буквы) или DIGIT (5 цифр): ");
                    String type = scanner.nextLine().toUpperCase();
                    if (type.equals("ALPHA")) {
                        currentDict = alphaDict;
                        System.out.println("Выбран словарь ALPHA.");
                    } else if (type.equals("DIGIT")) {
                        currentDict = digitDict;
                        System.out.println("Выбран словарь DIGIT.");
                    } else {
                        System.out.println("Неверный выбор.");
                    }
                }
                case "2" -> {
                    if (currentDict == null) {
                        System.out.println("Сначала выберите словарь.");
                    } else {
                        currentDict.displayAll();
                    }
                }
                case "3" -> {
                    if (currentDict == null) {
                        System.out.println("Сначала выберите словарь.");
                        continue;
                    }
                    System.out.print("Введите ключ: ");
                    String key = scanner.nextLine();
                    System.out.print("Введите перевод: ");
                    String value = scanner.nextLine();
                    try {
                        currentDict.addWord(key, value);
                        System.out.println("Слово добавлено.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                }
                case "4" -> {
                    if (currentDict == null) {
                        System.out.println("Сначала выберите словарь.");
                        continue;
                    }
                    System.out.print("Введите ключ: ");
                    String key = scanner.nextLine();
                    String value = currentDict.findWord(key);
                    if (value != null) {
                        System.out.println("Перевод: " + value);
                    } else {
                        System.out.println("Слово не найдено.");
                    }
                }
                case "5" -> {
                    if (currentDict == null) {
                        System.out.println("Сначала выберите словарь.");
                        continue;
                    }
                    System.out.print("Введите ключ: ");
                    String key = scanner.nextLine();
                    if (currentDict.deleteWord(key)) {
                        System.out.println("Слово удалено.");
                    } else {
                        System.out.println("Слово не найдено.");
                    }
                }
                case "0" -> {
                    System.out.println("Выход...");
                    return;
                }
                default -> System.out.println("Неверная команда.");
            }
        }
    }
}
