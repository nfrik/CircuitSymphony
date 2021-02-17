package org.circuitsymphony.element.logicgates;

import org.circuitsymphony.engine.CircuitEngine;

import java.util.StringTokenizer;

public class NandGateElm extends AndGateElm {
    public NandGateElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public NandGateElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                       StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public NandGateElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                       StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public boolean isInverting() {
        return true;
    }

    @Override
    public String getGateName() {
        return "NAND gate";
    }

    @Override
    public int getDumpType() {
        return 151;
    }
}
