package org.circuitsymphony.element.chips;

import org.circuitsymphony.element.ChipElm;
import org.circuitsymphony.engine.CircuitEngine;

import java.util.StringTokenizer;

public class PhaseCompElm extends ChipElm {
    private boolean ff1;
    private boolean ff2;

    public PhaseCompElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public PhaseCompElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                        StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public PhaseCompElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                        StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public String getChipName() {
        return "phase comparator";
    }

    @Override
    public void setupPins() {
        sizeX = 2;
        sizeY = 2;
        pins = new Pin[3];
        pins[0] = new Pin(0, SIDE_W, "I1");
        pins[1] = new Pin(1, SIDE_W, "I2");
        pins[2] = new Pin(0, SIDE_E, "O");
        pins[2].output = true;
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public void stamp() {
        int vn = engine.nodeList.size() + pins[2].voltSource;
        engine.stampNonLinear(vn);
        engine.stampNonLinear(0);
        engine.stampNonLinear(nodes[2]);
    }

    @Override
    public void doStep() {
        boolean v1 = volts[0] > 2.5;
        boolean v2 = volts[1] > 2.5;
        if (v1 && !pins[0].value)
            ff1 = true;
        if (v2 && !pins[1].value)
            ff2 = true;
        if (ff1 && ff2)
            ff1 = ff2 = false;
        double out = (ff1) ? 5 : (ff2) ? 0 : -1;
        //System.out.println(out + " " + v1 + " " + v2);
        if (out != -1)
            engine.stampVoltageSource(0, nodes[2], pins[2].voltSource, out);
        else {
            // tie current through output pin to 0
            int vn = engine.nodeList.size() + pins[2].voltSource;
            engine.stampMatrix(vn, vn, 1);
        }
        pins[0].value = v1;
        pins[1].value = v2;
    }

    @Override
    public int getPostCount() {
        return 3;
    }

    @Override
    public int getVoltageSourceCount() {
        return 1;
    }

    @Override
    public int getDumpType() {
        return 161;
    }
}
    
