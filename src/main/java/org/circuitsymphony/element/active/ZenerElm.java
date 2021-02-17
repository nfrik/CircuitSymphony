package org.circuitsymphony.element.active;

import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

// Zener code contributed by J. Mike Rollins
// http://www.camotruck.net/rollins/simulator.html
public class ZenerElm extends DiodeElm {
    private final int hs = 8;
    private final double default_zvoltage = 5.6;
    private Polygon poly;
    private Point[] cathode;
    private Point[] wing;

    public ZenerElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        zvoltage = default_zvoltage;
        setup();
    }

    public ZenerElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                    StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public ZenerElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                    StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
        zvoltage = new Double(st.nextToken());
        setup();
    }

    @Override
    public void setup() {
        diode.setLeakage(5e-6); // 1N4004 is 5.0 uAmp
        super.setup();
    }

    @Override
    public int getDumpType() {
        return 'z';
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + zvoltage;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(16);
        cathode = newPointArray(2);
        wing = newPointArray(2);
        Point pa[] = newPointArray(2);
        interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
        interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
        interpPoint(cathode[0], cathode[1], wing[0], -0.2, -hs);
        interpPoint(cathode[1], cathode[0], wing[1], -0.2, -hs);
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

        // draw wings on cathode
        ctx.drawThickLine(wing[0], cathode[0]);
        ctx.drawThickLine(wing[1], cathode[1]);

        doDots(ctx);
        drawPosts(ctx);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        super.getInfo(ctx, arr);
        arr[0] = "Zener diode";
        arr[5] = "Vz = " + ctx.getVoltageText(zvoltage);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Fwd Voltage @ 1A", fwdrop, 10, 1000);
        if (n == 1)
            return new EditInfo("Zener Voltage @ 5mA", zvoltage, 1, 25);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            fwdrop = ei.getValue();
        if (n == 1)
            zvoltage = ei.getValue();
        setup();
    }

    public double getZennerVoltage() {
        return zvoltage;
    }

    public void setZennerVoltage(double zvoltage) {
        this.zvoltage = zvoltage;
    }
}
