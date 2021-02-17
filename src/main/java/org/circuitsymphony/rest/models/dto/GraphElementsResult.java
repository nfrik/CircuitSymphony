package org.circuitsymphony.rest.models.dto;

import java.util.HashMap;

/**
 * Result of API method used to get currently loaded elements IDs.
 */
public class GraphElementsResult {
    private final String key;
    private final HashMap<Integer, Object> elements;

    public GraphElementsResult(String key, HashMap<Integer, Object> elements) {
        this.key = key;
        this.elements = elements;
    }

    public String getKey() {
        return key;
    }

    public HashMap<Integer, Object> getElements() {
        return elements;
    }
}
