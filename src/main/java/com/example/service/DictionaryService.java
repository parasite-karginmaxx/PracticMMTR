package com.example.service;

import com.example.DictionaryType;
import com.example.util.WordValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

@Service
public class DictionaryService {
    private final Map<DictionaryType, File> fileMap = new HashMap<>();

    @Value("${dictionary.alpha.path:dictionary_alpha.txt}")
    private String alphaPath;

    @Value("${dictionary.digit.path:dictionary_digit.txt}")
    private String digitPath;

    @Value("${dictionary.backspace.path:dictionary_backspace.txt}")
    private String backspacePath;

    @PostConstruct
    public void init() {
        fileMap.put(DictionaryType.ALPHA, new File(alphaPath));
        fileMap.put(DictionaryType.DIGIT, new File(digitPath));
        fileMap.put(DictionaryType.BACKSPACE, new File(backspacePath));
    }

    public boolean isValidKey(DictionaryType type, String key) {
        return switch (type) {
            case ALPHA -> WordValidator.isValidAlpha(key);
            case DIGIT -> WordValidator.isValidDigit(key);
            case BACKSPACE -> WordValidator.isValidBackspaceKey(key);
        };
    }

    public Map<String, String> load(DictionaryType type) throws IOException {
        Map<String, String> map = new LinkedHashMap<>();
        File file = fileMap.get(type);
        if (!file.exists()) return map;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }
        }
        return map;
    }

    public void save(DictionaryType type, Map<String, String> map) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileMap.get(type)))) {
            for (var entry : map.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
        }
    }

    public void add(DictionaryType type, String key, String value) throws IOException {
        Map<String, String> dict = load(type);

        if (type == DictionaryType.BACKSPACE) {
            String newProcessed = WordValidator.applyBackspace(key);
            for (String existingKey : dict.keySet()) {
                if (WordValidator.applyBackspace(existingKey).equals(newProcessed)) {
                    throw new IllegalArgumentException("error.duplicate");
                }
            }
        }

        dict.put(key, value);
        save(type, dict);
    }

    public String find(DictionaryType type, String key) throws IOException {
        return load(type).get(key);
    }

    public boolean delete(DictionaryType type, String key) throws IOException {
        Map<String, String> dict = load(type);
        if (dict.remove(key) != null) {
            save(type, dict);
            return true;
        }
        return false;
    }

    public List<Map.Entry<String, String>> getPage(DictionaryType type, int page, int size) throws IOException {
        List<Map.Entry<String, String>> entries = new ArrayList<>(load(type).entrySet());
        int from = Math.min((page - 1) * size, entries.size());
        int to = Math.min(from + size, entries.size());
        return entries.subList(from, to);
    }

    public InputStreamResource exportXml(DictionaryType type) throws IOException {
        File temp = File.createTempFile("dictionary-", ".xml");
        try (PrintWriter writer = new PrintWriter(new FileWriter(temp))) {
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<dictionary>");
            for (var entry : load(type).entrySet()) {
                writer.println("  <entry>");
                writer.printf("    <key>%s</key>%n", entry.getKey());
                writer.printf("    <value>%s</value>%n", entry.getValue());
                writer.println("  </entry>");
            }
            writer.println("</dictionary>");
        }
        return new InputStreamResource(Files.newInputStream(temp.toPath()));
    }
}
