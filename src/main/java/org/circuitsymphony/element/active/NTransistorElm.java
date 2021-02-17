package org.circuitsymphony.element.active;

import org.circuitsymphony.engine.CircuitEngine;

public class NTransistorElm extends TransistorElm {
    public NTransistorElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, false);
    }

    @Override
    public Class getDumpClass() {
        return TransistorElm.class;
    }
}
