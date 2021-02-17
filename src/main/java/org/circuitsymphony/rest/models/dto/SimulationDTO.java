package org.circuitsymphony.rest.models.dto;

import java.io.Serializable;

/**
 * Result of endpoint for creating simulations
 */
public class SimulationDTO implements Serializable {
    private final String key;

    public SimulationDTO(String key) {
        this.key = key;
    }

    private SimulationDTO() {
        key = null;
    }

    public String getKey() {
        return key;
    }
}
