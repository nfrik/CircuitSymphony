package org.circuitsymphony.engine;

import org.circuitsymphony.element.CircuitElm;

/**
 * Represents single element type that can be saved in circuit file. Also used to build right click menu for adding
 * new elements in GUI mode.

 */
public class MappedElement {
    public final MappedElementCategory category;
    public final String menuString;
    public final Class<? extends CircuitElm> clazz;

    MappedElement(MappedElementCategory category, String menuString, Class<? extends CircuitElm> clazz) {
        this.category = category;
        this.menuString = menuString;
        this.clazz = clazz;
    }
}

