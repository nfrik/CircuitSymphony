package org.circuitsymphony.element.active;

import org.circuitsymphony.engine.CircuitEngine;

public class CC2NegElm extends CC2Elm {
    public CC2NegElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, -1);
    }

    @Override
    public Class getDumpClass() {
        return CC2Elm.class;
    }
}
