package org.circuitsymphony.rest.models.dto;

import java.util.HashMap;
import java.util.List;

/**
 * Result of API method used to get currently loaded graph circuit as JSON string.
 */
public class GraphResult {
    private final String key;
    private final HashMap<Integer, List<Object>> graph;

    public GraphResult(String key, HashMap<Integer, List<Object>> graph) {
        this.key = key;
        this.graph = graph;
    }

    public String getKey() {
        return key;
    }

    public HashMap<Integer, List<Object>> getGraph() {
        return graph;
    }
}
