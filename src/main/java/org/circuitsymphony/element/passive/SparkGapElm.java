package org.circuitsymphony.element.passive;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class SparkGapElm extends CircuitElm {
    private double resistance;
    private double onresistance;
    private double offresistance;
    private double breakdown;
    private double holdcurrent;
    private boolean state;
    private Polygon arrow1;
    private Polygon arrow2;
    private int hs = 8;

    public SparkGapElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        offresistance = 1e9;
        onresistance = 1e3;
        breakdown = 1e3;
        holdcurrent = 0.001;
        state = false;
    }

    public SparkGapElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                       StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public SparkGapElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                       StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        onresistance = new Double(st.nextToken());
        offresistance = new Double(st.nextToken());
        breakdown = new Double(st.nextToken());
        holdcurrent = new Double(st.nextToken());
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public int getDumpType() {
        return 187;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + onresistance + " " + offresistance + " "
                + breakdown + " " + holdcurrent;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        int dist = 16;
        int alen = 8;
        calcLeads(dist + alen);
        Point p1 = interpPoint(point1, point2, (dn - alen) / (2 * dn));
        arrow1 = calcArrow(point1, p1, alen, alen);
        p1 = interpPoint(point1, point2, (dn + alen) / (2 * dn));
        arrow2 = calcArrow(point2, p1, alen, alen);
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(point1, point2, hs);
        draw2Leads(ctx);
        setPowerColor(ctx, true);
        setVoltageColor(ctx, volts[0]);
        ctx.fillPolygon(arrow1);
        setVoltageColor(ctx, volts[1]);
        ctx.fillPolygon(arrow2);
        if (state)
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
        double vd = volts[0] - volts[1];
        current = vd / resistance;
    }

    @Override
    public void reset() {
        super.reset();
        state = false;
    }

    @Override
    public void startIteration() {
        if (Math.abs(current) < holdcurrent)
            state = false;
        double vd = volts[0] - volts[1];
        if (Math.abs(vd) > breakdown)
            state = true;
    }

    @Override
    public void doStep() {
        resistance = (state) ? onresistance : offresistance;
        engine.stampResistor(nodes[0], nodes[1], resistance);
    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[0]);
        engine.stampNonLinear(nodes[1]);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "spark gap";
        getBasicInfo(ctx, arr);
        arr[3] = state ? "on" : "off";
        arr[4] = "Ron = " + ctx.getUnitText(onresistance, CirSim.ohmString);
        arr[5] = "Roff = " + ctx.getUnitText(offresistance, CirSim.ohmString);
        arr[6] = "Vbreakdown = " + ctx.getUnitText(breakdown, "V");
    }

    @Override
    public EditInfo getEditInfo(int n) {
        // ohmString doesn't work here on linux
        if (n == 0)
            return new EditInfo("On resistance (ohms)", onresistance, 0, 0);
        if (n == 1)
            return new EditInfo("Off resistance (ohms)", offresistance, 0, 0);
        if (n == 2)
            return new EditInfo("Breakdown voltage", breakdown, 0, 0);
        if (n == 3)
            return new EditInfo("Holding current (A)", holdcurrent, 0, 0);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (ei.getValue() > 0 && n == 0)
            onresistance = ei.getValue();
        if (ei.getValue() > 0 && n == 1)
            offresistance = ei.getValue();
        if (ei.getValue() > 0 && n == 2)
            breakdown = ei.getValue();
        if (ei.getValue() > 0 && n == 3)
            holdcurrent = ei.getValue();
    }

    @Override
    public boolean needsShortcut() {
        return false;
    }
}

