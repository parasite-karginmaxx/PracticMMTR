package com.example.controller;

import com.example.model.Dictionary;
import com.example.repository.DictionaryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/dictionaries")
public class AdminDictionaryController {

    private final DictionaryRepository dictionaryRepository;

    @Value("${app.admin-token}")
    private String adminToken;

    public AdminDictionaryController(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
    }

    private boolean isAuthorized(String token) {
        return adminToken.equals(token);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("X-API-KEY") String token,
                                    @RequestBody Map<String, String> body) {
        if (!isAuthorized(token)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String name = body.get("name");
        String regex = body.get("validationRegex");

        if (dictionaryRepository.existsByNameAndDeletedFalse(name)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Dictionary already exists");
        }

        Dictionary dict = Dictionary.builder()
                .name(name)
                .validationRegex(regex)
                .deleted(false)
                .build();

        return ResponseEntity.ok(dictionaryRepository.save(dict));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDelete(@RequestHeader("X-API-KEY") String token,
                                        @PathVariable Long id) {
        if (!isAuthorized(token)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Dictionary> dict = dictionaryRepository.findById(id);
        if (dict.isEmpty()) return ResponseEntity.notFound().build();

        Dictionary d = dict.get();
        d.setDeleted(true);
        dictionaryRepository.save(d);
        return ResponseEntity.ok().build();
    }
}
