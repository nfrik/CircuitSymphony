package org.circuitsymphony.manager;

import java.util.HashMap;

/**
 * Contains measurements of all tracked elements performed in single point of time.
 */
public class Measurement {
    private final double time;
    private final HashMap<Integer, ElementRecord> records;

    public Measurement(double time, HashMap<Integer, ElementRecord> records) {
        this.time = time;
        this.records = records;
    }

    public double getTime() {
        return time;
    }

    public HashMap<Integer, ElementRecord> getRecords() {
        return records;
    }
}
