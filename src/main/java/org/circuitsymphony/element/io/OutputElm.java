package org.circuitsymphony.element.io;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class OutputElm extends CircuitElm {
    private final int FLAG_VALUE = 1;

    public OutputElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public OutputElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                     StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public OutputElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                     StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
    }

    @Override
    public int getDumpType() {
        return 'O';
    }

    @Override
    public int getPostCount() {
        return 1;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        lead1 = new Point();
    }

    @Override
    public void draw(DrawContext ctx) {
        boolean selected = (needsHighlight(ctx) || ctx.getSim().plotYElm == this);
        Font f = new Font("SansSerif", selected ? Font.BOLD : 0, 14);
        ctx.setFont(f);
        ctx.setColor(selected ? ctx.selectColor : ctx.whiteColor);
        String s = (flags & FLAG_VALUE) != 0 ? ctx.getVoltageText(volts[0]) : "out";
        FontMetrics fm = ctx.getFontMetrics();
        if (this == ctx.getSim().plotXElm)
            s = "X";
        if (this == ctx.getSim().plotYElm)
            s = "Y";
        interpPoint(point1, point2, lead1, 1 - (fm.stringWidth(s) / 2 + 8) / dn);
        setBbox(point1, lead1, 0);
        drawCenteredText(ctx, s, x2, y2, true);
        setVoltageColor(ctx, volts[0]);
        if (selected)
            ctx.setColor(ctx.selectColor);
        ctx.drawThickLine(point1, lead1);
        drawPosts(ctx);
    }

    @Override
    public double getVoltageDiff() {
        return volts[0];
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "output";
        arr[1] = "V = " + ctx.getVoltageText(volts[0]);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Show Voltage", (flags & FLAG_VALUE) != 0));
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            flags = (ei.getCheckbox().getState()) ?
                    (flags | FLAG_VALUE) :
                    (flags & ~FLAG_VALUE);
    }
}
