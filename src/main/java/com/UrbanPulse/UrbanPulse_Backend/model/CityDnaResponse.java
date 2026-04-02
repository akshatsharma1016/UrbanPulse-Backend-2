package com.UrbanPulse.UrbanPulse_Backend.model;

import java.time.Instant;
import java.util.List;

public class CityDnaResponse {
    private String city;
    private String densityCategory;
    private String economicProfile;
    private String geographicRisk;
    private int existingTransitQuality;
    private int greenSpaceScore;
    private int housingPressure;
    private List<String> politicalResistance;
    private List<String> popularPolicies;
    private List<String> quickWins;
    private List<String> warningFlags;
    private CityCharacterScores characterScores;
    private String cityVerdict;
    private String generatedAt = Instant.now().toString();

    public CityDnaResponse() {}

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDensityCategory() { return densityCategory; }
    public void setDensityCategory(String densityCategory) { this.densityCategory = densityCategory; }

    public String getEconomicProfile() { return economicProfile; }
    public void setEconomicProfile(String economicProfile) { this.economicProfile = economicProfile; }

    public String getGeographicRisk() { return geographicRisk; }
    public void setGeographicRisk(String geographicRisk) { this.geographicRisk = geographicRisk; }

    public int getExistingTransitQuality() { return existingTransitQuality; }
    public void setExistingTransitQuality(int existingTransitQuality) { this.existingTransitQuality = existingTransitQuality; }

    public int getGreenSpaceScore() { return greenSpaceScore; }
    public void setGreenSpaceScore(int greenSpaceScore) { this.greenSpaceScore = greenSpaceScore; }

    public int getHousingPressure() { return housingPressure; }
    public void setHousingPressure(int housingPressure) { this.housingPressure = housingPressure; }

    public List<String> getPoliticalResistance() { return politicalResistance; }
    public void setPoliticalResistance(List<String> politicalResistance) { this.politicalResistance = politicalResistance; }

    public List<String> getPopularPolicies() { return popularPolicies; }
    public void setPopularPolicies(List<String> popularPolicies) { this.popularPolicies = popularPolicies; }

    public List<String> getQuickWins() { return quickWins; }
    public void setQuickWins(List<String> quickWins) { this.quickWins = quickWins; }

    public List<String> getWarningFlags() { return warningFlags; }
    public void setWarningFlags(List<String> warningFlags) { this.warningFlags = warningFlags; }

    public CityCharacterScores getCharacterScores() { return characterScores; }
    public void setCharacterScores(CityCharacterScores characterScores) { this.characterScores = characterScores; }

    public String getCityVerdict() { return cityVerdict; }
    public void setCityVerdict(String cityVerdict) { this.cityVerdict = cityVerdict; }

    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
}
