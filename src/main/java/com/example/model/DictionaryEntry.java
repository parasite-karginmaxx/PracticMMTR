package com.example.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dictionary_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictionaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dictionary_id", nullable = false)
    private Dictionary dictionary;
}
