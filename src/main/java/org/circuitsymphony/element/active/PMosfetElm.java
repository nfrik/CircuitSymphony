package org.circuitsymphony.element.active;

import org.circuitsymphony.engine.CircuitEngine;

public class PMosfetElm extends MosfetElm {
    public PMosfetElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, true);
    }

    @Override
    public Class getDumpClass() {
        return MosfetElm.class;
    }
}
