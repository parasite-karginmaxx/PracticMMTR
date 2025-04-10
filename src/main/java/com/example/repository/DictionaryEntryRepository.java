package com.example.repository;

import com.example.model.Dictionary;
import com.example.model.DictionaryEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DictionaryEntryRepository extends JpaRepository<DictionaryEntry, Long> {

    List<DictionaryEntry> findAllByDictionaryAndKey(Dictionary dictionary, String key);
    Page<DictionaryEntry> findByDictionaryAndKey(Dictionary dictionary, String key, Pageable pageable);
    Page<DictionaryEntry> findByDictionaryAndValueContainingIgnoreCase(Dictionary dictionary, String value, Pageable pageable);
    Page<DictionaryEntry> findByDictionary(Dictionary dictionary, Pageable pageable);

    @Query("SELECT e FROM DictionaryEntry e WHERE e.dictionary.deleted = false AND (:key IS NULL OR e.key = :key) AND (:value IS NULL OR LOWER(e.value) LIKE LOWER(CONCAT('%', :value, '%')))")
    Page<DictionaryEntry> globalSearch(@Param("key") String key,
                                       @Param("value") String value,
                                       Pageable pageable);
}
