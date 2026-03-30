package com.UrbanPulse.UrbanPulse_Backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metrics {
    private int traffic;
    private int economy;
    private int ecology;
    private int sentiment;

    /** Average of all four metrics — used as the city health score (0–100). */
    public int healthScore() {
        return (traffic + economy + ecology + sentiment) / 4;
    }
}
