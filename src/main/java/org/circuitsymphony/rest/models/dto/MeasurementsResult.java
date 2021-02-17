package org.circuitsymphony.rest.models.dto;

import org.circuitsymphony.manager.Measurement;

import java.util.ArrayList;


public class MeasurementsResult {
    private final String key;
    private final ArrayList<Measurement> measurements;

    public MeasurementsResult(String key, ArrayList<Measurement> measurements) {
        this.key = key;
        this.measurements = measurements;
    }


    public String getKey() {
        return key;
    }

    public ArrayList<Measurement> getMeasurements() {
        return measurements;
    }
}
