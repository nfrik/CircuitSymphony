package org.circuitsymphony.rest.models.dto;

import java.time.LocalDateTime;

/**
 * Represents status of single simulation instance.
 */
public class SimulationState {
    private final String key;
    private final LocalDateTime dataSubmitted;
    private final String status;

    public SimulationState(String key, LocalDateTime dataSubmitted, boolean running) {
        this.key = key;
        this.dataSubmitted = dataSubmitted;
        this.status = running ? "running" : "paused";
    }

    public String getKey() {
        return key;
    }

    public LocalDateTime getDataSubmitted() {
        return dataSubmitted;
    }

    public String getStatus() {
        return status;
    }
}
