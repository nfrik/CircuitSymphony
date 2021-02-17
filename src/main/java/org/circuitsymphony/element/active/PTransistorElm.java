package org.circuitsymphony.element.active;

import org.circuitsymphony.engine.CircuitEngine;

public class PTransistorElm extends TransistorElm {
    public PTransistorElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, true);
    }

    @Override
    public Class getDumpClass() {
        return TransistorElm.class;
    }
}
