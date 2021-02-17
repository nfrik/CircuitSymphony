package org.circuitsymphony.element.io;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class CurrentElm extends CircuitElm {
    private double currentValue;
    private Polygon arrow;
    private Point ashaft1;
    private Point ashaft2;
    private Point center;

    public CurrentElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        currentValue = .01;
    }

    public CurrentElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                      StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public CurrentElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                      StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        try {
            currentValue = new Double(st.nextToken());
        } catch (Exception e) {
            currentValue = .01;
        }
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + currentValue;
    }

    @Override
    public int getDumpType() {
        return 'i';
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(26);
        ashaft1 = interpPoint(lead1, lead2, .25);
        ashaft2 = interpPoint(lead1, lead2, .6);
        center = interpPoint(lead1, lead2, .5);
        Point p2 = interpPoint(lead1, lead2, .75);
        arrow = calcArrow(center, p2, 4, 4);
    }

    @Override
    public void draw(DrawContext ctx) {
        int cr = 12;
        draw2Leads(ctx);
        setVoltageColor(ctx, (volts[0] + volts[1]) / 2);
        setPowerColor(ctx, false);

        ctx.drawThickCircle(center.x, center.y, cr);
        ctx.drawThickLine(ashaft1, ashaft2);
        ctx.fillPolygon(arrow);
        setBbox(point1, point2, cr);
        doDots(ctx);

        if (ctx.getSim().getShowValuesCheckItem().getState()) {
            String s = ctx.getShortUnitText(currentValue, "A");
            if (dx == 0 || dy == 0)
                drawValues(ctx, s, cr);
        }

        drawPosts(ctx);
    }

    @Override
    public void stamp() {
        current = currentValue;
        engine.stampCurrentSource(nodes[0], nodes[1], current);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Current (A)", currentValue, 0, .1);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        currentValue = ei.getValue();
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "current source";
        getBasicInfo(ctx, arr);
    }

    @Override
    public double getVoltageDiff() {
        return volts[1] - volts[0];
    }
}
