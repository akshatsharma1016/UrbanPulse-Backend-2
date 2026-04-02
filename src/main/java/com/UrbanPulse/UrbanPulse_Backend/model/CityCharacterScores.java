package com.UrbanPulse.UrbanPulse_Backend.model;

public class CityCharacterScores {
    private int infrastructure;
    private int governance;
    private int resilience;
    private int liveability;

    public CityCharacterScores() {}

    public CityCharacterScores(int infrastructure, int governance, int resilience, int liveability) {
        this.infrastructure = infrastructure;
        this.governance = governance;
        this.resilience = resilience;
        this.liveability = liveability;
    }

    public int getInfrastructure() { return infrastructure; }
    public void setInfrastructure(int infrastructure) { this.infrastructure = infrastructure; }

    public int getGovernance() { return governance; }
    public void setGovernance(int governance) { this.governance = governance; }

    public int getResilience() { return resilience; }
    public void setResilience(int resilience) { this.resilience = resilience; }

    public int getLiveability() { return liveability; }
    public void setLiveability(int liveability) { this.liveability = liveability; }
}
