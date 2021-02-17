package org.circuitsymphony.rest.models.dto;

import java.io.Serializable;

/**
 * General response for most API endpoints
 */
public class ResponseModel implements Serializable {
    private final String message;

    public ResponseModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
