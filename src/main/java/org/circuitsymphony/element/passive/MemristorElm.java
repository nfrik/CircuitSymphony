package org.circuitsymphony.element.passive;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class MemristorElm extends CircuitElm {
    private double r_on;
    private double r_off;
    private double dopeWidth;
    private double totalWidth;
    private double mobility;
    private double resistance;
    private int hs;

    public MemristorElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        r_on = 100;
        r_off = 160 * r_on;
        dopeWidth = 0;
        totalWidth = 10e-9; // meters
        mobility = 1e-10;   // m^2/sV
        resistance = 100;
    }

    public MemristorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                        StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public MemristorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                        StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        r_on = new Double(st.nextToken());
        r_off = new Double(st.nextToken());
        dopeWidth = new Double(st.nextToken());
        totalWidth = new Double(st.nextToken());
        mobility = new Double(st.nextToken());
        resistance = 100;
    }

    @Override
    public int getDumpType() {
        return 'm';
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + r_on + " " + r_off + " " + dopeWidth + " " +
                totalWidth + " " + mobility;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(32);
    }

    @Override
    public void draw(DrawContext ctx) {
        int segments = 6;
        double d = 6; //distance from lead to draw anode bar
        int i;
        int ox = 0;
        double v1 = volts[0];
        double v2 = volts[1];
        Point barpoint1 = new Point();
        Point barpoint2 = new Point();
        hs = 2 + (int) (8 * (1 - dopeWidth / totalWidth));
        setBbox(point1, point2, hs);
        draw2Leads(ctx);
        setPowerColor(ctx, true);
        double segf = 1. / segments;

        // draw zigzag
        for (i = 0; i <= segments; i++) {
            int nx = (i & 1) == 0 ? 1 : -1;

            if (i == segments)
                nx = 0;
            double v = v1 + (v2 - v1) * i / segments;
            setVoltageColor(ctx, v);
            interpPoint(lead1, lead2, ps1, i * segf, hs * ox);
            interpPoint(lead1, lead2, ps2, i * segf, hs * nx);
            ctx.drawThickLine(ps1, ps2);
            if (i == 0) {
                double a=Math.atan2((point1.y-lead1.y),(point1.x-lead1.x));
                int dx=(int) Math.floor(Math.cos(a)*d);
                int dy=(int) Math.floor(Math.sin(a)*d);

                ctx.drawDoubleThickLine(ps1.x+dx,ps1.y+dy, ps2.x+dx,ps2.y+dy);
                interpPoint(lead1, lead2, ps1, i * segf, -hs * nx);
                ctx.drawDoubleThickLine(ps1.x+dx,ps1.y+dy, ps2.x+dx,ps2.y+dy);
            }
            if (i == segments)
                break;
            interpPoint(lead1, lead2, ps1, (i + 1) * segf, hs * nx);
            ctx.drawThickLine(ps1, ps2);
            ox = nx;
        }

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
    public boolean nonLinear() {
        return true;
    }

    @Override
    public void calculateCurrent() {
        current = (volts[0] - volts[1]) / resistance;
    }

    @Override
    public void reset() {
        dopeWidth = 0;
    }

    @Override
    public void startIteration() {
        double wd = dopeWidth / totalWidth;
        dopeWidth += engine.timeStep * mobility * r_on * current / totalWidth;
        if (dopeWidth < 0)
            dopeWidth = 0;
        if (dopeWidth > totalWidth)
            dopeWidth = totalWidth;
        resistance = r_on * wd + r_off * (1 - wd);

    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[0]);
        engine.stampNonLinear(nodes[1]);
    }

    @Override
    public void doStep() {
        engine.stampResistor(nodes[0], nodes[1], resistance);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "Strukov memristor";
        getBasicInfo(ctx, arr);
        arr[3] = "R = " + ctx.getUnitText(resistance, CirSim.ohmString);
        arr[4] = "P = " + ctx.getUnitText(getPower(), "W");
    }

    @Override
    public double getScopeValue(int x) {
        return (x == 2) ? resistance : (x == 1) ? getPower() : getVoltageDiff();
    }

    @Override
    public String getScopeUnits(int x) {
        return (x == 2) ? CirSim.ohmString : (x == 1) ? "W" : "V";
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Ron (ohms)", r_on, 0, 0);
        if (n == 1)
            return new EditInfo("Roff (ohms)", r_off, 0, 0);
        if (n == 2)
            return new EditInfo("Width of Doped Region (nm)", dopeWidth * 1e9, 0, 0);
        if (n == 3)
            return new EditInfo("Total Width (nm)", totalWidth * 1e9, 0, 0);
        if (n == 4)
            return new EditInfo("Mobility (um^2/(s*V))", mobility * 1e12, 0, 0);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            r_on = ei.getValue();
        if (n == 1)
            r_off = ei.getValue();
        if (n == 2)
            dopeWidth = ei.getValue() * 1e-9;
        if (n == 3)
            totalWidth = ei.getValue() * 1e-9;
        if (n == 4)
            mobility = ei.getValue() * 1e-12;
    }

    public double getROn() {
        return r_on;
    }

    public void setROn(double r_on) {
        this.r_on = r_on;
    }

    public double getROff() {
        return r_off;
    }

    public void setROff(double r_off) {
        this.r_off = r_off;
    }

    public double getDopeWidth() {
        return dopeWidth;
    }

    public void setDopeWidth(double dopeWidth) {
        this.dopeWidth = dopeWidth;
    }

    public double getTotalWidth() {
        return totalWidth;
    }

    public void setTotalWidth(double totalWidth) {
        this.totalWidth = totalWidth;
    }

    public double getMobility() {
        return mobility;
    }

    public void setMobility(double mobility) {
        this.mobility = mobility;
    }
    @Override
    public boolean needsShortcut() {
        return true;
    }
}

