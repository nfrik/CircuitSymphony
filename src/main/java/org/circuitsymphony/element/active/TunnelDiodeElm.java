package org.circuitsymphony.element.active;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;

import java.awt.*;
import java.util.StringTokenizer;

public class TunnelDiodeElm extends CircuitElm {
    private static final double pvp = .1;
    private static final double pip = 4.7e-3;
    private static final double pvv = .37;
    private static final double pvt = .026;
    private static final double pvpp = .525;
    private static final double piv = 370e-6;
    private final int hs = 8;
    private Polygon poly;
    private Point[] cathode;
    private double lastvoltdiff;

    public TunnelDiodeElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public TunnelDiodeElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                          StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, 0);
    }

    public TunnelDiodeElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                          StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public int getDumpType() {
        return 175;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(16);
        cathode = newPointArray(4);
        Point pa[] = newPointArray(2);
        interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
        interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
        interpPoint2(lead1, lead2, cathode[2], cathode[3], .8, hs);
        poly = createPolygon(pa[0], pa[1], lead2);
    }

    @Override
    public void draw(DrawContext ctx) {
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
        ctx.drawThickLine(cathode[2], cathode[0]);
        ctx.drawThickLine(cathode[3], cathode[1]);

        doDots(ctx);
        drawPosts(ctx);
    }

    @Override
    public void reset() {
        lastvoltdiff = volts[0] = volts[1] = curcount = 0;
    }

    private double limitStep(double vnew, double vold) {
        // Prevent voltage changes of more than 1V when iterating.  Wow, I thought it would be
        // much harder than this to prevent convergence problems.
        if (vnew > vold + 1)
            return vold + 1;
        if (vnew < vold - 1)
            return vold - 1;
        return vnew;
    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[0]);
        engine.stampNonLinear(nodes[1]);
    }

    @Override
    public void doStep() {
        double voltdiff = volts[0] - volts[1];
        if (Math.abs(voltdiff - lastvoltdiff) > .01)
            engine.converged = false;
        //System.out.println(voltdiff + " " + lastvoltdiff + " " + Math.abs(voltdiff-lastvoltdiff));
        voltdiff = limitStep(voltdiff, lastvoltdiff);
        lastvoltdiff = voltdiff;

        double i = pip * Math.exp(-pvpp / pvt) * (Math.exp(voltdiff / pvt) - 1) +
                pip * (voltdiff / pvp) * Math.exp(1 - voltdiff / pvp) +
                piv * Math.exp(voltdiff - pvv);

        double geq = pip * Math.exp(-pvpp / pvt) * Math.exp(voltdiff / pvt) / pvt +
                pip * Math.exp(1 - voltdiff / pvp) / pvp
                - Math.exp(1 - voltdiff / pvp) * pip * voltdiff / (pvp * pvp) +
                Math.exp(voltdiff - pvv) * piv;
        double nc = i - geq * voltdiff;
        engine.stampConductance(nodes[0], nodes[1], geq);
        engine.stampCurrentSource(nodes[0], nodes[1], nc);
    }

    @Override
    public void calculateCurrent() {
        double voltdiff = volts[0] - volts[1];
        current = pip * Math.exp(-pvpp / pvt) * (Math.exp(voltdiff / pvt) - 1) +
                pip * (voltdiff / pvp) * Math.exp(1 - voltdiff / pvp) +
                piv * Math.exp(voltdiff - pvv);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "tunnel diode";
        arr[1] = "I = " + ctx.getCurrentText(getCurrent());
        arr[2] = "Vd = " + ctx.getVoltageText(getVoltageDiff());
        arr[3] = "P = " + ctx.getUnitText(getPower(), "W");
    }
}
