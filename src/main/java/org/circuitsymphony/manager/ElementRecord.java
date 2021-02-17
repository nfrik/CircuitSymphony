package org.circuitsymphony.manager;

/**
 * Contains measured current and voltage diff for single element.
 */
public class ElementRecord {
    private final double current;
    private final double voltageDiff;

    public ElementRecord(double current, double voltageDiff) {
        this.current = current;
        this.voltageDiff = voltageDiff;
    }

    public double getCurrent() {
        return current;
    }

    public double getVoltageDiff() {
        return voltageDiff;
    }
}
