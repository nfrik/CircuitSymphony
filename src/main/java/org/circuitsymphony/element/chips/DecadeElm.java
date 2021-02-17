package org.circuitsymphony.element.chips;

import org.circuitsymphony.element.ChipElm;
import org.circuitsymphony.engine.CircuitEngine;

import java.util.StringTokenizer;

public class DecadeElm extends ChipElm {
    public DecadeElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public DecadeElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                     StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public DecadeElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                     StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public String getChipName() {
        return "decade counter";
    }

    @Override
    public boolean needsBits() {
        return true;
    }

    @Override
    public void setupPins() {
        sizeX = bits > 2 ? bits : 2;
        sizeY = 2;
        pins = new Pin[getPostCount()];
        pins[0] = new Pin(1, SIDE_W, "");
        pins[0].clock = true;
        pins[1] = new Pin(sizeX - 1, SIDE_S, "R");
        pins[1].bubble = true;
        int i;
        for (i = 0; i != bits; i++) {
            int ii = i + 2;
            pins[ii] = new Pin(i, SIDE_N, "Q" + i);
            pins[ii].output = pins[ii].state = true;
        }
        allocNodes();
    }

    @Override
    public int getPostCount() {
        return bits + 2;
    }

    @Override
    public int getVoltageSourceCount() {
        return bits;
    }

    @Override
    public void execute() {
        int i;
        if (pins[0].value && !lastClock) {
            for (i = 0; i != bits; i++)
                if (pins[i + 2].value) break;
            if (i < bits)
                pins[i++ + 2].value = false;
            i %= bits;
            pins[i + 2].value = true;
        }

        if (!pins[1].value) {
            for (i = 1; i != bits; i++)
                pins[i + 2].value = false;
            pins[2].value = true;
        }

        lastClock = pins[0].value;
    }

    @Override
    public int getDumpType() {
        return 163;
    }
}
