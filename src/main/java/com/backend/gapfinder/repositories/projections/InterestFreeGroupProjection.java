package com.backend.gapfinder.repositories.projections;

public interface InterestFreeGroupProjection {
    Long getInterestId();
    String getInterestName();
    Long getLargestGroup();     // Students free at the same time with that interest
    Integer getDayOfWeek();     // 1 = Monday ... 6 = Saturday
    String getSlotStart();      // "14:30"
}
