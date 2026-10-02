package com.backend.gapfinder.repositories.projections;

// Counts all stored connections and those explicitly marked COMPLETED, without date filters.
public interface ConnectionCompletionProjection {
    // Total stored connections, including those that have not ended.
    Long getTotalConnections();

    // Connections whose stored status is COMPLETED; ACCEPTED is not included.
    Long getCompletedConnections();
}
