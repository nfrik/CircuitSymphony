package org.circuitsymphony.element.active;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class DiacElm extends CircuitElm {
    private double onresistance;
    private double offresistance;
    private double breakdown;
    private double holdcurrent;
    private boolean state;
    private int hs = 6;

    public DiacElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        // FIXME need to adjust defaults to make sense for diac
        offresistance = 1e9;
        onresistance = 1e3;
        breakdown = 1e3;
        holdcurrent = 0.001;
        state = false;
    }

    public DiacElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                   StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public DiacElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
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
        return 185;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + onresistance + " " + offresistance + " "
                + breakdown + " " + holdcurrent;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(32);
    }

    @Override
    public void draw(DrawContext ctx) {
        // FIXME need to draw Diac
        setBbox(point1, point2, hs);
        draw2Leads(ctx);
        setPowerColor(ctx, true);
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
        if (state)
            current = vd / onresistance;
        else
            current = vd / offresistance;
    }

    @Override
    public void startIteration() {
        double vd = volts[0] - volts[1];
        if (Math.abs(current) < holdcurrent) state = false;
        if (Math.abs(vd) > breakdown) state = true;
        //System.out.print(this + " res current set to " + current + "\n");
    }

    @Override
    public void doStep() {
        if (state)
            engine.stampResistor(nodes[0], nodes[1], onresistance);
        else
            engine.stampResistor(nodes[0], nodes[1], offresistance);
    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[0]);
        engine.stampNonLinear(nodes[1]);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        // FIXME
        arr[0] = "spark gap";
        getBasicInfo(ctx, arr);
        arr[3] = state ? "on" : "off";
        arr[4] = "Ron = " + ctx.getUnitText(onresistance, CirSim.ohmString);
        arr[5] = "Roff = " + ctx.getUnitText(offresistance, CirSim.ohmString);
        arr[6] = "Vbrkdn = " + ctx.getUnitText(breakdown, "V");
        arr[7] = "Ihold = " + ctx.getUnitText(holdcurrent, "A");
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("On resistance (ohms)", onresistance, 0, 0);
        if (n == 1)
            return new EditInfo("Off resistance (ohms)", offresistance, 0, 0);
        if (n == 2)
            return new EditInfo("Breakdown voltage (volts)", breakdown, 0, 0);
        if (n == 3)
            return new EditInfo("Hold current (amps)", holdcurrent, 0, 0);
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

