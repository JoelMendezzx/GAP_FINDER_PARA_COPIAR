package com.backend.gapfinder.dto.responses;

import lombok.Data;

// One row per career and semester (BQ 12)
@Data
public class CareerUnmatchedRateResponseDTO {
    private String career;
    private Integer semester;
    private Long students;
    private Double freeMinutes;
    private Double unmatchedMinutes;
    private Double unmatchedRatePercent;   // Unmatched minutes / free minutes * 100
}
