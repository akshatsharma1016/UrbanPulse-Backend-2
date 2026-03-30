package com.UrbanPulse.UrbanPulse_Backend.model;

import lombok.Data;

@Data
public class PhaseInput {
    /** Calendar year when this policy is deployed, e.g. "2025". */
    private String year;
    /** Internal policy key, e.g. "add_metro". */
    private String policy;
    /** Human-readable label shown in the UI, e.g. "Metro Line". */
    private String label;
    /** Emoji icon shown in the UI, e.g. "🚇". */
    private String icon;
}
