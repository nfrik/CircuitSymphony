package org.circuitsymphony.rest.models.dto;

import java.util.List;

/**
 * Result of API method for listing all available element properties
 */
public class ElementPropertiesResult {
    private final int elementId;
    private final List<String> properties;

    public ElementPropertiesResult(int elementId, List<String> properties) {
        this.elementId = elementId;
        this.properties = properties;
    }

    public int getElementId() {
        return elementId;
    }

    public List<String> getProperties() {
        return properties;
    }
}
