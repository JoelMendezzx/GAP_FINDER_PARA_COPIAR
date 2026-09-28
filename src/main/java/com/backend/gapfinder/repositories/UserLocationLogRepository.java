package com.backend.gapfinder.repositories;

import com.backend.gapfinder.models.UserLocationLogModel;
import com.backend.gapfinder.repositories.projections.BuildingGapPresenceProjection;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserLocationLogRepository extends JpaRepository<UserLocationLogModel, Long> {

    // Calculates current student presence and free time metrics per building based on location logs during gaps
    @Query(value = """
        SELECT b.id AS buildingId,
               b.name AS buildingName,
               COUNT(DISTINCT x.user_id) AS studentCount,
               COUNT(*) AS gapCount,
               COALESCE(SUM(x.gap_minutes), 0) AS totalGapMinutes
        FROM (
            SELECT DISTINCT ON (g.id)
                   g.id, g.user_id, l.building_id,
                   EXTRACT(EPOCH FROM (g.end_time - g.start_time)) / 60.0 AS gap_minutes
            FROM gap g
            JOIN user_location_log l
              ON l.user_id = g.user_id
             AND l.timestamp BETWEEN g.start_time AND g.end_time
            ORDER BY g.id, l.timestamp DESC
        ) x
        JOIN building b ON b.id = x.building_id
        GROUP BY b.id, b.name
        ORDER BY studentCount DESC, totalGapMinutes DESC
        """, nativeQuery = true)
    List<BuildingGapPresenceProjection> findGapPresenceByBuilding();

}