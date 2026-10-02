package com.backend.gapfinder.dto.responses;

import com.backend.gapfinder.enums.ConnectionCompletionResultEnum;
import lombok.Data;

// Compares matches and open tables using stored statuses without time or participant checks (BQ 7).
@Data
public class ConnectionCompletionResponseDTO {
    // Completion metrics for all stored matches.
    private ConnectionCompletionStatsResponseDTO matches;

    // Completion metrics for all stored open tables.
    private ConnectionCompletionStatsResponseDTO openTables;

    // Higher rounded rate, tie, or insufficient data when either method has no records.
    private ConnectionCompletionResultEnum result;
}
