package com.backend.gapfinder.models;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data

public class UserModel {

    // Unique user identifier
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // Full name of the student
    @Column(nullable = false)
    private String name;

    // Contact phone number
    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    // Academic program or major
    @Column(nullable = false)
    private String career;

    // List of student's personal interests
    @ManyToMany
    @JoinTable(
        name = "user_interests",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "interest_id")
    )
    private List<InterestModel> interests;

    // Current building on campus (for context-aware features)
    @ManyToOne
    @JoinColumn(name = "current_building_id", nullable = true)
    private BuildingModel currentBuilding;

    // Last time the current location was updated
    @Column(name = "location_updated_at", nullable = true)
    private LocalDateTime locationUpdatedAt;
}