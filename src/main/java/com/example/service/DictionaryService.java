package com.example.service;

import com.example.model.Dictionary;
import com.example.model.DictionaryEntry;
import com.example.repository.DictionaryRepository;
import com.example.repository.DictionaryEntryRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

@Service
public class DictionaryService {

    private final DictionaryRepository dictionaryRepository;
    private final DictionaryEntryRepository entryRepository;

    public DictionaryService(DictionaryRepository dictionaryRepository, DictionaryEntryRepository entryRepository) {
        this.dictionaryRepository = dictionaryRepository;
        this.entryRepository = entryRepository;
    }

    public void add(String dictionaryName, String key, String value) {
        Dictionary dictionary = dictionaryRepository.findByNameAndDeletedFalse(dictionaryName)
                .orElseThrow(() -> new IllegalArgumentException("Dictionary not found"));

        if (!key.matches(dictionary.getValidationRegex())) {
            throw new IllegalArgumentException("Invalid key format");
        }

        DictionaryEntry entry = DictionaryEntry.builder()
                .dictionary(dictionary)
                .key(key)
                .value(value)
                .build();

        entryRepository.save(entry);
    }

    public List<DictionaryEntry> find(String dictionaryName, String key) {
        Dictionary dictionary = dictionaryRepository.findByNameAndDeletedFalse(dictionaryName)
                .orElseThrow(() -> new IllegalArgumentException("Dictionary not found"));
        return entryRepository.findAllByDictionaryAndKey(dictionary, key);
    }

    public boolean delete(String dictionaryName, String key) {
        Dictionary dictionary = dictionaryRepository.findByNameAndDeletedFalse(dictionaryName)
                .orElseThrow(() -> new IllegalArgumentException("Dictionary not found"));

        List<DictionaryEntry> entries = entryRepository.findAllByDictionaryAndKey(dictionary, key);
        if (!entries.isEmpty()) {
            entryRepository.deleteAll(entries);
            return true;
        }
        return false;
    }

    public Page<DictionaryEntry> getFilteredPage(String type, String key, String value, Pageable pageable) {
        if (type != null && !"all".equalsIgnoreCase(type)) {
            Dictionary dictionary = dictionaryRepository.findByNameAndDeletedFalse(type)
                    .orElseThrow(() -> new IllegalArgumentException("Dictionary not found"));

            if (key != null && !key.isEmpty()) {
                return entryRepository.findByDictionaryAndKey(dictionary, key, pageable);
            } else if (value != null && !value.isEmpty()) {
                return entryRepository.findByDictionaryAndValueContainingIgnoreCase(dictionary, value, pageable);
            } else {
                return entryRepository.findByDictionary(dictionary, pageable);
            }

        } else {
            return entryRepository.globalSearch(key, value, pageable);
        }
    }

    public InputStreamResource exportXml(String dictionaryName) throws IOException {
        Dictionary dictionary = dictionaryRepository.findByNameAndDeletedFalse(dictionaryName)
                .orElseThrow(() -> new IllegalArgumentException("Dictionary not found"));

        List<DictionaryEntry> entries = entryRepository.findByDictionary(dictionary, Pageable.unpaged()).getContent();

        File temp = File.createTempFile("dictionary-", ".xml");
        try (PrintWriter writer = new PrintWriter(new FileWriter(temp))) {
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<dictionary name=\"" + dictionaryName + "\">");
            for (DictionaryEntry entry : entries) {
                writer.println("  <entry>");
                writer.printf("    <key>%s</key>%n", entry.getKey());
                writer.printf("    <value>%s</value>%n", entry.getValue());
                writer.println("  </entry>");
            }
            writer.println("</dictionary>");
        }
        return new InputStreamResource(Files.newInputStream(temp.toPath()));
    }

    public String getRegexFor(String dictionaryName) {
        Dictionary dictionary = dictionaryRepository.findByNameAndDeletedFalse(dictionaryName)
                .orElseThrow(() -> new IllegalArgumentException("Dictionary not found"));
        return dictionary.getValidationRegex();
    }
}
