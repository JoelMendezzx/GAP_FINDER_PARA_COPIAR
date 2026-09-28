package com.backend.gapfinder.services;

import com.backend.gapfinder.dto.responses.BuildingGapPresenceResponseDTO;
import com.backend.gapfinder.dto.responses.GapCoverageResponseDTO;
import com.backend.gapfinder.repositories.GapRepository;
import com.backend.gapfinder.repositories.UserLocationLogRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class AnalyticsService {

    private final GapRepository gapRepository;
    private final UserLocationLogRepository locationLogRepository;

    public AnalyticsService(GapRepository gapRepository, UserLocationLogRepository locationLogRepository) {
        this.gapRepository = gapRepository;
        this.locationLogRepository = locationLogRepository;
    }

    // ==================== BQ 5 ====================
    // Miguel Cabrera: Which gap durations have the lowest match coverage?

    // Retrieves match coverage metrics grouped by gap duration buckets sorted by lowest coverage
    @Transactional(readOnly = true)
    public List<GapCoverageResponseDTO> getMatchCoverageByGapDuration() {
        log.info("Inicia proceso de consultar la cobertura de matches por duración de gap");

        List<GapCoverageResponseDTO> result = gapRepository.findCoverageByDurationBucket().stream()
                .map(row -> {
                    double totalGapMinutes = ((Number) row[2]).doubleValue();
                    double totalMatchedMinutes = ((Number) row[3]).doubleValue();

                    GapCoverageResponseDTO dto = new GapCoverageResponseDTO();
                    dto.setDurationRange((String) row[0]);
                    dto.setGapCount(((Number) row[1]).longValue());
                    dto.setTotalGapMinutes(totalGapMinutes);
                    dto.setTotalMatchedMinutes(totalMatchedMinutes);
                    dto.setCoveragePercent(
                            calculateCoveragePercent(totalMatchedMinutes, totalGapMinutes)
                    );

                    return dto;
                })
                .sorted(Comparator.comparingDouble(GapCoverageResponseDTO::getCoveragePercent))
                .toList();

        log.info("Termina proceso de consultar la cobertura de matches por duración de gap");
        return result;
    }

    // Calculates the coverage percentage based on matched minutes and total gap minutes
    private double calculateCoveragePercent(double matchedMinutes, double gapMinutes) {
        if (gapMinutes <= 0) {
            return 0.0;
        }

        return matchedMinutes / gapMinutes * 100;
    }

    // ================== END BQ 5 ==================

    // ==================== BQ 11 ====================
    // Joel: Where do the most students have free time on campus?

    // Retrieves student presence and free time metrics aggregated by campus building
    @Transactional(readOnly = true)
    public List<BuildingGapPresenceResponseDTO> getBuildingsByGapPresence() {
        log.info("Inicia proceso de consultar la presencia de estudiantes en gaps por edificio");

        List<BuildingGapPresenceResponseDTO> result = locationLogRepository.findGapPresenceByBuilding().stream()
                .map(p -> new BuildingGapPresenceResponseDTO(
                        p.getBuildingId(),
                        p.getBuildingName(),
                        p.getStudentCount(),
                        p.getGapCount(),
                        p.getTotalGapMinutes()))
                .toList();

        log.info("Termina proceso de consultar la presencia de estudiantes en gaps por edificio");
        return result;
    }

    // ================== END BQ 11 ==================
}