package org.circuitsymphony.element.active;

import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;

import java.awt.*;
import java.util.StringTokenizer;

public class AnalogSwitch2Elm extends AnalogSwitchElm {
    private final int openhs = 16;
    private Point[] swposts;
    private Point[] swpoles;
    private Point ctlPoint;

    public AnalogSwitch2Elm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public AnalogSwitch2Elm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                            StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, st);
    }

    public AnalogSwitch2Elm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                            StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(32);
        swposts = newPointArray(2);
        swpoles = newPointArray(2);
        interpPoint2(lead1, lead2, swpoles[0], swpoles[1], 1, openhs);
        interpPoint2(point1, point2, swposts[0], swposts[1], 1, openhs);
        ctlPoint = interpPoint(point1, point2, .5, openhs);
    }

    @Override
    public int getPostCount() {
        return 4;
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
        ctx.setColor(ctx.lightGrayColor);
        int position = (open) ? 1 : 0;
        ctx.drawThickLine(lead1, swpoles[position]);

        updateDotCount(ctx);
        drawDots(ctx, point1, lead1, curcount);
        drawDots(ctx, swpoles[position], swposts[position], curcount);
        drawPosts(ctx);
    }

    @Override
    public Point getPost(int n) {
        return (n == 0) ? point1 : (n == 3) ? ctlPoint : swposts[n - 1];
    }

    @Override
    public int getDumpType() {
        return 160;
    }

    @Override
    public void calculateCurrent() {
        if (open)
            current = (volts[0] - volts[2]) / r_on;
        else
            current = (volts[0] - volts[1]) / r_on;
    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[0]);
        engine.stampNonLinear(nodes[1]);
        engine.stampNonLinear(nodes[2]);
    }

    @Override
    public void doStep() {
        open = (volts[3] < 2.5);
        if ((flags & FLAG_INVERT) != 0)
            open = !open;
        if (open) {
            engine.stampResistor(nodes[0], nodes[2], r_on);
            engine.stampResistor(nodes[0], nodes[1], r_off);
        } else {
            engine.stampResistor(nodes[0], nodes[1], r_on);
            engine.stampResistor(nodes[0], nodes[2], r_off);
        }
    }

    @Override
    public boolean getConnection(int n1, int n2) {
        return !(n1 == 3 || n2 == 3);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "analog switch (SPDT)";
        arr[1] = "I = " + ctx.getCurrentDText(getCurrent());
    }
}

