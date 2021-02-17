package org.circuitsymphony.manager;

/**
 * Contains element properties for single element
 */
public class ElementProperties {
    private final int elementId;
    private final String type;
    private final double current;
    private final double voltageDiff;

    public ElementProperties(int elementId, String type, double current, double voltageDiff) {
        this.elementId = elementId;
        this.type = type;
        this.current = current;
        this.voltageDiff = voltageDiff;
    }

    public int getElementId() {
        return elementId;
    }

    public String getType() {
        return type;
    }

    public double getCurrent() {
        return current;
    }

    public double getVoltageDiff() {
        return voltageDiff;
    }
}
