package org.circuitsymphony.element.logicgates;

import org.circuitsymphony.engine.CircuitEngine;

import java.util.StringTokenizer;

public class NorGateElm extends OrGateElm {
    public NorGateElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public NorGateElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                      StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public NorGateElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                      StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public String getGateName() {
        return "NOR gate";
    }

    @Override
    public boolean isInverting() {
        return true;
    }

    @Override
    public int getDumpType() {
        return 153;
    }
}
