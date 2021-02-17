package org.circuitsymphony.element.active;

import org.circuitsymphony.element.ChipElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;

import java.util.StringTokenizer;

public class CC2Elm extends ChipElm {
    private final double gain;

    public CC2Elm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        gain = 1;
    }

    public CC2Elm(CircuitEngine engine, int xx, int yy, int g) {
        super(engine, xx, yy);
        gain = g;
    }

    public CC2Elm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public CC2Elm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2, StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
        gain = new Double(st.nextToken());
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + gain;
    }

    @Override
    public String getChipName() {
        return "CC2";
    }

    @Override
    public void setupPins() {
        sizeX = 2;
        sizeY = 3;
        pins = new Pin[3];
        pins[0] = new Pin(0, SIDE_W, "X");
        pins[0].output = true;
        pins[1] = new Pin(2, SIDE_W, "Y");
        pins[2] = new Pin(1, SIDE_E, "Z");
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = (gain == 1) ? "CCII+" : "CCII-";
        arr[1] = "X,Y = " + ctx.getVoltageText(volts[0]);
        arr[2] = "Z = " + ctx.getVoltageText(volts[2]);
        arr[3] = "I = " + ctx.getCurrentText(pins[0].current);
    }

    //boolean nonLinear() { return true; }
    @Override
    public void stamp() {
        // X voltage = Y voltage
        engine.stampVoltageSource(0, nodes[0], pins[0].voltSource);
        engine.stampVCVS(0, nodes[1], 1, pins[0].voltSource);
        // Z current = gain * X current
        engine.stampCCCS(0, nodes[2], pins[0].voltSource, gain);
    }

    @Override
    public void draw(DrawContext ctx) {
        pins[2].current = pins[0].current * gain;
        drawChip(ctx);
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
        return 179;
    }
}
