package org.circuitsymphony.element.io;

import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;

import java.awt.*;
import java.util.StringTokenizer;

public class RailElm extends VoltageElm {
    protected final int FLAG_CLOCK = 1;

    public RailElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, WF_DC);
    }

    public RailElm(CircuitEngine engine, int xx, int yy, int wf) {
        super(engine, xx, yy, wf);
    }

    public RailElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, st);
    }

    public RailElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2, StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public int getDumpType() {
        return 'R';
    }

    @Override
    public int getPostCount() {
        return 1;
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
        boolean clock = waveform == WF_SQUARE && (flags & FLAG_CLOCK) != 0;
        if (waveform == WF_DC || waveform == WF_VAR || clock) {
            Font f = new Font("SansSerif", 0, 12);
            ctx.setFont(f);
            ctx.setColor(needsHighlight(ctx) ? ctx.selectColor : ctx.whiteColor);
            setPowerColor(ctx, false);
            double v = getVoltage();
            String s = ctx.getShortUnitText(v, "V");
            if (Math.abs(v) < 1)
                s = ctx.getShowFormat().format(v) + "V";
            if (getVoltage() > 0)
                s = "+" + s;
            if (this instanceof AntennaElm)
                s = "Ant";
            if (clock)
                s = "CLK";
            drawCenteredText(ctx, s, x2, y2, true);
        } else {
            drawWaveform(ctx, point2);
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
        boolean clock = waveform == WF_SQUARE && (flags & FLAG_CLOCK) != 0;
        if (waveform != WF_DC && waveform != WF_VAR && !clock) {
            int xc = tempP2.x;
            int yc = tempP2.y;
            adjustBbox(result, xc - circleSize, yc - circleSize,
                    xc + circleSize, yc + circleSize);
        }
    }

    @Override
    public double getVoltageDiff() {
        return volts[0];
    }

    @Override
    public void stamp() {
        if (waveform == WF_DC)
            engine.stampVoltageSource(0, nodes[0], voltSource, getVoltage());
        else
            engine.stampVoltageSource(0, nodes[0], voltSource);
    }

    @Override
    public void doStep() {
        if (waveform != WF_DC)
            engine.updateVoltageSource(voltSource, getVoltage());
    }

    @Override
    public boolean hasGroundConnection(int n1) {
        return true;
    }
}
