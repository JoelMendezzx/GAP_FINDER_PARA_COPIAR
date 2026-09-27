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

public class GapModel {

    // Unique gap identifier
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // Student who owns this free time slot
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    // Start time of the free time slot
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    // End time of the free time slot
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
}