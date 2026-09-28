package com.backend.gapfinder.dto;

import lombok.Data;

@Data
public class MatchSearchCriteriaDTO {

    // ID of the target gap for which we want candidates
    private Long gapId;

    // Optional flags to toggle bonus strategies
    private boolean includeSameCareer;
    private boolean includeSharedInterests;
    private boolean includeEffortType;
}