package com.backend.gapfinder.repositories;

import com.backend.gapfinder.models.GapModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GapRepository extends JpaRepository<GapModel, Long> {
}