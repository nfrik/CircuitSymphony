package org.circuitsymphony.manager;

/**
 * Allows to synchronously modify state of simulation.
 */
public interface SimStateModifier {
    void modifyState(CircuitManager.PropertiesController controller);
}
