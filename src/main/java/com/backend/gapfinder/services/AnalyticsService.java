package com.backend.gapfinder.services;

import com.backend.gapfinder.dto.OpenTableAbandonmentStatsBasicDTO;
import com.backend.gapfinder.dto.responses.BuildingGapPresenceResponseDTO;
import com.backend.gapfinder.dto.responses.GapCoverageResponseDTO;
import com.backend.gapfinder.dto.responses.ConnectionCompletionResponseDTO;
import com.backend.gapfinder.dto.responses.ConnectionCompletionStatsResponseDTO;
import com.backend.gapfinder.enums.ConnectionCompletionResultEnum;
import com.backend.gapfinder.repositories.MatchRepository;
import com.backend.gapfinder.repositories.OpenTableRepository;
import com.backend.gapfinder.repositories.projections.ConnectionCompletionProjection;
import com.backend.gapfinder.exceptions.NotFoundException;
import com.backend.gapfinder.repositories.GapRepository;
import com.backend.gapfinder.repositories.UserLocationLogRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class AnalyticsService {

    private final GapRepository gapRepository;
    private final UserLocationLogRepository locationLogRepository;
    private final OpenTableAbandonmentService abandonmentService;
    private final MatchRepository matchRepository;
    private final OpenTableRepository openTableRepository;

    public AnalyticsService(GapRepository gapRepository,
                            MatchRepository matchRepository,
                            OpenTableRepository openTableRepository,
                            UserLocationLogRepository locationLogRepository,
                            OpenTableAbandonmentService abandonmentService) {
        this.gapRepository = gapRepository;
        this.locationLogRepository = locationLogRepository;
        this.abandonmentService = abandonmentService;
        this.matchRepository = matchRepository;
        this.openTableRepository = openTableRepository;
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

    // ==================== BQ 3 TYPE 2 GRUPAL ====================
    // At which step do students most often leave the gap-publishing process?

    // Retrieves the creation step with the highest abandonment rate since the given date
    @Transactional(readOnly = true)
    public OpenTableAbandonmentStatsBasicDTO getMostAbandonedStep(LocalDateTime since) {
        log.info("Inicia proceso de consultar el paso más abandonado desde {}", since);

        OpenTableAbandonmentStatsBasicDTO result = abandonmentService.getMostAbandonedStep(since)
                .orElseThrow(() -> new NotFoundException("No hay abandonos registrados desde la fecha indicada"));

        log.info("Termina proceso de consultar el paso más abandonado desde {}", since);
        return result;
    }

    // ================== END BQ 3 TYPE 2 GRUPAL ==================

    // ==================== BQ 7 ====================
    // Which connection method has the higher completion rate: open tables or matches?

    // Compares stored COMPLETED counts over all records, without inferring completion or filtering dates.
    @Transactional(readOnly = true)
    public ConnectionCompletionResponseDTO getConnectionCompletion() {
        ConnectionCompletionStatsResponseDTO matches = toConnectionCompletionStats(
                matchRepository.findConnectionCompletionStats());
        ConnectionCompletionStatsResponseDTO openTables = toConnectionCompletionStats(
                openTableRepository.findConnectionCompletionStats());

        ConnectionCompletionResultEnum result;
        if (matches.getTotalConnections() == 0 || openTables.getTotalConnections() == 0) {
            result = ConnectionCompletionResultEnum.INSUFFICIENT_DATA;
        } else {
            int comparison = Double.compare(matches.getCompletionRatePercent(), openTables.getCompletionRatePercent());
            result = comparison == 0 ? ConnectionCompletionResultEnum.TIE
                    : comparison > 0 ? ConnectionCompletionResultEnum.MATCH : ConnectionCompletionResultEnum.OPEN_TABLE;
        }

        ConnectionCompletionResponseDTO response = new ConnectionCompletionResponseDTO();
        response.setMatches(matches);
        response.setOpenTables(openTables);
        response.setResult(result);
        return response;
    }

    // Uses zero for an empty denominator and compares percentages after rounding to two decimals.
    private ConnectionCompletionStatsResponseDTO toConnectionCompletionStats(ConnectionCompletionProjection row) {
        ConnectionCompletionStatsResponseDTO stats = new ConnectionCompletionStatsResponseDTO();
        stats.setTotalConnections(row.getTotalConnections());
        stats.setCompletedConnections(row.getCompletedConnections());
        double rate = row.getTotalConnections() == 0 ? 0.0
                : row.getCompletedConnections().doubleValue() / row.getTotalConnections() * 100.0;
        stats.setCompletionRatePercent(Math.round(rate * 100.0) / 100.0);
        return stats;
    }

    // ================== END BQ 7 ==================
}