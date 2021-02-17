package org.circuitsymphony.element.passive;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;
import org.circuitsymphony.util.Inductor;

import java.awt.*;
import java.util.StringTokenizer;

public class InductorElm extends CircuitElm {
    private final Inductor ind;
    private double inductance;
    private int hs = 8;

    public InductorElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        ind = new Inductor(engine);
        inductance = 1;
        ind.setup(inductance, current, flags);
    }

    public InductorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                       StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public InductorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                       StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        ind = new Inductor(engine);
        inductance = new Double(st.nextToken());
        current = new Double(st.nextToken());
        ind.setup(inductance, current, flags);
    }

    @Override
    public int getDumpType() {
        return 'l';
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + inductance + " " + current;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(32);
    }

    @Override
    public void draw(DrawContext ctx) {
        double v1 = volts[0];
        double v2 = volts[1];
        setBbox(point1, point2, hs);
        draw2Leads(ctx);
        setPowerColor(ctx, false);
        drawCoil(ctx, 8, lead1, lead2, v1, v2);
        if (ctx.getSim().getShowValuesCheckItem().getState()) {
            String s = ctx.getShortUnitText(inductance, "H");
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
    public void reset() {
        current = volts[0] = volts[1] = curcount = 0;
        ind.reset();
    }

    @Override
    public void stamp() {
        ind.stamp(nodes[0], nodes[1]);
    }

    @Override
    public void startIteration() {
        ind.startIteration(volts[0] - volts[1]);
    }

    @Override
    public boolean nonLinear() {
        return ind.nonLinear();
    }

    @Override
    public void calculateCurrent() {
        double voltdiff = volts[0] - volts[1];
        current = ind.calculateCurrent(voltdiff);
    }

    @Override
    public void doStep() {
        ind.doStep();
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "inductor";
        getBasicInfo(ctx, arr);
        arr[3] = "L = " + ctx.getUnitText(inductance, "H");
        arr[4] = "P = " + ctx.getUnitText(getPower(), "W");
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Inductance (H)", inductance, 0, 0);
        if (n == 1) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Trapezoidal Approximation", ind.isTrapezoidal()));
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            inductance = ei.getValue();
        if (n == 1) {
            if (ei.getCheckbox().getState())
                flags &= ~Inductor.FLAG_BACK_EULER;
            else
                flags |= Inductor.FLAG_BACK_EULER;
        }
        ind.setup(inductance, current, flags);
    }

    public void setInductance(double inductance) {
        this.inductance = inductance;
    }

    public double getInductance() {
        return inductance;
    }
}
