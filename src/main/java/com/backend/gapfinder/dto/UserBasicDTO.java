package com.backend.gapfinder.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserBasicDTO {

    // Unique user identifier
    private Long id;

    // Full name of the student
    private String name;

    // Contact phone number
    private String phoneNumber;

    // Academic program or major
    private String career;

    // Last time the current location was updated
    private LocalDateTime locationUpdatedAt;
}