package org.circuitsymphony.element.logicgates;

import org.circuitsymphony.engine.CircuitEngine;

import java.util.StringTokenizer;

public class XorGateElm extends OrGateElm {
    public XorGateElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public XorGateElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                      StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public XorGateElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                      StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public String getGateName() {
        return "XOR gate";
    }

    @Override
    public boolean calcFunction() {
        int i;
        boolean f = false;
        for (i = 0; i != inputCount; i++)
            f ^= getInput(i);
        return f;
    }

    @Override
    public int getDumpType() {
        return 154;
    }
}
