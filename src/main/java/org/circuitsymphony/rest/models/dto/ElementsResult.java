package org.circuitsymphony.rest.models.dto;

import java.util.List;

/**
 * Result of API method used to get currently loaded element properties
 */
public class ElementsResult {
    private final String key;
    private final List<Object> elements;

    public ElementsResult(String key, List<Object> elements) {
        this.key = key;
        this.elements = elements;
    }

    public String getKey() {
        return key;
    }

    public List<Object> getElements() {
        return elements;
    }
}
