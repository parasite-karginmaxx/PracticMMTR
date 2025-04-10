import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DictionaryApp {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        DictionaryService alphaDict = new DictionaryService(DictionaryType.ALPHA, "dictionary_alpha.txt");
        DictionaryService digitDict = new DictionaryService(DictionaryType.DIGIT, "dictionary_digit.txt");
        DictionaryService backDict = new DictionaryService(DictionaryType.BACKSPACE, "dictionary_backspace.txt");

        DictionaryService currentDict = null;

        while (true) {
            System.out.println("\n=== МЕНЮ ===");
            System.out.println("1. Выбрать словарь (ALPHA / DIGIT / BACKSPACE)");
            System.out.println("2. Показать содержимое словаря");
            System.out.println("3. Добавить слово");
            System.out.println("4. Найти слово");
            System.out.println("5. Удалить слово");
            System.out.println("6. Показать страницу словаря (н-р: стр 2, по 5 эл.)");
            System.out.println("7. Вывести словарь в формате XML");
            System.out.println("0. Выход");
            System.out.print("Выбор: ");
            String input = scanner.nextLine();

            switch (input) {
                case "1" -> {
                    System.out.print("Введите ALPHA / DIGIT / BACKSPACE: ");
                    String type = scanner.nextLine().toUpperCase();
                    switch (type) {
                        case "ALPHA" -> {
                            currentDict = alphaDict;
                            System.out.println("Выбран словарь ALPHA.");
                        }
                        case "DIGIT" -> {
                            currentDict = digitDict;
                            System.out.println("Выбран словарь DIGIT.");
                        }
                        case "BACKSPACE" -> {
                            currentDict = backDict;
                            System.out.println("Выбран словарь BACKSPACE.");
                        }
                        default -> System.out.println("Неверный выбор.");
                    }
                }

                case "2" -> {
                    if (checkSelected(currentDict)) {
                        currentDict.displayAll();
                    }
                }

                case "3" -> {
                    if (checkSelected(currentDict)) {
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
                }

                case "4" -> {
                    if (checkSelected(currentDict)) {
                        System.out.print("Введите ключ: ");
                        String key = scanner.nextLine();
                        String value = currentDict.findWord(key);
                        if (value != null) {
                            System.out.println("Перевод: " + value);
                        } else {
                            System.out.println("Слово не найдено.");
                        }
                    }
                }

                case "5" -> {
                    if (checkSelected(currentDict)) {
                        System.out.print("Введите ключ: ");
                        String key = scanner.nextLine();
                        if (currentDict.deleteWord(key)) {
                            System.out.println("Слово удалено.");
                        } else {
                            System.out.println("Слово не найдено.");
                        }
                    }
                }

                case "6" -> {
                    if (checkSelected(currentDict)) {
                        try {
                            System.out.print("Введите номер страницы: ");
                            int page = Integer.parseInt(scanner.nextLine());
                            System.out.print("Введите размер страницы: ");
                            int size = Integer.parseInt(scanner.nextLine());
                            List<Map.Entry<String, String>> pageEntries = currentDict.getPage(page, size);
                            if (pageEntries.isEmpty()) {
                                System.out.println("Пустая страница.");
                            } else {
                                System.out.println("Страница " + page + ":");
                                for (var entry : pageEntries) {
                                    System.out.println(entry.getKey() + " = " + entry.getValue());
                                }
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Ошибка: неверный формат числа.");
                        }
                    }
                }

                case "7" -> {
                    if (checkSelected(currentDict)) {
                        System.out.println("Вывод в формате XML:");
                        currentDict.printAsXml();
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

    private static boolean checkSelected(DictionaryService dict) {
        if (dict == null) {
            System.out.println("Сначала выберите словарь.");
            return false;
        }
        return true;
    }
}
