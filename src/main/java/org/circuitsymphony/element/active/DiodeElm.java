package org.circuitsymphony.element.active;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;
import org.circuitsymphony.util.Diode;

import java.awt.*;
import java.util.StringTokenizer;

public class DiodeElm extends CircuitElm {

    protected static final int FLAG_FWDROP = 1;
    private final double defaultdrop = .805904783;
    private final int hs = 8;
    protected double fwdrop, zvoltage;
    protected Diode diode;
    private Polygon poly;
    private Point[] cathode;

    public DiodeElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        diode = new Diode(engine);
        fwdrop = defaultdrop;
        zvoltage = 0;
        setup();
    }

    public DiodeElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                    StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public DiodeElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                    StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        diode = new Diode(engine);
        fwdrop = defaultdrop;
        zvoltage = 0;
        if ((f & FLAG_FWDROP) > 0) {
            try {
                fwdrop = new Double(st.nextToken());
            } catch (Exception e) {
            }
        }
        setup();
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    public void setup() {
        diode.setup(fwdrop, zvoltage);
    }

    @Override
    public int getDumpType() {
        return 'd';
    }

    @Override
    public String dump(boolean newFormat) {
        flags |= FLAG_FWDROP;
        return super.dump(newFormat) + " " + fwdrop;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(16);
        cathode = newPointArray(2);
        Point pa[] = newPointArray(2);
        interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
        interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
        poly = createPolygon(pa[0], pa[1], lead2);
    }

    @Override
    public void draw(DrawContext ctx) {
        drawDiode(ctx);
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
        diode.reset();
        volts[0] = volts[1] = curcount = 0;
    }

    private void drawDiode(DrawContext ctx) {
        setBbox(point1, point2, hs);

        double v1 = volts[0];
        double v2 = volts[1];

        draw2Leads(ctx);

        // draw arrow thingy
        setPowerColor(ctx, true);
        setVoltageColor(ctx, v1);
        ctx.fillPolygon(poly);

        // draw thing arrow is pointing to
        setVoltageColor(ctx, v2);
        ctx.drawThickLine(cathode[0], cathode[1]);
    }

    @Override
    public void stamp() {
        diode.stamp(nodes[0], nodes[1]);
    }

    @Override
    public void doStep() {
        diode.doStep(volts[0] - volts[1]);
    }

    @Override
    public void calculateCurrent() {
        current = diode.calculateCurrent(volts[0] - volts[1]);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "diode";
        arr[1] = "I = " + ctx.getCurrentText(getCurrent());
        arr[2] = "Vd = " + ctx.getVoltageText(getVoltageDiff());
        arr[3] = "P = " + ctx.getUnitText(getPower(), "W");
        arr[4] = "Vf = " + ctx.getVoltageText(fwdrop);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Fwd Voltage @ 1A", fwdrop, 10, 1000);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        fwdrop = ei.getValue();
        setup();
    }

    @Override
    public boolean needsShortcut() {
        return getClass() == DiodeElm.class;
    }

    public double getFwdVoltage() {
        return fwdrop;
    }

    public void setFwdVoltage(double fwdrop) {
        this.fwdrop = fwdrop;
    }
}
