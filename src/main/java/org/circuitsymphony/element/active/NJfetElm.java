package org.circuitsymphony.element.active;

import org.circuitsymphony.engine.CircuitEngine;

public class NJfetElm extends JfetElm {
    public NJfetElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, false);
    }

    @Override
    public Class getDumpClass() {
        return JfetElm.class;
    }
}
