package org.circuitsymphony.rest.models.dto;

/**
 * Result of endpoint for getting simulation time
 */
public class TimeResult {
    private final String key;
    private double time;

    public TimeResult(String key, double time) {
        this.key = key;
        this.time = time;
    }

    public String getKey() {
        return key;
    }

    public double getTime() {
        return time;
    }
}
