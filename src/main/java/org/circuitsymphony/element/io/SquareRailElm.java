package org.circuitsymphony.element.io;

import org.circuitsymphony.engine.CircuitEngine;

public class SquareRailElm extends RailElm {
    public SquareRailElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, WF_SQUARE);
    }

    @Override
    public Class getDumpClass() {
        return RailElm.class;
    }
}
