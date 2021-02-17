package org.circuitsymphony.element.io;

import org.circuitsymphony.engine.CircuitEngine;

public class DCVoltageElm extends VoltageElm {
    public DCVoltageElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, WF_DC);
    }

    @Override
    public Class getDumpClass() {
        return VoltageElm.class;
    }
}
