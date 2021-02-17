package org.circuitsymphony.element.active;

import org.circuitsymphony.engine.CircuitEngine;

public class PJfetElm extends JfetElm {
    public PJfetElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, true);
    }

    @Override
    public Class getDumpClass() {
        return JfetElm.class;
    }
}
