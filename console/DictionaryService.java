import java.io.*;
import java.util.*;

public class DictionaryService {
    private final File file;
    private final DictionaryType type;

    public DictionaryService(DictionaryType type, String fileName) {
        this.type = type;
        this.file = new File(fileName);
    }

    public Map<String, String> loadDictionary() throws IOException {
        Map<String, String> dict = new LinkedHashMap<>();
        if (!file.exists()) return dict;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2)
                    dict.put(parts[0], parts[1]);
            }
        }
        return dict;
    }

    public void saveDictionary(Map<String, String> dict) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (var entry : dict.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
        }
    }

    public boolean isValidKey(String key) {
        return switch (type) {
            case ALPHA -> WordValidator.isValidAlpha(key);
            case DIGIT -> WordValidator.isValidDigit(key);
        };
    }

    public void addWord(String key, String value) throws IOException {
        if (!isValidKey(key)) throw new IllegalArgumentException("Invalid key format");
        var dict = loadDictionary();
        dict.put(key, value);
        saveDictionary(dict);
    }

    public String findWord(String key) throws IOException {
        return loadDictionary().getOrDefault(key, null);
    }

    public boolean deleteWord(String key) throws IOException {
        var dict = loadDictionary();
        if (dict.remove(key) != null) {
            saveDictionary(dict);
            return true;
        }
        return false;
    }

    public void displayAll() throws IOException {
        var dict = loadDictionary();
        if (dict.isEmpty()) {
            System.out.println("Словарь пуст.");
        } else {
            dict.forEach((k, v) -> System.out.println(k + " = " + v));
        }
    }
}
