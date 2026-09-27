package com.backend.gapfinder.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data

public class InterestModel {

    // Unique interest identifier
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // Name of the interest or hobby (e.g., Gaming, Sports, Music)
    @Column(nullable = false, unique = true)
    private String name;
}