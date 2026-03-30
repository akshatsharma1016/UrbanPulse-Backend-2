package com.UrbanPulse.UrbanPulse_Backend.model;

import lombok.Data;
import java.util.List;

@Data
public class MasterPlanResponse {
    /** Unique identifier for this plan run, e.g. "PLAN-AB3XY". */
    private String planId;
    private String city;
    private String planName;
    /** One PhaseResult per input phase, in order. */
    private List<PhaseResult> phases;
    /** Sparse year series for the city trajectory chart (baseline + one point per phase). */
    private List<TrajectoryPoint> trajectory;
    /** Multi-sentence AI analysis of the compounding effect across all phases. */
    private String compoundInsight;
    /** City health score (0–100) before any policies — average of 4 baseline metrics. */
    private int cityHealthBefore;
    /** City health score (0–100) after all policies — average of 4 final metrics. */
    private int cityHealthAfter;
}
