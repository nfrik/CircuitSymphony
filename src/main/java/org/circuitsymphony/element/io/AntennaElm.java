package org.circuitsymphony.element.io;

import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.element.CircuitElm;

import java.util.StringTokenizer;

public class AntennaElm extends RailElm {
    private double fmphase;

    public AntennaElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, WF_DC);
    }

    public AntennaElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                      StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public AntennaElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                      StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
        waveform = WF_DC;
    }

    @Override
    public void stamp() {
        engine.stampVoltageSource(0, nodes[0], voltSource);
    }

    @Override
    public void doStep() {
        engine.updateVoltageSource(voltSource, getVoltage());
    }

    @Override
    public double getVoltage() {
        fmphase += 2 * CircuitElm.PI * (2200 + Math.sin(2 * CircuitElm.PI * engine.t * 13) * 100) * engine.timeStep;
        double fm = 3 * Math.sin(fmphase);
        return Math.sin(2 * CircuitElm.PI * engine.t * 3000) * (1.3 + Math.sin(2 * CircuitElm.PI * engine.t * 12)) * 3 +
                Math.sin(2 * CircuitElm.PI * engine.t * 2710) * (1.3 + Math.sin(2 * CircuitElm.PI * engine.t * 13)) * 3 +
                Math.sin(2 * CircuitElm.PI * engine.t * 2433) * (1.3 + Math.sin(2 * CircuitElm.PI * engine.t * 14)) * 3 + fm;
    }

    @Override
    public int getDumpType() {
        return 'A';
    }
}
