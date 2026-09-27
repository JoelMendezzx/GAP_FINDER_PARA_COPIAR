package com.backend.gapfinder.models;

import com.backend.gapfinder.enums.EffortTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class ActivityModel {

    // Unique activity identifier
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    // Name of the activity (e.g., Study Session, Coffee Break, Board Game)
    @Column(nullable = false)
    private String name;

    // Associated category or topic of interest
    @ManyToOne
    @JoinColumn(name = "interest_id", nullable = false)
    private InterestModel interest;

    // Estimated duration of the activity in minutes
    @Column(nullable = false)
    private Integer durationMinutes;

    // Energy or engagement level required (e.g., LOW, MEDIUM, HIGH)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EffortTypeEnum effortType;
}