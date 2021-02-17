package org.circuitsymphony.rest.models.dto;

/**
 * Response object for API methods exposing circuit voltage difference or current.
 */
public class ElementResult {
    private final String key;
    private int elementId;
    private double value;

    public ElementResult(String key, int elementId, double value) {
        this.key = key;
        this.elementId = elementId;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public int getElementId() {
        return elementId;
    }

    public double getValue() {
        return value;
    }
}
