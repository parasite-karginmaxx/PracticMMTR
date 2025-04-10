package com.example.controller;

import com.example.DictionaryType;
import com.example.dto.EntryRequest;
import com.example.service.DictionaryService;
import com.example.util.LocaleMessageResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/dictionaries/{type}")
public class DictionaryController {

    private final DictionaryService service;
    private final LocaleMessageResolver messageResolver;

    @Value("${app.admin-token}")
    private String adminToken;

    public DictionaryController(DictionaryService service, LocaleMessageResolver messageResolver) {
        this.service = service;
        this.messageResolver = messageResolver;
    }

    @PostMapping("/entries")
    public ResponseEntity<?> addEntry(@PathVariable String type, @RequestBody EntryRequest request, Locale locale) {
        DictionaryType dictType = DictionaryType.fromString(type);
        if (!service.isValidKey(dictType, request.key)) {
            return ResponseEntity.badRequest().body("Invalid key format");
        }
        try {
            service.add(dictType, request.key, request.value);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            String message = messageResolver.getMessage(e.getMessage(), locale);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("File error");
        }
    }

    @GetMapping("/entries/{key}")
    public ResponseEntity<?> findEntry(@PathVariable String type, @PathVariable String key) throws IOException {
        DictionaryType dictType = DictionaryType.fromString(type);
        String value = service.find(dictType, key);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("key", key, "value", value));
    }

    @DeleteMapping("/entries/{key}")
    public ResponseEntity<?> deleteEntry(@PathVariable String type, @PathVariable String key,
                                         @RequestHeader(value = "X-API-KEY", required = false) String token) throws IOException {
        if (!adminToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid API key");
        }

        DictionaryType dictType = DictionaryType.fromString(type);
        boolean removed = service.delete(dictType, key);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/entries/page")
    public List<Map<String, String>> getPage(@PathVariable String type,
                                             @RequestParam int page,
                                             @RequestParam int size) throws IOException {
        DictionaryType dictType = DictionaryType.fromString(type);
        List<Map.Entry<String, String>> entries = service.getPage(dictType, page, size);
        List<Map<String, String>> result = new ArrayList<>();
        for (var entry : entries) {
            result.add(Map.of("key", entry.getKey(), "value", entry.getValue()));
        }
        return result;
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportXml(@PathVariable String type) throws IOException {
        DictionaryType dictType = DictionaryType.fromString(type);
        InputStreamResource resource = service.exportXml(dictType);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(resource);
    }
}
