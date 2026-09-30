package com.backend.gapfinder.repositories;

import com.backend.gapfinder.models.OpenTableParticipantModel;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OpenTableParticipantRepository extends JpaRepository<OpenTableParticipantModel, Long> {

    // Checks if a student is already registered as a participant in a specific open table
    boolean existsByOpenTableIdAndUserId(Long openTableId, Long userId);
}
