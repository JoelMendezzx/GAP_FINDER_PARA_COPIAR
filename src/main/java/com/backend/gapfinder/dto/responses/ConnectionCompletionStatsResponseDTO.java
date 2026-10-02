package com.backend.gapfinder.dto.responses;

import lombok.Data;

// Completion metrics for one connection method across all stored records.
@Data
public class ConnectionCompletionStatsResponseDTO {
    // All stored connections, regardless of dates or status.
    private Long totalConnections;

    // Connections explicitly marked COMPLETED.
    private Long completedConnections;

    // Completed / total * 100, rounded to two decimals; zero when total is zero.
    private Double completionRatePercent;
}
