package org.circuitsymphony.element.chips;

import org.circuitsymphony.element.ChipElm;
import org.circuitsymphony.engine.CircuitEngine;

import java.util.StringTokenizer;

public class LatchElm extends ChipElm {
    private int loadPin;
    private boolean lastLoad = false;

    public LatchElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public LatchElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                    StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public LatchElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                    StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public String getChipName() {
        return "Latch";
    }

    @Override
    public boolean needsBits() {
        return true;
    }

    @Override
    public void setupPins() {
        sizeX = 2;
        sizeY = bits + 1;
        pins = new Pin[getPostCount()];
        int i;
        for (i = 0; i != bits; i++)
            pins[i] = new Pin(bits - 1 - i, SIDE_W, "I" + i);
        for (i = 0; i != bits; i++) {
            pins[i + bits] = new Pin(bits - 1 - i, SIDE_E, "O");
            pins[i + bits].output = true;
        }
        loadPin = bits * 2;
        pins[loadPin] = new Pin(bits, SIDE_W, "Ld");
        allocNodes();
    }

    @Override
    public void execute() {
        int i;
        if (pins[loadPin].value && !lastLoad)
            for (i = 0; i != bits; i++)
                pins[i + bits].value = pins[i].value;
        lastLoad = pins[loadPin].value;
    }

    @Override
    public int getVoltageSourceCount() {
        return bits;
    }

    @Override
    public int getPostCount() {
        return bits * 2 + 1;
    }

    @Override
    public int getDumpType() {
        return 168;
    }
}
    
