package org.circuitsymphony.element.io;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class LogicOutputElm extends CircuitElm {
    private final int FLAG_TERNARY = 1;
    private final int FLAG_NUMERIC = 2;
    private final int FLAG_PULLDOWN = 4;
    private double threshold;
    private String value;

    public LogicOutputElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        threshold = 2.5;
    }

    public LogicOutputElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                          StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public LogicOutputElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                          StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        try {
            threshold = new Double(st.nextToken());
        } catch (Exception e) {
            threshold = 2.5;
        }
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + threshold;
    }

    @Override
    public int getDumpType() {
        return 'M';
    }

    @Override
    public int getPostCount() {
        return 1;
    }

    private boolean isTernary() {
        return (flags & FLAG_TERNARY) != 0;
    }

    private boolean isNumeric() {
        return (flags & (FLAG_TERNARY | FLAG_NUMERIC)) != 0;
    }

    private boolean needsPullDown() {
        return (flags & FLAG_PULLDOWN) != 0;
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
        //g.setColor(needsHighlight() ? selectColor : lightGrayColor);
        ctx.setColor(ctx.lightGrayColor);
        String s = (volts[0] < threshold) ? "L" : "H";
        if (isTernary()) {
            if (volts[0] > 3.75)
                s = "2";
            else if (volts[0] > 1.25)
                s = "1";
            else
                s = "0";
        } else if (isNumeric())
            s = (volts[0] < threshold) ? "0" : "1";
        value = s;
        setBbox(point1, lead1, 0);
        drawCenteredText(ctx, s, x2, y2, true);
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(point1, lead1);
        drawPosts(ctx);
    }

    @Override
    public void stamp() {
        if (needsPullDown())
            engine.stampResistor(nodes[0], 0, 1e6);
    }

    @Override
    public double getVoltageDiff() {
        return volts[0];
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "logic output";
        arr[1] = (volts[0] < threshold) ? "low" : "high";
        if (isNumeric())
            arr[1] = value;
        arr[2] = "V = " + ctx.getVoltageText(volts[0]);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Threshold", threshold, 10, -10);
        if (n == 1) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Current Required", needsPullDown()));
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            threshold = ei.getValue();
        if (n == 1) {
            if (ei.getCheckbox().getState())
                flags = FLAG_PULLDOWN;
            else
                flags &= ~FLAG_PULLDOWN;
        }
    }
}
