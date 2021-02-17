package org.circuitsymphony.element.io;

import org.circuitsymphony.engine.CircuitEngine;

public class ACRailElm extends RailElm {
    public ACRailElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, WF_AC);
    }

    @Override
    public Class getDumpClass() {
        return RailElm.class;
    }
}
