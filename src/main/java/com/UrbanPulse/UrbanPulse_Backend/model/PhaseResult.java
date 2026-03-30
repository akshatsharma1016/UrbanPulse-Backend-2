package com.UrbanPulse.UrbanPulse_Backend.model;

import lombok.Data;

@Data
public class PhaseResult {
    private String year;
    private String policy;
    private String label;
    private String icon;
    /** Hex color assigned to this policy type for charts/UI. */
    private String color;
    /** City state just before this policy takes effect. */
    private Metrics before;
    /** City state after this policy has run for its allotted period. */
    private Metrics after;
    /** One-sentence AI-generated insight about this phase's impact. */
    private String insight;
}
