package org.circuitsymphony.element.active;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class ResistorElm extends CircuitElm {
    private double resistance;
    private Point ps3;
    private Point ps4;
    private int hs = 8;

    public ResistorElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        resistance = 100;
    }

    public ResistorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                       StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public ResistorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                       StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        resistance = new Double(st.nextToken());
    }

    @Override
    public int getDumpType() {
        return 'r';
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + resistance;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(32);
        ps3 = new Point();
        ps4 = new Point();
    }

    @Override
    public void draw(DrawContext ctx) {
        int segments = 16;
        int i;
        int ox = 0;
        hs = ctx.getSim().getEuroResistorCheckItem().getState() ? 6 : 8;
        double v1 = volts[0];
        double v2 = volts[1];
        setBbox(point1, point2, hs);
        draw2Leads(ctx);
        setPowerColor(ctx, true);
        double segf = 1. / segments;
        if (!ctx.getSim().getEuroResistorCheckItem().getState()) {
            // draw zigzag
            for (i = 0; i != segments; i++) {
                int nx = 0;
                switch (i & 3) {
                    case 0:
                        nx = 1;
                        break;
                    case 2:
                        nx = -1;
                        break;
                    default:
                        nx = 0;
                        break;
                }
                double v = v1 + (v2 - v1) * i / segments;
                setVoltageColor(ctx, v);
                interpPoint(lead1, lead2, ps1, i * segf, hs * ox);
                interpPoint(lead1, lead2, ps2, (i + 1) * segf, hs * nx);
                ctx.drawThickLine(ps1, ps2);
                ox = nx;
            }
        } else {
            // draw rectangle
            setVoltageColor(ctx, v1);
            interpPoint2(lead1, lead2, ps1, ps2, 0, hs);
            ctx.drawThickLine(ps1, ps2);
            for (i = 0; i != segments; i++) {
                double v = v1 + (v2 - v1) * i / segments;
                setVoltageColor(ctx, v);
                interpPoint2(lead1, lead2, ps1, ps2, i * segf, hs);
                interpPoint2(lead1, lead2, ps3, ps4, (i + 1) * segf, hs);
                ctx.drawThickLine(ps1, ps3);
                ctx.drawThickLine(ps2, ps4);
            }
            interpPoint2(lead1, lead2, ps1, ps2, 1, hs);
            ctx.drawThickLine(ps1, ps2);
        }
        if (ctx.getSim().getShowValuesCheckItem().getState()) {
            String s = ctx.getShortUnitText(resistance, "");
            drawValues(ctx, s, hs);
        }
        doDots(ctx);
        drawPosts(ctx);
    }

    @Override
    public boolean isBasicBoundingBoxSupported() {
        return true;
    }

    @Override
    public void getBasicBoundingBox(Point tempP1, Point tempP2, Rectangle result) {
        setBbox(result, tempP1, tempP2, hs);
    }

    @Override
    public void calculateCurrent() {
        current = (volts[0] - volts[1]) / resistance;
        //System.out.print(this + " res current set to " + current + "\n");
    }

    @Override
    public void stamp() {
        engine.stampResistor(nodes[0], nodes[1], resistance);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "resistor";
        getBasicInfo(ctx, arr);
        arr[3] = "R = " + ctx.getUnitText(resistance, CirSim.ohmString);
        arr[4] = "P = " + ctx.getUnitText(getPower(), "W");
    }

    @Override
    public EditInfo getEditInfo(int n) {
        // ohmString doesn't work here on linux
        if (n == 0)
            return new EditInfo("Resistance (ohms)", resistance, 0, 0);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (ei.getValue() > 0)
            resistance = ei.getValue();
    }

    @Override
    public boolean needsShortcut() {
        return true;
    }

    public double getResistance() {
        return resistance;
    }

    public void setResistance(double resistance) {
        this.resistance = resistance;
    }
}
