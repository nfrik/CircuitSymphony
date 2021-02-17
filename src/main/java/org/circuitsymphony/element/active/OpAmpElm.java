package org.circuitsymphony.element.active;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;
import org.circuitsymphony.util.MathUtil;

import java.awt.*;
import java.util.StringTokenizer;

public class OpAmpElm extends CircuitElm {
    public static final int FLAG_SWAP = 1;
    private final int FLAG_SMALL = 2;
    private final int FLAG_LOWGAIN = 4;
    private int opsize;
    private int opheight;
    private int opwidth;
    private double maxOut;
    private double minOut;
    private double gain;
    private double gbw;
    private Point[] in1p;
    private Point[] in2p;
    private Point[] textp;
    private Polygon triangle;
    private Font plusFont;
    private double lastvd;

    public OpAmpElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        noDiagonal = true;
        maxOut = 15;
        minOut = -15;
        gbw = 1e6;
        setSize(1);
        setGain();
    }

    public OpAmpElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                    StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public OpAmpElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                    StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        maxOut = 15;
        minOut = -15;
        // GBW has no effect in this version of the simulator, but we
        // retain it to keep the file format the same
        gbw = 1e6;
        try {
            maxOut = new Double(st.nextToken());
            minOut = new Double(st.nextToken());
            gbw = new Double(st.nextToken());
        } catch (Exception e) {
        }
        noDiagonal = true;
        setSize((f & FLAG_SMALL) != 0 ? 1 : 2);
        setGain();
    }

    private void setGain() {
        // gain of 100000 breaks e-amp-dfdx.txt
        // gain was 1000, but it broke amp-schmitt.txt
        gain = ((flags & FLAG_LOWGAIN) != 0) ? 1000 : 100000;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + maxOut + " " + minOut + " " + gbw;
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(point1, point2, opheight * 2);
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(in1p[0], in1p[1]);
        setVoltageColor(ctx, volts[1]);
        ctx.drawThickLine(in2p[0], in2p[1]);
        ctx.setColor(needsHighlight(ctx) ? ctx.selectColor : ctx.lightGrayColor);
        setPowerColor(ctx, true);
        ctx.drawThickPolygon(triangle);
        ctx.setFont(plusFont);
        drawCenteredText(ctx, "-", textp[0].x, textp[0].y - 2, true);
        drawCenteredText(ctx, "+", textp[1].x, textp[1].y, true);
        setVoltageColor(ctx, volts[2]);
        ctx.drawThickLine(lead2, point2);
        curcount = updateDotCount(ctx, current, curcount);
        drawDots(ctx, point2, lead2, curcount);
        drawPosts(ctx);
    }

    @Override
    public double getPower() {
        return volts[2] * current;
    }

    private void setSize(int s) {
        opsize = s;
        opheight = 8 * s;
        opwidth = 13 * s;
        flags = (flags & ~FLAG_SMALL) | ((s == 1) ? FLAG_SMALL : 0);
    }

    @Override
    public void setPoints() {
        super.setPoints();
        if (dn > 150 && this == engine.getDraggedElement())
            setSize(2);
        int ww = opwidth;
        if (ww > dn / 2)
            ww = (int) (dn / 2);
        calcLeads(ww * 2);
        int hs = opheight * dsign;
        if ((flags & FLAG_SWAP) != 0)
            hs = -hs;
        in1p = newPointArray(2);
        in2p = newPointArray(2);
        textp = newPointArray(2);
        interpPoint2(point1, point2, in1p[0], in2p[0], 0, hs);
        interpPoint2(lead1, lead2, in1p[1], in2p[1], 0, hs);
        interpPoint2(lead1, lead2, textp[0], textp[1], .2, hs);
        Point tris[] = newPointArray(2);
        interpPoint2(lead1, lead2, tris[0], tris[1], 0, hs * 2);
        triangle = createPolygon(tris[0], tris[1], lead2);
        plusFont = new Font("SansSerif", 0, opsize == 2 ? 14 : 10);
    }

    @Override
    public int getPostCount() {
        return 3;
    }

    @Override
    public Point getPost(int n) {
        return (n == 0) ? in1p[0] : (n == 1) ? in2p[0] : point2;
    }

    @Override
    public int getVoltageSourceCount() {
        return 1;
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "op-amp";
        arr[1] = "V+ = " + ctx.getVoltageText(volts[1]);
        arr[2] = "V- = " + ctx.getVoltageText(volts[0]);
        // sometimes the voltage goes slightly outside range, to make
        // convergence easier.  so we hide that here.
        double vo = Math.max(Math.min(volts[2], maxOut), minOut);
        arr[3] = "Vout = " + ctx.getVoltageText(vo);
        arr[4] = "Iout = " + ctx.getCurrentText(getCurrent());
        arr[5] = "range = " + ctx.getVoltageText(minOut) + " to " + ctx.getVoltageText(maxOut);
    }

    @Override
    public void stamp() {
        int vn = engine.nodeList.size() + voltSource;
        engine.stampNonLinear(vn);
        engine.stampMatrix(nodes[2], vn, 1);
    }

    @Override
    public void doStep() {
        double vd = volts[1] - volts[0];
        if (Math.abs(lastvd - vd) > .1)
            engine.converged = false;
        else if (volts[2] > maxOut + .1 || volts[2] < minOut - .1)
            engine.converged = false;
        double x = 0;
        int vn = engine.nodeList.size() + voltSource;
        double dx;
        if (vd >= maxOut / gain && (lastvd >= 0 || MathUtil.getrand(4) == 1)) {
            dx = 1e-4;
            x = maxOut - dx * maxOut / gain;
        } else if (vd <= minOut / gain && (lastvd <= 0 || MathUtil.getrand(4) == 1)) {
            dx = 1e-4;
            x = minOut - dx * minOut / gain;
        } else {
            dx = gain;
        }
        //System.out.println("opamp " + vd + " " + volts[2] + " " + dx + " "  + x + " " + lastvd + " " + sim.converged);

        // newton-raphson
        engine.stampMatrix(vn, nodes[0], dx);
        engine.stampMatrix(vn, nodes[1], -dx);
        engine.stampMatrix(vn, nodes[2], 1);
        engine.stampRightSide(vn, x);

        lastvd = vd;
        /*if (sim.converged)
          System.out.println((volts[1]-volts[0]) + " " + volts[2] + " " + initvd);*/
    }

    // there is no current path through the op-amp inputs, but there
    // is an indirect path through the output to ground.
    @Override
    public boolean getConnection(int n1, int n2) {
        return false;
    }

    @Override
    public boolean hasGroundConnection(int n1) {
        return (n1 == 2);
    }

    @Override
    public double getVoltageDiff() {
        return volts[2] - volts[1];
    }

    @Override
    public int getDumpType() {
        return 'a';
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Max Output (V)", maxOut, 1, 20);
        if (n == 1)
            return new EditInfo("Min Output (V)", minOut, -20, 0);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            maxOut = ei.getValue();
        if (n == 1)
            minOut = ei.getValue();
    }

    public double getMaxOut() {
        return maxOut;
    }

    public void setMaxOut(double maxOut) {
        this.maxOut = maxOut;
    }

    public double getMinOut() {
        return minOut;
    }

    public void setMinOut(double minOut) {
        this.minOut = minOut;
    }
}
