package org.circuitsymphony.element.active;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class AnalogSwitchElm extends CircuitElm {
    public static final int FLAG_INVERT = 1;
    protected double r_on;
    protected double r_off;
    protected boolean open;
    protected double resistance;
    private Point ps;
    private Point point3;
    private Point lead3;

    public AnalogSwitchElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        r_on = 20;
        r_off = 1e10;
    }

    public AnalogSwitchElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                           StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public AnalogSwitchElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                           StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        r_on = 20;
        r_off = 1e10;
        try {
            r_on = new Double(st.nextToken());
            r_off = new Double(st.nextToken());
        } catch (Exception e) {
        }
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + r_on + " " + r_off;
    }

    @Override
    public int getDumpType() {
        return 159;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(32);
        ps = new Point();
        int openhs = 16;
        point3 = interpPoint(point1, point2, .5, -openhs);
        lead3 = interpPoint(point1, point2, .5, -openhs / 2);
    }

    @Override
    public void draw(DrawContext ctx) {
        int openhs = 16;
        int hs = (open) ? openhs : 0;
        setBbox(point1, point2, openhs);

        draw2Leads(ctx);

        ctx.setColor(ctx.lightGrayColor);
        interpPoint(lead1, lead2, ps, 1, hs);
        ctx.drawThickLine(lead1, ps);

        setVoltageColor(ctx, volts[2]);
        ctx.drawThickLine(point3, lead3);

        if (!open)
            doDots(ctx);
        drawPosts(ctx);
    }

    @Override
    public void calculateCurrent() {
        current = (volts[0] - volts[1]) / resistance;
    }

    // we need this to be able to change the matrix for each step
    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[0]);
        engine.stampNonLinear(nodes[1]);
    }

    @Override
    public void doStep() {
        open = (volts[2] < 2.5);
        if ((flags & FLAG_INVERT) != 0)
            open = !open;
        resistance = (open) ? r_off : r_on;
        engine.stampResistor(nodes[0], nodes[1], resistance);
    }

    @Override
    public void drag(CirSim sim, int xx, int yy) {
        xx = sim.snapGrid(xx);
        yy = sim.snapGrid(yy);
        if (abs(x - xx) < abs(y - yy))
            xx = x;
        else
            yy = y;
        int q1 = abs(x - xx) + abs(y - yy);
        int q2 = (q1 / 2) % sim.getGridSize();
        if (q2 != 0)
            return;
        x2 = xx;
        y2 = yy;
        setPoints();
    }

    @Override
    public int getPostCount() {
        return 3;
    }

    @Override
    public Point getPost(int n) {
        return (n == 0) ? point1 : (n == 1) ? point2 : point3;
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "analog switch";
        arr[1] = open ? "open" : "closed";
        arr[2] = "Vd = " + ctx.getVoltageDText(getVoltageDiff());
        arr[3] = "I = " + ctx.getCurrentDText(getCurrent());
        arr[4] = "Vc = " + ctx.getVoltageText(volts[2]);
    }

    // we have to just assume current will flow either way, even though that
    // might cause singular matrix errors
    @Override
    public boolean getConnection(int n1, int n2) {
        return !(n1 == 2 || n2 == 2);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Normally closed", (flags & FLAG_INVERT) != 0));
            return ei;
        }
        if (n == 1)
            return new EditInfo("On Resistance (ohms)", r_on, 0, 0);
        if (n == 2)
            return new EditInfo("Off Resistance (ohms)", r_off, 0, 0);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            flags = (ei.getCheckbox().getState()) ?
                    (flags | FLAG_INVERT) :
                    (flags & ~FLAG_INVERT);
        if (n == 1 && ei.getValue() > 0)
            r_on = ei.getValue();
        if (n == 2 && ei.getValue() > 0)
            r_off = ei.getValue();
    }
}

