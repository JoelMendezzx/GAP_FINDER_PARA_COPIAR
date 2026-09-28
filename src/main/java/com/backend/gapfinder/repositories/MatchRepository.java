package com.backend.gapfinder.repositories;

import com.backend.gapfinder.enums.MatchStatusEnum;
import com.backend.gapfinder.models.MatchModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<MatchModel, Long> {

    // Get all matches with a given status (used to auto-complete expired ones)
    List<MatchModel> findByStatus(MatchStatusEnum status);
}