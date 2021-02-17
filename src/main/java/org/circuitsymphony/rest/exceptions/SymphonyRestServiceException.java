package org.circuitsymphony.rest.exceptions;

public class SymphonyRestServiceException extends RuntimeException {
    public SymphonyRestServiceException(String message) {
        super(message);
    }

    public SymphonyRestServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
