package org.circuitsymphony.element.chips;

import org.circuitsymphony.element.ChipElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;

import java.awt.*;
import java.util.StringTokenizer;

public class SevenSegElm extends ChipElm {
    private Color darkred;

    public SevenSegElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public SevenSegElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                       StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public SevenSegElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                       StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public String getChipName() {
        return "7-segment driver/display";
    }

    @Override
    public void setupPins() {
        darkred = new Color(30, 0, 0);
        sizeX = 4;
        sizeY = 4;
        pins = new Pin[7];
        pins[0] = new Pin(0, SIDE_W, "a");
        pins[1] = new Pin(1, SIDE_W, "b");
        pins[2] = new Pin(2, SIDE_W, "c");
        pins[3] = new Pin(3, SIDE_W, "d");
        pins[4] = new Pin(1, SIDE_S, "e");
        pins[5] = new Pin(2, SIDE_S, "f");
        pins[6] = new Pin(3, SIDE_S, "g");
    }

    @Override
    public void draw(DrawContext ctx) {
        drawChip(ctx);
        ctx.setColor(Color.red);
        int xl = x + cspc * 5;
        int yl = y + cspc;
        setColor(ctx, 0);
        ctx.drawThickLine(xl, yl, xl + cspc, yl);
        setColor(ctx, 1);
        ctx.drawThickLine(xl + cspc, yl, xl + cspc, yl + cspc);
        setColor(ctx, 2);
        ctx.drawThickLine(xl + cspc, yl + cspc, xl + cspc, yl + cspc2);
        setColor(ctx, 3);
        ctx.drawThickLine(xl, yl + cspc2, xl + cspc, yl + cspc2);
        setColor(ctx, 4);
        ctx.drawThickLine(xl, yl + cspc, xl, yl + cspc2);
        setColor(ctx, 5);
        ctx.drawThickLine(xl, yl, xl, yl + cspc);
        setColor(ctx, 6);
        ctx.drawThickLine(xl, yl + cspc, xl + cspc, yl + cspc);
    }

    private void setColor(DrawContext ctx, int p) {
        ctx.setColor(pins[p].value ? Color.red : darkred);
    }

    @Override
    public int getPostCount() {
        return 7;
    }

    @Override
    public int getVoltageSourceCount() {
        return 0;
    }

    @Override
    public int getDumpType() {
        return 157;
    }
}
