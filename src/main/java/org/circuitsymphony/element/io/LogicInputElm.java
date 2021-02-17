package org.circuitsymphony.element.io;

import org.circuitsymphony.element.passive.SwitchElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class LogicInputElm extends SwitchElm {
    private final int FLAG_TERNARY = 1;
    private final int FLAG_NUMERIC = 2;
    private double hiV;
    private double loV;

    public LogicInputElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, false);
        hiV = 5;
        loV = 0;
    }

    public LogicInputElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                         StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public LogicInputElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                         StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
        try {
            hiV = new Double(st.nextToken());
            loV = new Double(st.nextToken());
        } catch (Exception e) {
            hiV = 5;
            loV = 0;
        }
        if (isTernary())
            posCount = 3;
    }

    private boolean isTernary() {
        return (flags & FLAG_TERNARY) != 0;
    }

    private boolean isNumeric() {
        return (flags & (FLAG_TERNARY | FLAG_NUMERIC)) != 0;
    }

    @Override
    public int getDumpType() {
        return 'L';
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + hiV + " " + loV;
    }

    @Override
    public int getPostCount() {
        return 1;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        lead1 = interpPoint(point1, point2, 1 - 12 / dn);
    }

    @Override
    public void draw(DrawContext ctx) {
        Font f = new Font("SansSerif", Font.BOLD, 20);
        ctx.setFont(f);
        ctx.setColor(needsHighlight(ctx) ? ctx.selectColor : ctx.whiteColor);
        String s = position == 0 ? "L" : "H";
        if (isNumeric())
            s = "" + position;
        setBbox(point1, lead1, 0);
        drawCenteredText(ctx, s, x2, y2, true);
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(point1, lead1);
        updateDotCount(ctx);
        drawDots(ctx, point1, lead1, curcount);
        drawPosts(ctx);
    }

    @Override
    public boolean isBasicBoundingBoxSupported() {
        return false;
    }

    @Override
    public void setCurrent(int vs, double c) {
        current = -c;
    }

    @Override
    public void stamp() {
        double v = (position == 0) ? loV : hiV;
        if (isTernary())
            v = position * 2.5;
        engine.stampVoltageSource(0, nodes[0], voltSource, v);
    }

    @Override
    public int getVoltageSourceCount() {
        return 1;
    }

    @Override
    public double getVoltageDiff() {
        return volts[0];
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "logic input";
        arr[1] = (position == 0) ? "low" : "high";
        if (isNumeric())
            arr[1] = "" + position;
        arr[1] += " (" + ctx.getVoltageText(volts[0]) + ")";
        arr[2] = "I = " + ctx.getCurrentText(getCurrent());
    }

    @Override
    public boolean hasGroundConnection(int n1) {
        return true;
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0) {
            EditInfo ei = new EditInfo("", 0, 0, 0);
            ei.setCheckbox(new Checkbox("Momentary Switch", momentary));
            return ei;
        }
        if (n == 1)
            return new EditInfo("High Voltage", hiV, 10, -10);
        if (n == 2)
            return new EditInfo("Low Voltage", loV, 10, -10);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            momentary = ei.getCheckbox().getState();
        if (n == 1)
            hiV = ei.getValue();
        if (n == 2)
            loV = ei.getValue();
    }
}
