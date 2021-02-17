package org.circuitsymphony.engine;

import org.circuitsymphony.util.Scope;

import java.util.EnumSet;
import java.util.StringTokenizer;

/**
 * Used to get events from {@link CircuitLoader}
 *
 */
public interface CircuitLoaderListener {
    /**
     * Called when hints part of file was read by circuit loader, generally only used by GUI.
     */
    default void configureHints(int hintType, int hintItem1, int hintItem2) {

    }

    /**
     * Called when circuit option part of file was read by circuit loader.
     */
    void configureOptions(CircuitOptions options);

    /**
     * Called when scope data should be read from file. By default this skip scope data and continue parsing file normally.
     */
    default void handleScope(StringTokenizer tokenizer) {
        new Scope(null).undump(tokenizer, false);
    }

    /**
     * Called after loading file has been finished.
     *
     * @param retain elements that should be retained.
     */
    void afterLoading(EnumSet<CircuitLoader.RetentionPolicy> retain);
}
