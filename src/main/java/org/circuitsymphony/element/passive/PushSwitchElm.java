package org.circuitsymphony.element.passive;

import org.circuitsymphony.engine.CircuitEngine;

public class PushSwitchElm extends SwitchElm {
    public PushSwitchElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, true);
    }

    @Override
    public Class getDumpClass() {
        return SwitchElm.class;
    }
}
