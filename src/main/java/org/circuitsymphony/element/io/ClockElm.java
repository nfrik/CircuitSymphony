package org.circuitsymphony.element.io;

import org.circuitsymphony.engine.CircuitEngine;

public class ClockElm extends RailElm {
    public ClockElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, WF_SQUARE);
        maxVoltage = 2.5;
        bias = 2.5;
        frequency = 100;
        flags |= FLAG_CLOCK;
    }

    @Override
    public Class getDumpClass() {
        return RailElm.class;
    }
}
