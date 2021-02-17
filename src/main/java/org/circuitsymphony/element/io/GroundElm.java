package org.circuitsymphony.element.io;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;

import java.awt.*;
import java.util.StringTokenizer;

public class GroundElm extends CircuitElm {
    private int hs = 11;

    public GroundElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public GroundElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                     StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public GroundElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                     StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
    }

    @Override
    public int getDumpType() {
        return 'g';
    }

    @Override
    public int getPostCount() {
        return 1;
    }

    @Override
    public void draw(DrawContext ctx) {
        setVoltageColor(ctx, 0);
        ctx.drawThickLine(point1, point2);
        int i;
        for (i = 0; i != 3; i++) {
            int a = 10 - i * 4;
            int b = i * 5; // -10;
            interpPoint2(point1, point2, ps1, ps2, 1 + b / dn, a);
            ctx.drawThickLine(ps1, ps2);
        }
        doDots(ctx);
        interpPoint(point1, point2, ps2, 1 + 11. / dn);
        setBbox(point1, ps2, hs);
        drawPost(ctx, x, y, nodes[0]);
    }

    @Override
    public boolean isBasicBoundingBoxSupported() {
        return true;
    }

    @Override
    public void getBasicBoundingBox(Point tempP1, Point tempP2, Rectangle result) {
        tempP2.x += hs;
        setBbox(result, tempP1, tempP2, hs);
    }

    @Override
    public void setCurrent(int x, double c) {
        current = -c;
    }

    @Override
    public void stamp() {
        engine.stampVoltageSource(0, nodes[0], voltSource, 0);
    }

    @Override
    public double getVoltageDiff() {
        return 0;
    }

    @Override
    public int getVoltageSourceCount() {
        return 1;
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "ground";
        arr[1] = "I = " + ctx.getCurrentText(getCurrent());
    }

    @Override
    public boolean hasGroundConnection(int n1) {
        return true;
    }

    @Override
    public boolean needsShortcut() {
        return true;
    }
}
