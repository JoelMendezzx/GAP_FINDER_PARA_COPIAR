package com.backend.gapfinder.repositories;

import com.backend.gapfinder.models.MatchModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<MatchModel, Long> {
}
