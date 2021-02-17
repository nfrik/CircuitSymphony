package org.circuitsymphony.element.devices;

import org.circuitsymphony.element.ChipElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;

import java.awt.*;
import java.util.StringTokenizer;

public class LEDArrayElm extends ChipElm {
    private Color darkgreen;

    public LEDArrayElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public LEDArrayElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                       StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public LEDArrayElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                       StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public String getChipName() {
        return "LED Array";
    }

    @Override
    public void setupPins() {
        darkgreen = new Color(0, 30, 0);
        sizeX = 2;
        sizeY = 10;
        pins = new Pin[10];
        pins[0] = new Pin(0, SIDE_W, "i9");
        pins[1] = new Pin(1, SIDE_W, "i8");
        pins[2] = new Pin(2, SIDE_W, "i7");
        pins[3] = new Pin(3, SIDE_W, "i6");
        pins[4] = new Pin(4, SIDE_W, "i5");
        pins[5] = new Pin(5, SIDE_W, "i4");
        pins[6] = new Pin(6, SIDE_W, "i3");
        pins[7] = new Pin(7, SIDE_W, "i2");
        pins[8] = new Pin(8, SIDE_W, "i1");
        pins[9] = new Pin(9, SIDE_W, "i0");
    }

    @Override
    public void draw(DrawContext ctx) {
        drawChip(ctx);
        ctx.setColor(Color.green);
        int s = csize * 16;
        int p = csize * 5;
        int xl = x + cspc * 3;
        int yl = y;

        setColor(ctx, 0);
        ctx.fillRect(xl, yl, p * 2, p);

        setColor(ctx, 1);
        ctx.fillRect(xl, yl + s, p * 2, p);

        setColor(ctx, 2);
        ctx.fillRect(xl, yl + (s * 2), p * 2, p);

        setColor(ctx, 3);
        ctx.fillRect(xl, yl + (s * 3), p * 2, p);

        setColor(ctx, 4);
        ctx.fillRect(xl, yl + (s * 4), p * 2, p);

        setColor(ctx, 5);
        ctx.fillRect(xl, yl + (s * 5), p * 2, p);

        setColor(ctx, 6);
        ctx.fillRect(xl, yl + (s * 6), p * 2, p);

        setColor(ctx, 7);
        ctx.fillRect(xl, yl + (s * 7), p * 2, p);

        setColor(ctx, 8);
        ctx.fillRect(xl, yl + (s * 8), p * 2, p);

        setColor(ctx, 9);
        ctx.fillRect(xl, yl + (s * 9), p * 2, p);
    }

    private void setColor(DrawContext ctx, int p) {
        ctx.setColor(pins[p].value ? Color.green : darkgreen);
    }

    @Override
    public int getPostCount() {
        return 10;
    }

    @Override
    public int getVoltageSourceCount() {
        return 0;
    }

    @Override
    public int getDumpType() {
        return 176;
    }
}
