package com.backend.gapfinder.repositories.projections;

public interface CareerUnmatchedTimeProjection {
    String getCareer();
    Integer getSemester();
    Long getStudents();            // Students of the career and semester with gaps in the period
    Double getFreeMinutes();       // Total free minutes of the group
    Double getUnmatchedMinutes();  // Free minutes not covered by an accepted/completed match
}
