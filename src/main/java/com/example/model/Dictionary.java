package com.example.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "dictionaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String validationRegex;

    @Builder.Default
    private boolean deleted = false;

    @OneToMany(mappedBy = "dictionary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DictionaryEntry> entries;
}
