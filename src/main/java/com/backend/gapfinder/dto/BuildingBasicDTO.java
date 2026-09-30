package com.backend.gapfinder.dto;

import lombok.Data;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class BuildingBasicDTO {

    // Unique building identifier
    private Long id;

    // Name of the campus building
    private String name;

    // Radius in meters defining the coverage area around the building
    private double radiusMeters;
}