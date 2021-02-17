package org.circuitsymphony.element.io;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class SweepElm extends CircuitElm {
    private final int FLAG_LOG = 1;
    private final int FLAG_BIDIR = 2;
    private final int circleSize = 17;
    private double maxV;
    private double maxF;
    private double minF;
    private double sweepTime;
    private double frequency;
    private double fadd;
    private double fmul;
    private double freqTime;
    private double savedTimeStep;
    private int dir = 1;
    private double v;

    public SweepElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        minF = 20;
        maxF = 4000;
        maxV = 5;
        sweepTime = .1;
        flags = FLAG_BIDIR;
        reset();
    }

    public SweepElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public SweepElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2, StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        minF = new Double(st.nextToken());
        maxF = new Double(st.nextToken());
        maxV = new Double(st.nextToken());
        sweepTime = new Double(st.nextToken());
        reset();
    }

    @Override
    public int getDumpType() {
        return 170;
    }

    @Override
    public int getPostCount() {
        return 1;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + minF + " " + maxF + " " + maxV + " " +
                sweepTime;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        lead1 = interpPoint(point1, point2, 1 - circleSize / dn);
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(point1, point2, circleSize);
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(point1, lead1);
        ctx.setColor(needsHighlight(ctx) ? ctx.selectColor : Color.gray);
        setPowerColor(ctx, false);
        int xc = point2.x;
        int yc = point2.y;
        ctx.drawThickCircle(xc, yc, circleSize);
        int wl = 8;
        adjustBbox(xc - circleSize, yc - circleSize,
                xc + circleSize, yc + circleSize);
        int i;
        int xl = 10;
        int ox = -1, oy = -1;
        long tm = System.currentTimeMillis();
        //double w = (this == mouseElm ? 3 : 2);
        tm %= 2000;
        if (tm > 1000)
            tm = 2000 - tm;
        double w = 1 + tm * .002;
        if (!ctx.getSim().stoppedCheck.getState())
            w = 1 + 2 * (frequency - minF) / (maxF - minF);
        for (i = -xl; i <= xl; i++) {
            int yy = yc + (int) (.95 * Math.sin(i * PI * w / xl) * wl);
            if (ox != -1)
                ctx.drawThickLine(ox, oy, xc + i, yy);
            ox = xc + i;
            oy = yy;
        }

        if (ctx.getSim().getShowValuesCheckItem().getState()) {
            String s = ctx.getShortUnitText(frequency, "Hz");
            if (dx == 0 || dy == 0)
                drawValues(ctx, s, circleSize);
        }

        drawPosts(ctx);
        curcount = updateDotCount(ctx, -current, curcount);
        if (engine.getDraggedElement() != this)
            drawDots(ctx, point1, lead1, curcount);
    }

    @Override
    public boolean isBasicBoundingBoxSupported() {
        return true;
    }

    @Override
    public void getBasicBoundingBox(Point tempP1, Point tempP2, Rectangle result) {
        setBbox(result, tempP1, tempP2, circleSize);
        int xc = tempP2.x;
        int yc = tempP2.y;
        adjustBbox(result, xc - circleSize, yc - circleSize, xc + circleSize, yc + circleSize);
    }

    @Override
    public void stamp() {
        engine.stampVoltageSource(0, nodes[0], voltSource);
    }

    private void setParams() {
        if (frequency < minF || frequency > maxF) {
            frequency = minF;
            freqTime = 0;
            dir = 1;
        }
        if ((flags & FLAG_LOG) == 0) {
            fadd = dir * engine.timeStep * (maxF - minF) / sweepTime;
            fmul = 1;
        } else {
            fadd = 0;
            fmul = Math.pow(maxF / minF, dir * engine.timeStep / sweepTime);
        }
        savedTimeStep = engine.timeStep;
    }

    @Override
    public void reset() {
        frequency = minF;
        freqTime = 0;
        dir = 1;
        setParams();
    }

    @Override
    public void startIteration() {
        // has timestep been changed?
        if (engine.timeStep != savedTimeStep)
            setParams();
        v = Math.sin(freqTime) * maxV;
        freqTime += frequency * 2 * PI * engine.timeStep;
        frequency = frequency * fmul + fadd;
        if (frequency >= maxF && dir == 1) {
            if ((flags & FLAG_BIDIR) != 0) {
                fadd = -fadd;
                fmul = 1 / fmul;
                dir = -1;
            } else
                frequency = minF;
        }
        if (frequency <= minF && dir == -1) {
            fadd = -fadd;
            fmul = 1 / fmul;
            dir = 1;
        }
    }

    @Override
    public void doStep() {
        engine.updateVoltageSource(voltSource, v);
    }

    @Override
    public double getVoltageDiff() {
        return volts[0];
    }

    @Override
    public int getVoltageSourceCount() {
        return 1;
    }

    @Override
    public boolean hasGroundConnection(int n1) {
        return true;
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "sweep " + (((flags & FLAG_LOG) == 0) ? "(linear)" : "(log)");
        arr[1] = "I = " + ctx.getCurrentDText(getCurrent());
        arr[2] = "V = " + ctx.getVoltageText(volts[0]);
        arr[3] = "f = " + ctx.getUnitText(frequency, "Hz");
        arr[4] = "range = " + ctx.getUnitText(minF, "Hz") + " " + ctx.getUnitText(maxF, "Hz");
        arr[5] = "time = " + ctx.getUnitText(sweepTime, "s");
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Min Frequency (Hz)", minF, 0, 0);
        if (n == 1)
            return new EditInfo("Max Frequency (Hz)", maxF, 0, 0);
        if (n == 2)
            return new EditInfo("Sweep Time (s)", sweepTime, 0, 0);
        if (n == 3) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Logarithmic", (flags & FLAG_LOG) != 0));
            return ei;
        }
        if (n == 4)
            return new EditInfo("Max Voltage", maxV, 0, 0);
        if (n == 5) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Bidirectional", (flags & FLAG_BIDIR) != 0));
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        double maxfreq = 1 / (8 * engine.timeStep);
        if (n == 0) {
            minF = ei.getValue();
            if (minF > maxfreq)
                minF = maxfreq;
        }
        if (n == 1) {
            maxF = ei.getValue();
            if (maxF > maxfreq)
                maxF = maxfreq;
        }
        if (n == 2)
            sweepTime = ei.getValue();
        if (n == 3) {
            flags &= ~FLAG_LOG;
            if (ei.getCheckbox().getState())
                flags |= FLAG_LOG;
        }
        if (n == 4)
            maxV = ei.getValue();
        if (n == 5) {
            flags &= ~FLAG_BIDIR;
            if (ei.getCheckbox().getState())
                flags |= FLAG_BIDIR;
        }
        setParams();

    }
}
    
