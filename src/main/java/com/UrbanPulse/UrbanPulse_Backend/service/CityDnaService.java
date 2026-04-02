package com.UrbanPulse.UrbanPulse_Backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.UrbanPulse.UrbanPulse_Backend.model.CityDnaResponse;
import com.UrbanPulse.UrbanPulse_Backend.model.CityCharacterScores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CityDnaService {

    private final GeminiService gemini;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentHashMap<String, CityDnaResponse> cache = new ConcurrentHashMap<>();

    @Autowired
    public CityDnaService(GeminiService gemini) {
        this.gemini = gemini;
    }

    public CityDnaResponse getCityDna(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            cityName = "Unknown City";
        }
        String normalizedCity = cityName.toLowerCase().trim();

        if (cache.containsKey(normalizedCity)) {
            return cache.get(normalizedCity);
        }

        String prompt = "You are an urban planning expert with deep knowledge of global cities.\n" +
                "Generate a structured urban DNA profile for: " + cityName + "\n\n" +
                "Return ONLY valid JSON matching this exact structure, no markdown, no explanation:\n" +
                "{\n" +
                "  \"densityCategory\": \"one of [sparse, moderate, dense, extreme]\",\n" +
                "  \"economicProfile\": \"one phrase describing the economic character\",\n" +
                "  \"geographicRisk\": \"the primary environmental or geographic risk\",\n" +
                "  \"existingTransitQuality\": integer 0-100,\n" +
                "  \"greenSpaceScore\": integer 0-100,\n" +
                "  \"housingPressure\": integer 0-100,\n" +
                "  \"politicalResistance\": [\"array of 1-3 policy keys from [add_metro, add_park, remove_parking, increase_tax, build_highway, subsidize_ev]\"],\n" +
                "  \"popularPolicies\": [\"array of 1-3 policy keys most likely to have public support\"],\n" +
                "  \"quickWins\": [\"array of 1-2 policy keys with highest ROI for this city\"],\n" +
                "  \"warningFlags\": [\"array of 0-2 policy keys that could backfire\"],\n" +
                "  \"characterScores\": { \"infrastructure\": 0-100, \"governance\": 0-100, \"resilience\": 0-100, \"liveability\": 0-100 },\n" +
                "  \"cityVerdict\": \"one sentence capturing this city's core urban identity and challenge\"\n" +
                "}\n" +
                "If the city is fictional or unknown, generate a plausible profile based on the name.";

        String json = gemini.call(prompt);
        if (json != null) {
            try {
                CityDnaResponse response = mapper.readValue(json, CityDnaResponse.class);
                response.setCity(cityName);
                cache.put(normalizedCity, response);
                return response;
            } catch (Exception e) {
                System.err.println("[CityDnaService] Failed to parse DNA for " + cityName + ": " + e.getMessage());
            }
        }

        return createFallback(cityName);
    }

    public List<CityDnaResponse> getAllProfiles() {
        return new java.util.ArrayList<>(cache.values());
    }

    private CityDnaResponse createFallback(String city) {
        CityDnaResponse fallback = new CityDnaResponse();
        fallback.setCity(city);
        fallback.setDensityCategory("moderate");
        fallback.setEconomicProfile("developing multi-sector");
        fallback.setGeographicRisk("standard urban vulnerability");
        fallback.setExistingTransitQuality(50);
        fallback.setGreenSpaceScore(50);
        fallback.setHousingPressure(50);
        fallback.setPoliticalResistance(List.of("increase_tax"));
        fallback.setPopularPolicies(List.of("add_park"));
        fallback.setQuickWins(List.of("add_metro"));
        fallback.setWarningFlags(List.of("remove_parking"));
        fallback.setCharacterScores(new CityCharacterScores(50, 50, 50, 50));
        fallback.setCityVerdict("A growing city facing standard urban challenges where balanced policy implementation is required.");
        return fallback;
    }
}
