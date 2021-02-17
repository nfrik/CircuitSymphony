package org.circuitsymphony.rest.models.dto;

/**
 * Result of API methods used for manipulaing element properties
 */
public class ElementPropertyResult {
    private final int elementId;
    private final String propertyKey;
    private final Object propertyValue;

    public ElementPropertyResult(int elementId, String propertyKey, Object propertyValue) {
        this.elementId = elementId;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
    }

    public int getElementId() {
        return elementId;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }
}
