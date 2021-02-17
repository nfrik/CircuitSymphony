package org.circuitsymphony.engine;

import org.circuitsymphony.element.CircuitElm;

import java.awt.*;

/**
 * Used to get events from {@link CircuitEngine}.
 */
public interface CircuitEngineListener {
    /**
     * Called when simulation was stopped.
     *
     * @param cause reason why simulation was stopped
     * @param ce    element related to stop reason
     */
    void stop(String cause, CircuitElm ce) throws Exception;

    /**
     * Called when circuit needs reanalyzing. Usually next analysis should be performed
     * before calling {@link CircuitEngine#runCircuit()}
     */
    void setAnalyzeFlag();

    /**
     * Called when scope should be updated, generally only used by GUI.
     */
    default void updateScopes() {

    }

    /**
     * Called when currently dragged element should be returned, generally only used by GUI.
     */
    default CircuitElm getDraggedElement() {
        return null;
    }

    /**
     * Called when engine needs duration of last frame to determinate whether to continue current circuit update,
     * generally only used by GUI when engine is not in fixed iteration mode.
     */
    default long getLastFrameTime() {
        return 0;
    }

    /**
     * Called to check if this {@link CircuitEngine} manager supports creation of UI elements.
     * If this returns false then no UI elements should be created to prevent AWT {@link HeadlessException}.
     */
    default boolean isUISupported() {
        return false;
    }

    /**
     * Called when {@link CircuitElm} requested to create UI element for controlling it's properties.
     *
     * @param comp that should be added to some GUI container
     */
    default void createUI(Component comp) {

    }

    /**
     * Called when {@link CircuitElm} requested to remove previously created UI element.
     *
     * @param comp that should be removed from some GUI container
     */
    default void removeUI(Component comp) {

    }
}
