package com.UrbanPulse.UrbanPulse_Backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("UrbanPulse API Backend is running and healthy!");
    }
}
