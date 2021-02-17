package org.circuitsymphony.element.logicgates;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class InverterSTElm extends CircuitElm {

    private double slewRate; // V/ns
    private Polygon gatePoly;
    private Point pcircle;

    public InverterSTElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        noDiagonal = true;
        slewRate = 0.5;
    }

    public InverterSTElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                         StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public InverterSTElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                         StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        noDiagonal = true;
        try {
            slewRate = new Double(st.nextToken());
        } catch (Exception e) {
            slewRate = .5;
        }
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + slewRate;
    }

    @Override
    public int getDumpType() {
        return 186;
    }

    @Override
    public void draw(DrawContext ctx) {
        drawPosts(ctx);
        draw2Leads(ctx);
        ctx.setColor(needsHighlight(ctx) ? ctx.selectColor : ctx.lightGrayColor);
        ctx.drawThickPolygon(gatePoly);
        ctx.drawThickCircle(pcircle.x, pcircle.y, 3);
        curcount = updateDotCount(ctx, current, curcount);
        drawDots(ctx, lead2, point2, curcount);
        if (lead2.x > lead1.x) {
            //System.out.println(lead1.x + "-" + lead2.x);
            ctx.drawLine(lead1.x + 5, lead1.y - 5, lead1.x + 5, lead1.y + 2);
            ctx.drawLine(lead1.x + 10, lead1.y - 2, lead1.x + 10, lead1.y + 5);
            ctx.drawRect(lead1.x + 5, lead1.y - 2, 5, 5);
        }
        if (lead2.x < lead1.x) {
            ctx.drawLine(lead1.x - 5, lead1.y - 5, lead1.x - 5, lead1.y + 2);
            ctx.drawLine(lead1.x - 10, lead1.y - 2, lead1.x - 10, lead1.y + 5);
            ctx.drawRect(lead1.x - 10, lead1.y - 2, 5, 5);
        }
        if (lead2.y > lead1.y) {
            ctx.drawLine(lead1.x - 3, lead1.y + 5, lead1.x - 3, lead1.y + 12);
            ctx.drawLine(lead1.x + 2, lead1.y + 7, lead1.x + 2, lead1.y + 14);
            ctx.drawRect(lead1.x - 3, lead1.y + 7, 5, 5);
        }
        if (lead2.y < lead1.y) {
            ctx.drawLine(lead1.x - 3, lead1.y - 12, lead1.x - 3, lead1.y - 5);
            ctx.drawLine(lead1.x + 2, lead1.y - 10, lead1.x + 2, lead1.y - 3);
            ctx.drawRect(lead1.x - 3, lead1.y - 10, 5, 5);
        }
    }

    @Override
    public void setPoints() {
        super.setPoints();
        int hs = 16;
        int ww = 16;
        if (ww > dn / 2)
            ww = (int) (dn / 2);
        lead1 = interpPoint(point1, point2, .5 - ww / dn);
        lead2 = interpPoint(point1, point2, .5 + (ww + 2) / dn);
        pcircle = interpPoint(point1, point2, .5 + (ww - 2) / dn);
        Point triPoints[] = newPointArray(3);
        interpPoint2(lead1, lead2, triPoints[0], triPoints[1], 0, hs);
        triPoints[2] = interpPoint(point1, point2, .5 + (ww - 5) / dn);
        gatePoly = createPolygon(triPoints);
        setBbox(point1, point2, hs);
    }

    @Override
    public int getVoltageSourceCount() {
        return 1;
    }

    @Override
    public void stamp() {
        engine.stampVoltageSource(0, nodes[1], voltSource);
    }

    @Override
    public void doStep() {
        double v0 = volts[1];
        double out = 0;
        if (volts[0] < 1.65) {
            out = 5;
        }
        if (volts[0] > 3.32) {
            out = 0;
        }
        if (volts[0] > 1.65 && volts[0] < 3.32) {
            if (v0 == 0) out = 0;
            if (v0 == 5) out = 5;
        }
        double maxStep = slewRate * engine.timeStep * 1e9;
        out = Math.max(Math.min(v0 + maxStep, out), v0 - maxStep);
        engine.updateVoltageSource(voltSource, out);
    }

    @Override
    public double getVoltageDiff() {
        return volts[0];
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "schmitt trigger inverter";
        arr[1] = "Vi = " + ctx.getVoltageText(volts[0]);
        arr[2] = "Vo = " + ctx.getVoltageText(volts[1]);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Slew Rate (V/ns)", slewRate, 0, 0);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        slewRate = ei.getValue();
    }

    // there is no current path through the inverter input, but there
    // is an indirect path through the output to ground.
    @Override
    public boolean getConnection(int n1, int n2) {
        return false;
    }

    @Override
    public boolean hasGroundConnection(int n1) {
        return (n1 == 1);
    }
}
