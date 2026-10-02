package com.backend.gapfinder.repositories;

import com.backend.gapfinder.enums.MatchStatusEnum;
import com.backend.gapfinder.models.MatchModel;
import com.backend.gapfinder.repositories.projections.ConnectionCompletionProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<MatchModel, Long> {

    // Get all matches with a given status (used to auto-complete expired ones)
    List<MatchModel> findByStatus(MatchStatusEnum status);

    // Get the matches with a given status whose acceptor gap belongs to the given user
    List<MatchModel> findByAcceptorGapUserIdAndStatus(Long userId, MatchStatusEnum status);

    // Check if a gap is used by any match, as proposer or acceptor
    boolean existsByProposerGapIdOrAcceptorGapId(Long proposerGapId, Long acceptorGapId);

    // Counts all matches without date filters; only the stored COMPLETED status counts as completed.
    // Connections that have not ended remain in the denominator, as required by BQ 7.
    @Query(value = """
        SELECT COUNT(*) AS "totalConnections",
               COUNT(*) FILTER (WHERE status = 'COMPLETED') AS "completedConnections"
        FROM matches
        """, nativeQuery = true)
    ConnectionCompletionProjection findConnectionCompletionStats();
}