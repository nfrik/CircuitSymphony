package org.circuitsymphony.element;

import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class WireElm extends CircuitElm {
    private static final int FLAG_SHOWCURRENT = 1;
    private static final int FLAG_SHOWVOLTAGE = 2;
    private int hs = 3;

    public WireElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public WireElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                   StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public WireElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                   StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
    }

    @Override
    public void draw(DrawContext ctx) {
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(point1, point2);
        doDots(ctx);
        setBbox(point1, point2, hs);
        if (mustShowCurrent()) {
            String s = ctx.getShortUnitText(Math.abs(getCurrent()), "A");
            drawValues(ctx, s, 4);
        } else if (mustShowVoltage()) {
            String s = ctx.getShortUnitText(volts[0], "V");
            drawValues(ctx, s, 4);
        }
        drawPosts(ctx);
    }

    @Override
    public boolean isBasicBoundingBoxSupported() {
        return true;
    }

    @Override
    public void getBasicBoundingBox(Point tempP1, Point tempP2, Rectangle result) {
        setBbox(result, tempP1, tempP2, hs + 2);
    }

    @Override
    public void stamp() {
        engine.stampVoltageSource(nodes[0], nodes[1], voltSource, 0);
    }

    private boolean mustShowCurrent() {
        return (flags & FLAG_SHOWCURRENT) != 0;
    }

    private boolean mustShowVoltage() {
        return (flags & FLAG_SHOWVOLTAGE) != 0;
    }

    @Override
    public int getVoltageSourceCount() {
        return 1;
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "wire";
        arr[1] = "I = " + ctx.getCurrentDText(getCurrent());
        arr[2] = "V = " + ctx.getVoltageText(volts[0]);
    }

    @Override
    public int getDumpType() {
        return 'w';
    }

    @Override
    public double getPower() {
        return 0;
    }

    @Override
    public double getVoltageDiff() {
        return volts[0];
    }

    @Override
    public boolean isWire() {
        return true;
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Show Current", mustShowCurrent()));
            return ei;
        }
        if (n == 1) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Show Voltage", mustShowVoltage()));
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0) {
            if (ei.getCheckbox().getState())
                flags = FLAG_SHOWCURRENT;
            else
                flags &= ~FLAG_SHOWCURRENT;
        }
        if (n == 1) {
            if (ei.getCheckbox().getState())
                flags = FLAG_SHOWVOLTAGE;
            else
                flags &= ~FLAG_SHOWVOLTAGE;
        }
    }

    @Override
    public boolean needsShortcut() {
        return true;
    }
}
