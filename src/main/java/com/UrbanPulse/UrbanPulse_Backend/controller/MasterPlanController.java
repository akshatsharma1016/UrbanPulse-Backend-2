package com.UrbanPulse.UrbanPulse_Backend.controller;

import com.UrbanPulse.UrbanPulse_Backend.model.MasterPlanRequest;
import com.UrbanPulse.UrbanPulse_Backend.model.MasterPlanResponse;
import com.UrbanPulse.UrbanPulse_Backend.service.MasterPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/masterplan")
public class MasterPlanController {

    private final MasterPlanService service;

    @Autowired
    public MasterPlanController(MasterPlanService service) {
        this.service = service;
    }

    /**
     * POST /api/masterplan/simulate
     *
     * Accepts a city + ordered list of policy phases.
     * Returns enriched phase results, trajectory chart data, compound AI insight,
     * and before/after city health scores.
     */
    @PostMapping("/simulate")
    public ResponseEntity<MasterPlanResponse> simulate(@RequestBody MasterPlanRequest request) {
        System.out.println("[MasterPlanController] Received plan: " + request.getPlanName()
                + " | City: " + request.getCity()
                + " | Phases: " + request.getPhases().size());
        MasterPlanResponse response = service.simulate(request);
        return ResponseEntity.ok(response);
    }
}
