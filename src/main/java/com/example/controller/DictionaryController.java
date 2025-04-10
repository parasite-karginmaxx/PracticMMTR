package com.example.controller;

import com.example.dto.EntryRequest;
import com.example.model.DictionaryEntry;
import com.example.service.DictionaryService;
import com.example.util.LocaleMessageResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        try {
            String regex = service.getRegexFor(type);
            if (!request.key.matches(regex)) {
                return ResponseEntity.badRequest().body("Invalid key format");
            }
            service.add(type, request.key, request.value);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            String message = messageResolver.getMessage(e.getMessage(), locale);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        }
    }

    @GetMapping("/entries/{key}")
    public ResponseEntity<?> findEntry(@PathVariable String type, @PathVariable String key) {
        try {
            List<DictionaryEntry> result = service.find(type, key);
            if (result.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }
    }

    @DeleteMapping("/entries/{key}")
    public ResponseEntity<?> deleteEntry(@PathVariable String type, @PathVariable String key,
                                         @RequestHeader(value = "X-API-KEY", required = false) String token) {
        if (!adminToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid API key");
        }
        boolean removed = service.delete(type, key);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/entries/page")
    public ResponseEntity<?> getPage(
            @PathVariable String type,
            @RequestParam Optional<String> key,
            @RequestParam Optional<String> value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<DictionaryEntry> result = service.getFilteredPage(type, key.orElse(null), value.orElse(null), pageable);

        List<Map<String, String>> response = result.stream()
                .map(entry -> Map.of(
                        "dictionary", entry.getDictionary().getName(),
                        "key", entry.getKey(),
                        "value", entry.getValue()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportXml(@PathVariable String type) throws IOException {
        InputStreamResource resource = service.exportXml(type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(resource);
    }
}
