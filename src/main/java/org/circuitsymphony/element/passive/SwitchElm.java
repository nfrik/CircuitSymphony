package org.circuitsymphony.element.passive;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.element.io.LogicInputElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class SwitchElm extends CircuitElm {
    protected boolean momentary;
    // position 0 == closed, position 1 == open
    protected int position, posCount;
    private Point ps;
    private Point ps2;
    private int openhs = 16;

    public SwitchElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        momentary = false;
        position = 0;
        posCount = 2;
    }

    public SwitchElm(CircuitEngine engine, int xx, int yy, boolean mm) {
        super(engine, xx, yy);
        position = (mm) ? 1 : 0;
        momentary = mm;
        posCount = 2;
    }

    public SwitchElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                     StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public SwitchElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                     StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        String str = st.nextToken();
        if (str.compareTo("true") == 0)
            position = (this instanceof LogicInputElm) ? 0 : 1;
        else if (str.compareTo("false") == 0)
            position = (this instanceof LogicInputElm) ? 1 : 0;
        else
            position = new Integer(str);
        momentary = Boolean.valueOf(st.nextToken());
        posCount = 2;
    }

    @Override
    public int getDumpType() {
        return 's';
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + position + " " + momentary;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(32);
        ps = new Point();
        ps2 = new Point();
    }

    @Override
    public void draw(DrawContext ctx) {
        int hs1 = (position == 1) ? 0 : 2;
        int hs2 = (position == 1) ? openhs : 2;
        setBbox(point1, point2, openhs);

        draw2Leads(ctx);

        if (position == 0)
            doDots(ctx);

        if (!needsHighlight(ctx))
            ctx.setColor(ctx.whiteColor);
        interpPoint(lead1, lead2, ps, 0, hs1);
        interpPoint(lead1, lead2, ps2, 1, hs2);

        ctx.drawThickLine(ps, ps2);
        drawPosts(ctx);
    }

    @Override
    public boolean isBasicBoundingBoxSupported() {
        return true;
    }

    @Override
    public void getBasicBoundingBox(Point tempP1, Point tempP2, Rectangle result) {
        setBbox(result, tempP1, tempP2, openhs);
    }

    @Override
    public void calculateCurrent() {
        if (position == 1)
            current = 0;
    }

    @Override
    public void stamp() {
        if (position == 0)
            engine.stampVoltageSource(nodes[0], nodes[1], voltSource, 0);
    }

    @Override
    public int getVoltageSourceCount() {
        return (position == 1) ? 0 : 1;
    }

    public void mouseUp() {
        if (momentary)
            toggle();
    }

    public void toggle() {
        position++;
        if (position >= posCount)
            position = 0;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = (momentary) ? "push switch (SPST)" : "switch (SPST)";
        if (position == 1) {
            arr[1] = "open";
            arr[2] = "Vd = " + ctx.getVoltageDText(getVoltageDiff());
        } else {
            arr[1] = "closed";
            arr[2] = "V = " + ctx.getVoltageText(volts[0]);
            arr[3] = "I = " + ctx.getCurrentDText(getCurrent());
        }
    }

    @Override
    public boolean getConnection(int n1, int n2) {
        return position == 0;
    }

    @Override
    public boolean isWire() {
        return true;
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Momentary Switch", momentary));
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            momentary = ei.getCheckbox().getState();
    }

    public boolean isMomentary() {
        return momentary;
    }
}
