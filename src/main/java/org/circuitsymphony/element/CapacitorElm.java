package org.circuitsymphony.element;

import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class CapacitorElm extends CircuitElm {
    private static final int FLAG_BACK_EULER = 2;
    private double capacitance;
    private double compResistance;
    private double voltdiff;
    private Point[] plate1;
    private Point[] plate2;
    private double curSourceValue;
    private int hs = 12;

    public CapacitorElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        capacitance = 1e-5;
    }

    public CapacitorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                        StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public CapacitorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                        StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        capacitance = new Double(st.nextToken());
        voltdiff = new Double(st.nextToken());
    }

    private boolean isTrapezoidal() {
        return (flags & FLAG_BACK_EULER) == 0;
    }

    @Override
    public void setNodeVoltage(int n, double c) {
        super.setNodeVoltage(n, c);
        voltdiff = volts[0] - volts[1];
    }

    @Override
    public void reset() {
        current = curcount = 0;
        // put small charge on caps when reset to start oscillators
        voltdiff = 1e-3;
    }

    @Override
    public int getDumpType() {
        return 'c';
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + capacitance + " " + voltdiff;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        double f = (dn / 2 - 4) / dn;
        // calc leads
        lead1 = interpPoint(point1, point2, f);
        lead2 = interpPoint(point1, point2, 1 - f);
        // calc plates
        plate1 = newPointArray(2);
        plate2 = newPointArray(2);
        interpPoint2(point1, point2, plate1[0], plate1[1], f, 12);
        interpPoint2(point1, point2, plate2[0], plate2[1], 1 - f, 12);
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(point1, point2, hs);

        // draw first lead and plate
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(point1, lead1);
        setPowerColor(ctx, false);
        ctx.drawThickLine(plate1[0], plate1[1]);
//        if (sim.powerCheckItem.getState())
//            g.setColor(Color.gray);

        // draw second lead and plate
        setVoltageColor(ctx, volts[1]);
        ctx.drawThickLine(point2, lead2);
        setPowerColor(ctx, false);
        ctx.drawThickLine(plate2[0], plate2[1]);

        updateDotCount(ctx);
        if (ctx.getSim().dragElm != this) {
            drawDots(ctx, point1, lead1, curcount);
            drawDots(ctx, point2, lead2, -curcount);
        }
        drawPosts(ctx);
        if (ctx.getSim().getShowValuesCheckItem().getState()) {
            String s = ctx.getShortUnitText(capacitance, "F");
            drawValues(ctx, s, hs);
        }
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
    public void stamp() {
        // capacitor companion model using trapezoidal approximation
        // (Norton equivalent) consists of a current source in
        // parallel with a resistor.  Trapezoidal is more accurate
        // than backward euler but can cause oscillatory behavior
        // if RC is small relative to the timestep.
        if (isTrapezoidal())
            compResistance = engine.timeStep / (2 * capacitance);
        else
            compResistance = engine.timeStep / capacitance;
        engine.stampResistor(nodes[0], nodes[1], compResistance);
        engine.stampRightSide(nodes[0]);
        engine.stampRightSide(nodes[1]);
    }

    @Override
    public void startIteration() {
        if (isTrapezoidal())
            curSourceValue = -voltdiff / compResistance - current;
        else
            curSourceValue = -voltdiff / compResistance;
        //System.out.println("cap " + compResistance + " " + curSourceValue + " " + current + " " + voltdiff);
    }

    @Override
    public void calculateCurrent() {
        double voltdiff = volts[0] - volts[1];
        // we check compResistance because this might get called
        // before stamp(), which sets compResistance, causing
        // infinite current
        if (compResistance > 0)
            current = voltdiff / compResistance + curSourceValue;
    }

    @Override
    public void doStep() {
        engine.stampCurrentSource(nodes[0], nodes[1], curSourceValue);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "capacitor";
        getBasicInfo(ctx, arr);
        arr[3] = "C = " + ctx.getUnitText(capacitance, "F");
        arr[4] = "P = " + ctx.getUnitText(getPower(), "W");
        //double v = getVoltageDiff();
        //arr[4] = "U = " + getUnitText(.5*capacitance*v*v, "J");
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Capacitance (F)", capacitance, 0, 0);
        if (n == 1) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Trapezoidal Approximation", isTrapezoidal()));
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0 && ei.getValue() > 0)
            capacitance = ei.getValue();
        if (n == 1) {
            if (ei.getCheckbox().getState())
                flags &= ~FLAG_BACK_EULER;
            else
                flags |= FLAG_BACK_EULER;
        }
    }

    @Override
    public boolean needsShortcut() {
        return true;
    }

    public double getCapacitance() {
        return capacitance;
    }

    public void setCapacitance(double capacitance) {
        this.capacitance = capacitance;
    }
}
