package org.circuitsymphony.element.passive;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class TappedTransformerElm extends CircuitElm {
    private final double[] current;
    private final double[] curcount;
    private double inductance;
    private double ratio;
    private Point[] ptEnds;
    private Point[] ptCoil;
    private Point[] ptCore;
    private double[] a;
    private double[] curSourceValue;
    private double[] voltdiff;

    public TappedTransformerElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        inductance = 4;
        ratio = 1;
        noDiagonal = true;
        current = new double[4];
        curcount = new double[4];
    }

    public TappedTransformerElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                                StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public TappedTransformerElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                                StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        inductance = new Double(st.nextToken());
        ratio = new Double(st.nextToken());
        current = new double[4];
        curcount = new double[4];
        current[0] = new Double(st.nextToken());
        current[1] = new Double(st.nextToken());
        try {
            current[2] = new Double(st.nextToken());
        } catch (Exception e) {
        }
        noDiagonal = true;
    }

    @Override
    public int getDumpType() {
        return 169;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + inductance + " " + ratio + " " +
                current[0] + " " + current[1] + " " + current[2];
    }

    @Override
    public void draw(DrawContext ctx) {
        int i;
        for (i = 0; i != 5; i++) {
            setVoltageColor(ctx, volts[i]);
            ctx.drawThickLine(ptEnds[i], ptCoil[i]);
        }
        for (i = 0; i != 4; i++) {
            if (i == 1)
                continue;
            setPowerColor(ctx, current[i] * (volts[i] - volts[i + 1]));
            drawCoil(ctx, i > 1 ? -6 : 6,
                    ptCoil[i], ptCoil[i + 1], volts[i], volts[i + 1]);
        }
        ctx.setColor(needsHighlight(ctx) ? ctx.selectColor : ctx.lightGrayColor);
        for (i = 0; i != 4; i += 2) {
            ctx.drawThickLine(ptCore[i], ptCore[i + 1]);
        }
        // calc current of tap wire
        current[3] = current[1] - current[2];
        for (i = 0; i != 4; i++)
            curcount[i] = updateDotCount(ctx, current[i], curcount[i]);

        // primary dots
        drawDots(ctx, ptEnds[0], ptCoil[0], curcount[0]);
        drawDots(ctx, ptCoil[0], ptCoil[1], curcount[0]);
        drawDots(ctx, ptCoil[1], ptEnds[1], curcount[0]);

        // secondary dots
        drawDots(ctx, ptEnds[2], ptCoil[2], curcount[1]);
        drawDots(ctx, ptCoil[2], ptCoil[3], curcount[1]);
        drawDots(ctx, ptCoil[3], ptEnds[3], curcount[3]);
        drawDots(ctx, ptCoil[3], ptCoil[4], curcount[2]);
        drawDots(ctx, ptCoil[4], ptEnds[4], curcount[2]);

        drawPosts(ctx);
        setBbox(ptEnds[0], ptEnds[4], 0);
    }

    @Override
    public void setPoints() {
        super.setPoints();
        int hs = 32;
        ptEnds = newPointArray(5);
        ptCoil = newPointArray(5);
        ptCore = newPointArray(4);
        ptEnds[0] = point1;
        ptEnds[2] = point2;
        interpPoint(point1, point2, ptEnds[1], 0, -hs * 2);
        interpPoint(point1, point2, ptEnds[3], 1, -hs);
        interpPoint(point1, point2, ptEnds[4], 1, -hs * 2);
        double ce = .5 - 12 / dn;
        double cd = .5 - 2 / dn;
        int i;
        interpPoint(ptEnds[0], ptEnds[2], ptCoil[0], ce);
        interpPoint(ptEnds[0], ptEnds[2], ptCoil[1], ce, -hs * 2);
        interpPoint(ptEnds[0], ptEnds[2], ptCoil[2], 1 - ce);
        interpPoint(ptEnds[0], ptEnds[2], ptCoil[3], 1 - ce, -hs);
        interpPoint(ptEnds[0], ptEnds[2], ptCoil[4], 1 - ce, -hs * 2);
        for (i = 0; i != 2; i++) {
            int b = -hs * i * 2;
            interpPoint(ptEnds[0], ptEnds[2], ptCore[i], cd, b);
            interpPoint(ptEnds[0], ptEnds[2], ptCore[i + 2], 1 - cd, b);
        }
    }

    @Override
    public Point getPost(int n) {
        return ptEnds[n];
    }

    @Override
    public int getPostCount() {
        return 5;
    }

    @Override
    public void reset() {
        current[0] = current[1] = volts[0] = volts[1] = volts[2] =
                volts[3] = curcount[0] = curcount[1] = 0;
    }

    @Override
    public void stamp() {
        // equations for transformer:
        //   v1 = L1 di1/dt + M1 di2/dt + M1 di3/dt
        //   v2 = M1 di1/dt + L2 di2/dt + M2 di3/dt
        //   v3 = M1 di1/dt + M2 di2/dt + L2 di3/dt
        // we invert that to get:
        //   di1/dt = a1 v1 + a2 v2 + a3 v3
        //   di2/dt = a4 v1 + a5 v2 + a6 v3
        //   di3/dt = a7 v1 + a8 v2 + a9 v3
        // integrate di1/dt using trapezoidal approx and we get:
        //   i1(t2) = i1(t1) + dt/2 (i1(t1) + i1(t2))
        //          = i1(t1) + a1 dt/2 v1(t1)+a2 dt/2 v2(t1)+a3 dt/2 v3(t3) +
        //                     a1 dt/2 v1(t2)+a2 dt/2 v2(t2)+a3 dt/2 v3(t3)
        // the norton equivalent of this for i1 is:
        //  a. current source, I = i1(t1) + a1 dt/2 v1(t1) + a2 dt/2 v2(t1)
        //                                + a3 dt/2 v3(t1)
        //  b. resistor, G = a1 dt/2
        //  c. current source controlled by voltage v2, G = a2 dt/2
        //  d. current source controlled by voltage v3, G = a3 dt/2
        // and similarly for i2
        //
        // first winding goes from node 0 to 1, second is from 2 to 3 to 4
        double l1 = inductance;
        // second winding is split in half, so each part has half the turns;
        // we square the 1/2 to divide by 4
        double cc = .99;
        //double m1 = .999*Math.sqrt(l1*l2);
        // mutual inductance between two halves of the second winding
        // is equal to self-inductance of either half (slightly less
        // because the coupling is not perfect)
        //double m2 = .999*l2;
        a = new double[9];
        // load pre-inverted matrix
        a[0] = (1 + cc) / (l1 * (1 + cc - 2 * cc * cc));
        a[1] = a[2] = a[3] = a[6] = 2 * cc / ((2 * cc * cc - cc - 1) * inductance * ratio);
        a[4] = a[8] = -4 * (1 + cc) / ((2 * cc * cc - cc - 1) * l1 * ratio * ratio);
        a[5] = a[7] = 4 * cc / ((2 * cc * cc - cc - 1) * l1 * ratio * ratio);
        int i;
        for (i = 0; i != 9; i++)
            a[i] *= engine.timeStep / 2;
        engine.stampConductance(nodes[0], nodes[1], a[0]);
        engine.stampVCCurrentSource(nodes[0], nodes[1], nodes[2], nodes[3], a[1]);
        engine.stampVCCurrentSource(nodes[0], nodes[1], nodes[3], nodes[4], a[2]);

        engine.stampVCCurrentSource(nodes[2], nodes[3], nodes[0], nodes[1], a[3]);
        engine.stampConductance(nodes[2], nodes[3], a[4]);
        engine.stampVCCurrentSource(nodes[2], nodes[3], nodes[3], nodes[4], a[5]);

        engine.stampVCCurrentSource(nodes[3], nodes[4], nodes[0], nodes[1], a[6]);
        engine.stampVCCurrentSource(nodes[3], nodes[4], nodes[2], nodes[3], a[7]);
        engine.stampConductance(nodes[3], nodes[4], a[8]);

        for (i = 0; i != 5; i++)
            engine.stampRightSide(nodes[i]);
        voltdiff = new double[3];
        curSourceValue = new double[3];
    }

    @Override
    public void startIteration() {
        voltdiff[0] = volts[0] - volts[1];
        voltdiff[1] = volts[2] - volts[3];
        voltdiff[2] = volts[3] - volts[4];
        int i, j;
        for (i = 0; i != 3; i++) {
            curSourceValue[i] = current[i];
            for (j = 0; j != 3; j++)
                curSourceValue[i] += a[i * 3 + j] * voltdiff[j];
        }
    }

    @Override
    public void doStep() {
        engine.stampCurrentSource(nodes[0], nodes[1], curSourceValue[0]);
        engine.stampCurrentSource(nodes[2], nodes[3], curSourceValue[1]);
        engine.stampCurrentSource(nodes[3], nodes[4], curSourceValue[2]);
    }

    @Override
    public void calculateCurrent() {
        voltdiff[0] = volts[0] - volts[1];
        voltdiff[1] = volts[2] - volts[3];
        voltdiff[2] = volts[3] - volts[4];
        int i, j;
        for (i = 0; i != 3; i++) {
            current[i] = curSourceValue[i];
            for (j = 0; j != 3; j++)
                current[i] += a[i * 3 + j] * voltdiff[j];
        }
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "transformer";
        arr[1] = "L = " + ctx.getUnitText(inductance, "H");
        arr[2] = "Ratio = " + ratio;
        //arr[3] = "I1 = " + getCurrentText(current1);
        arr[3] = "Vd1 = " + ctx.getVoltageText(volts[0] - volts[2]);
        //arr[5] = "I2 = " + getCurrentText(current2);
        arr[4] = "Vd2 = " + ctx.getVoltageText(volts[1] - volts[3]);
    }

    @Override
    public boolean getConnection(int n1, int n2) {
        return comparePair(n1, n2, 0, 1) || comparePair(n1, n2, 2, 3) || comparePair(n1, n2, 3, 4) || comparePair(n1, n2, 2, 4);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Primary Inductance (H)", inductance, .01, 5);
        if (n == 1)
            return new EditInfo("Ratio", ratio, 1, 10).setDimensionless();
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            inductance = ei.getValue();
        if (n == 1)
            ratio = ei.getValue();
    }
}
