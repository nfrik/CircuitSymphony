package org.circuitsymphony.element.passive;

import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class Switch2Elm extends SwitchElm {
    private static final int FLAG_CENTER_OFF = 1;
    private final int openhs = 16;
    private int link;
    private Point[] swposts;
    private Point[] swpoles;

    public Switch2Elm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, false);
        noDiagonal = true;
    }

    public Switch2Elm(CircuitEngine engine, int xx, int yy, boolean mm) {
        super(engine, xx, yy, mm);
        noDiagonal = true;
    }

    public Switch2Elm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                      StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public Switch2Elm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                      StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
        link = new Integer(st.nextToken());
        noDiagonal = true;
    }

    @Override
    public int getDumpType() {
        return 'S';
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + link;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(32);
        swposts = newPointArray(2);
        swpoles = newPointArray(3);
        interpPoint2(lead1, lead2, swpoles[0], swpoles[1], 1, openhs);
        swpoles[2] = lead2;
        interpPoint2(point1, point2, swposts[0], swposts[1], 1, openhs);
        posCount = hasCenterOff() ? 3 : 2;
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(point1, point2, openhs);

        // draw first lead
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(point1, lead1);

        // draw second lead
        setVoltageColor(ctx, volts[1]);
        ctx.drawThickLine(swpoles[0], swposts[0]);

        // draw third lead
        setVoltageColor(ctx, volts[2]);
        ctx.drawThickLine(swpoles[1], swposts[1]);

        // draw switch
        if (!needsHighlight(ctx))
            ctx.setColor(ctx.whiteColor);
        ctx.drawThickLine(lead1, swpoles[position]);

        updateDotCount(ctx);
        drawDots(ctx, point1, lead1, curcount);
        if (position != 2)
            drawDots(ctx, swpoles[position], swposts[position], curcount);
        drawPosts(ctx);
    }

    @Override
    public boolean isBasicBoundingBoxSupported() {
        return false;
    }

    @Override
    public Point getPost(int n) {
        return (n == 0) ? point1 : swposts[n - 1];
    }

    @Override
    public int getPostCount() {
        return 3;
    }

    @Override
    public void calculateCurrent() {
        if (position == 2)
            current = 0;
    }

    @Override
    public void stamp() {
        if (position == 2) // in center?
            return;
        engine.stampVoltageSource(nodes[0], nodes[position + 1], voltSource, 0);
    }

    @Override
    public int getVoltageSourceCount() {
        return (position == 2) ? 0 : 1;
    }

    @Override
    public void toggle() {
        super.toggle();
        if (link != 0) {
            int i;
            for (i = 0; i != engine.elmList.size(); i++) {
                Object o = engine.elmList.elementAt(i);
                if (o instanceof Switch2Elm) {
                    Switch2Elm s2 = (Switch2Elm) o;
                    if (s2.link == link)
                        s2.position = position;
                }
            }
        }
    }

    @Override
    public boolean getConnection(int n1, int n2) {
        return position != 2 && comparePair(n1, n2, 0, 1 + position);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = (link == 0) ? "switch (SPDT)" : "switch (DPDT)";
        arr[1] = "I = " + ctx.getCurrentDText(getCurrent());
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 1) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Center Off", hasCenterOff()));
            return ei;
        }
        return super.getEditInfo(n);
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 1) {
            flags &= ~FLAG_CENTER_OFF;
            if (ei.getCheckbox().getState())
                flags |= FLAG_CENTER_OFF;
            if (hasCenterOff())
                momentary = false;
            setPoints();
        } else
            super.setEditValue(n, ei);
    }

    private boolean hasCenterOff() {
        return (flags & FLAG_CENTER_OFF) != 0;
    }
}
