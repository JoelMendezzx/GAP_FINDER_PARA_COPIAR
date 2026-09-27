package com.backend.gapfinder.repositories;

import com.backend.gapfinder.models.GapModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GapRepository extends JpaRepository<GapModel, Long> {

    // Finds potential gaps that overlap in time and belong to other users
    @Query("SELECT g FROM GapModel g WHERE g.user.id <> :userId " +
           "AND g.startTime < :endTime AND g.endTime > :startTime")
    List<GapModel> findOverlappingGapsExcludingUser(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}