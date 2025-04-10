package com.example.repository;

import com.example.model.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DictionaryRepository extends JpaRepository<Dictionary, Long> {
    Optional<Dictionary> findByNameAndDeletedFalse(String name);
    boolean existsByNameAndDeletedFalse(String name);
}
