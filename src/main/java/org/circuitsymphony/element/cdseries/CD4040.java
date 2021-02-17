package org.circuitsymphony.element.cdseries;//import java.awt.*;

import org.circuitsymphony.element.ChipCDElm;
import org.circuitsymphony.engine.CircuitEngine;

import java.util.StringTokenizer;

public class CD4040 extends ChipCDElm {
    private final int FLAG_ENABLE = 2;
    private final int bits = 12;

    public CD4040(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public CD4040(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                  StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public CD4040(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                  StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    public String getChipName() {
        return "Counter 12-bit (4040)";
    }

    public void setupPins() {
        sizeX = 3;
        sizeY = bits;

        pins = new Pin[getPostCount()];
        pins[0] = new Pin(0, SIDE_W, "");
        pins[0].clock = true;
        pins[1] = new Pin(sizeY - 1, SIDE_W, "R");
        pins[1].bubble = true;
        int i;
        for (i = 0; i != bits; i++) {
            int ii = i + 2;
            pins[ii] = new Pin(i, SIDE_E, "Q" + (bits - i - 1));
            pins[ii].output = pins[ii].state = true;
        }
        if (hasEnable())
            pins[bits + 2] = new Pin(sizeY - 2, SIDE_W, "En");
        allocNodes();
    }

    public int getPostCount() {
        if (hasEnable())
            return bits + 3;
        return bits + 2;
    }

    private boolean hasEnable() {
        return (flags & FLAG_ENABLE) != 0;
    }

    public int getVoltageSourceCount() {
        return bits;
    }

    public void execute() {
        boolean en = true;
        if (hasEnable())
            en = pins[bits + 2].value;

        if (pins[0].value && !lastClock && en) {
            int i;
            for (i = bits - 1; i >= 0; i--) {
                int ii = i + 2;
                if (!pins[ii].value) {
                    pins[ii].value = true;
                    break;
                }
                pins[ii].value = false;
            }
        }
        if (pins[1].value) {
            int i;
            for (i = 0; i != bits; i++)
                pins[i + 2].value = false;
        }
        lastClock = pins[0].value;
    }

    public int getDumpType() {
        return 183;
    }
}
