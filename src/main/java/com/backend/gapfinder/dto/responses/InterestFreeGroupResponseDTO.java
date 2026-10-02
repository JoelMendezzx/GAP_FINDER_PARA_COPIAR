package com.backend.gapfinder.dto.responses;

import com.backend.gapfinder.enums.DayOfWeekEnum;
import lombok.Data;

// One row per interest with its largest group of students free at the same time (BQ 13)
@Data
public class InterestFreeGroupResponseDTO {
    private Long interestId;
    private String interestName;
    private Long largestGroupSize;
    private DayOfWeekEnum dayOfWeek;   // Uses the existing enum (MON ... SAT)
    private String slotStart;          // For example "14:30"
}
