package org.circuitsymphony.element.chips;

import org.circuitsymphony.element.ChipElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class DFlipFlopElm extends ChipElm {
    private final int FLAG_RESET = 2;

    public DFlipFlopElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public DFlipFlopElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                        StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public DFlipFlopElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                        StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
        pins[2].value = !pins[1].value;
    }

    private boolean hasReset() {
        return (flags & FLAG_RESET) != 0;
    }

    @Override
    public String getChipName() {
        return "D flip-flop";
    }

    @Override
    public void setupPins() {
        sizeX = 2;
        sizeY = 3;
        pins = new Pin[getPostCount()];
        pins[0] = new Pin(0, SIDE_W, "D");
        pins[1] = new Pin(0, SIDE_E, "Q");
        pins[1].output = pins[1].state = true;
        pins[2] = new Pin(2, SIDE_E, "Q");
        pins[2].output = true;
        pins[2].lineOver = true;
        pins[3] = new Pin(1, SIDE_W, "");
        pins[3].clock = true;
        if (hasReset())
            pins[4] = new Pin(2, SIDE_W, "R");
    }

    @Override
    public int getPostCount() {
        return hasReset() ? 5 : 4;
    }

    @Override
    public int getVoltageSourceCount() {
        return 2;
    }

    @Override
    public void execute() {
        if (pins[3].value && !lastClock) {
            pins[1].value = pins[0].value;
            pins[2].value = !pins[0].value;
        }
        if (pins.length > 4 && pins[4].value) {
            pins[1].value = false;
            pins[2].value = true;
        }
        lastClock = pins[3].value;
    }

    @Override
    public int getDumpType() {
        return 155;
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 2) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Reset Pin", hasReset()));
            return ei;
        }
        return super.getEditInfo(n);
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 2) {
            if (ei.getCheckbox().getState())
                flags |= FLAG_RESET;
            else
                flags &= ~FLAG_RESET;
            setupPins();
            allocNodes();
            setPoints();
        }
        super.setEditValue(n, ei);
    }
}
