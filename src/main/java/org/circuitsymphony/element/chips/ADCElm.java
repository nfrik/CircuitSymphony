package org.circuitsymphony.element.chips;

import org.circuitsymphony.element.ChipElm;
import org.circuitsymphony.engine.CircuitEngine;

import java.util.StringTokenizer;

public class ADCElm extends ChipElm {
    public ADCElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public ADCElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                  StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public ADCElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                  StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public String getChipName() {
        return "ADC";
    }

    @Override
    public boolean needsBits() {
        return true;
    }

    @Override
    public void setupPins() {
        sizeX = 2;
        sizeY = bits > 2 ? bits : 2;
        pins = new Pin[getPostCount()];
        int i;
        for (i = 0; i != bits; i++) {
            pins[i] = new Pin(bits - 1 - i, SIDE_E, "D" + i);
            pins[i].output = true;
        }
        pins[bits] = new Pin(0, SIDE_W, "In");
        pins[bits + 1] = new Pin(sizeY - 1, SIDE_W, "V+");
        allocNodes();
    }

    @Override
    public void execute() {
        int imax = (1 << bits) - 1;
        // if we round, the half-flash doesn't work
        double val = imax * volts[bits] / volts[bits + 1]; // + .5;
        int ival = (int) val;
        ival = min(imax, max(0, ival));
        int i;
        for (i = 0; i != bits; i++)
            pins[i].value = ((ival & (1 << i)) != 0);
    }

    @Override
    public int getVoltageSourceCount() {
        return bits;
    }

    @Override
    public int getPostCount() {
        return bits + 2;
    }

    @Override
    public int getDumpType() {
        return 167;
    }
}
    
