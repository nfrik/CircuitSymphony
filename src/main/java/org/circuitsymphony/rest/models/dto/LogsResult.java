package org.circuitsymphony.rest.models.dto;

import org.circuitsymphony.manager.Measurement;

import java.util.ArrayList;


public class LogsResult {
    private final String key;
    private final ArrayList<String> logs;

    public LogsResult(String key, ArrayList<String> logs) {
        this.key = key;
        this.logs = logs;
    }


    public String getKey() {
        return key;
    }

    public ArrayList<String> getMeasurements() {
        return logs;
    }
}
