package org.circuitsymphony.engine;

/**
 * Possible states of loading circuit file.
 *
 */
public enum CircuitLoadResult {
    /**
     * File loaded successfully
     */
    OK,
    /**
     * File was loaded however they were some unrecognized elements
     */
    DUMP_WARNING,
    /**
     * Loading file resulted in exception or severe error
     */
    ERROR
}
