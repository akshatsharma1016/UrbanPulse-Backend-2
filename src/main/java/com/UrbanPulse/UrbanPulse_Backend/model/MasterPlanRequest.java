package com.UrbanPulse.UrbanPulse_Backend.model;

import lombok.Data;
import java.util.List;

@Data
public class MasterPlanRequest {
    /** Target city, e.g. "Mumbai". */
    private String city;
    /** Budget tier: "Low", "Medium", or "High". */
    private String budget;
    /** User-defined name for this plan. */
    private String planName;
    /** Ordered list of policy phases that make up the roadmap. */
    private List<PhaseInput> phases;
}
