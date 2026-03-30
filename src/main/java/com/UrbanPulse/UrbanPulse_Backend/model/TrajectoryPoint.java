package com.UrbanPulse.UrbanPulse_Backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrajectoryPoint {
    /** Year label, e.g. "2024". */
    private String year;
    private int traffic;
    private int economy;
    private int ecology;
    private int sentiment;
}
