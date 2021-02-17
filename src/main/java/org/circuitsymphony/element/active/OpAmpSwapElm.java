package org.circuitsymphony.element.active;

import org.circuitsymphony.engine.CircuitEngine;

public class OpAmpSwapElm extends OpAmpElm {
    public OpAmpSwapElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        flags |= FLAG_SWAP;
    }

    @Override
    public Class getDumpClass() {
        return OpAmpElm.class;
    }
}
