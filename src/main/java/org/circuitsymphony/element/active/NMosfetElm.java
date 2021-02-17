package org.circuitsymphony.element.active;

import org.circuitsymphony.engine.CircuitEngine;

public class NMosfetElm extends MosfetElm {
    public NMosfetElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, false);
    }

    @Override
    public Class getDumpClass() {
        return MosfetElm.class;
    }
}
