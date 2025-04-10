import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
            case BACKSPACE -> WordValidator.isValidBackspaceKey(key);
        };
    }

    public void addWord(String key, String value) throws IOException {
        if (!isValidKey(key)) throw new IllegalArgumentException("Invalid key format");

        Map<String, String> dict = loadDictionary();

        if (type == DictionaryType.BACKSPACE) {
            String processedKey = WordValidator.applyBackspace(key);
            for (String k : dict.keySet()) {
                if (WordValidator.applyBackspace(k).equals(processedKey)) {
                    throw new IllegalArgumentException("Дубликат ключа (после backspace)");
                }
            }
        }

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

    public List<Map.Entry<String, String>> getPage(int pageNumber, int pageSize) throws IOException {
        List<Map.Entry<String, String>> all = new ArrayList<>(loadDictionary().entrySet());
        int from = Math.min((pageNumber - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        return all.subList(from, to);
    }

    public void printAsXml() throws Exception {
        Map<String, String> dict = loadDictionary();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("dictionary");
        doc.appendChild(root);

        for (var entry : dict.entrySet()) {
            Element word = doc.createElement("entry");

            Element key = doc.createElement("key");
            key.appendChild(doc.createTextNode(entry.getKey()));
            word.appendChild(key);

            Element value = doc.createElement("value");
            value.appendChild(doc.createTextNode(entry.getValue()));
            word.appendChild(value);

            root.appendChild(word);
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(System.out));
    }
}
