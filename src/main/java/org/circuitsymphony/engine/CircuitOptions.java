package org.circuitsymphony.engine;

import java.util.StringTokenizer;

/**
 * All possible circuit options stored in circuit file.
 */
public class CircuitOptions {
    public final boolean defaults;
    public final boolean dotsCheck;
    public final boolean smallGridCheck;
    public final boolean voltsCheck;
    public final boolean powerCheck;
    public final boolean showValuesCheck;
    public final double timeStep;
    public final int speedBarValue;
    public final int currentBarValue;
    public final double volateRange;
    public final int powerBarValue;

    /**
     * Creates new circuit options with default values
     */
    public CircuitOptions() {
        defaults = true;
        dotsCheck = true;
        smallGridCheck = false;
        voltsCheck = true;
        powerCheck = false;
        showValuesCheck = true;
        timeStep = 5e-6;
        speedBarValue = 117;
        currentBarValue = 50;
        volateRange = 5;
        powerBarValue = 50;
    }

    /**
     * Creates new circuit options reading values from {@link StringTokenizer}
     */
    public CircuitOptions(StringTokenizer tokenizer) {
        defaults = false;
        int flags = new Integer(tokenizer.nextToken());
        dotsCheck = (flags & 1) != 0;
        smallGridCheck = (flags & 2) != 0;
        voltsCheck = (flags & 4) == 0;
        powerCheck = (flags & 8) == 8;
        showValuesCheck = (flags & 16) == 0;
        timeStep = new Double(tokenizer.nextToken());
        double sp = new Double(tokenizer.nextToken());
        speedBarValue = (int) (Math.log(10 * sp) * 24 + 61.5);
        currentBarValue = new Integer(tokenizer.nextToken());
        volateRange = new Double(tokenizer.nextToken());
        int powerBarValue = 50;
        try {
            powerBarValue = new Integer(tokenizer.nextToken());
        } catch (Exception ignored) {
        }
        this.powerBarValue = powerBarValue;
    }
}
