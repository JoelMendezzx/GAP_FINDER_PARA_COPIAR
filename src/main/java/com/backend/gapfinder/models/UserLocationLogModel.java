package com.backend.gapfinder.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data

public class UserLocationLogModel {

    // Unique location log identifier
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // Student whose location was logged
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    // Campus building visited by the student
    @ManyToOne
    @JoinColumn(name = "building_id", nullable = false)
    private BuildingModel building;

    // Date and time when the location was recorded
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}