package org.circuitsymphony.element.active;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class MosfetElm extends CircuitElm {
    public final int hs = 16;
    protected final int pnp;
    private final int FLAG_PNP = 1;
    private final int FLAG_SHOWVT = 2;
    private final int FLAG_DIGITAL = 4;
    protected Point src[];
    protected Point drn[];
    protected double ids;
    private double vt;
    private int pcircler;
    private Point[] gate;
    private Point pcircle;
    private Polygon arrowPoly;
    private double lastv1;
    private double lastv2;
    private int mode = 0;
    private double gm = 0;

    public MosfetElm(CircuitEngine engine, int xx, int yy, boolean pnpflag) {
        super(engine, xx, yy);
        pnp = (pnpflag) ? -1 : 1;
        flags = (pnpflag) ? FLAG_PNP : 0;
        noDiagonal = true;
        vt = getDefaultThreshold();
    }

    public MosfetElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                     StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public MosfetElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                     StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        pnp = ((f & FLAG_PNP) != 0) ? -1 : 1;
        noDiagonal = true;
        vt = getDefaultThreshold();
        try {
            vt = new Double(st.nextToken());
        } catch (Exception e) {
        }
    }

    double getDefaultThreshold() {
        return 1.5;
    }

    double getBeta() {
        return .02;
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    private boolean drawDigital() {
        return (flags & FLAG_DIGITAL) != 0;
    }

    @Override
    public void reset() {
        lastv1 = lastv2 = volts[0] = volts[1] = volts[2] = curcount = 0;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + vt;
    }

    @Override
    public int getDumpType() {
        return 'f';
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(point1, point2, hs);
        setVoltageColor(ctx, volts[1]);
        ctx.drawThickLine(src[0], src[1]);
        setVoltageColor(ctx, volts[2]);
        ctx.drawThickLine(drn[0], drn[1]);
        int segments = 6;
        int i;
        setPowerColor(ctx, true);
        double segf = 1. / segments;
        for (i = 0; i != segments; i++) {
            double v = volts[1] + (volts[2] - volts[1]) * i / segments;
            setVoltageColor(ctx, v);
            interpPoint(src[1], drn[1], ps1, i * segf);
            interpPoint(src[1], drn[1], ps2, (i + 1) * segf);
            ctx.drawThickLine(ps1, ps2);
        }
        setVoltageColor(ctx, volts[1]);
        ctx.drawThickLine(src[1], src[2]);
        setVoltageColor(ctx, volts[2]);
        ctx.drawThickLine(drn[1], drn[2]);
        if (!drawDigital()) {
            setVoltageColor(ctx, pnp == 1 ? volts[1] : volts[2]);
            ctx.fillPolygon(arrowPoly);
        }
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(point1, gate[1]);
        ctx.drawThickLine(gate[0], gate[2]);
        if (drawDigital() && pnp == -1)
            ctx.drawThickCircle(pcircle.x, pcircle.y, pcircler);
        if ((flags & FLAG_SHOWVT) != 0) {
            String s = "" + (vt * pnp);
            ctx.setColor(ctx.whiteColor);
            ctx.setFont(ctx.unitsFont);
            drawCenteredText(ctx, s, x2 + 2, y2, false);
        }
        if ((needsHighlight(ctx) || engine.getDraggedElement() == this) && dy == 0) {
            ctx.setColor(Color.white);
            ctx.setFont(ctx.unitsFont);
            int ds = sign(dx);
            ctx.drawString("G", gate[1].x - 10 * ds, gate[1].y - 5);
            ctx.drawString(pnp == -1 ? "D" : "S", src[0].x - 3 + 9 * ds, src[0].y + 4); // x+6 if ds=1, -12 if -1
            ctx.drawString(pnp == -1 ? "S" : "D", drn[0].x - 3 + 9 * ds, drn[0].y + 4);
        }
        curcount = updateDotCount(ctx, -ids, curcount);
        drawDots(ctx, src[0], src[1], curcount);
        drawDots(ctx, src[1], drn[1], curcount);
        drawDots(ctx, drn[1], drn[0], curcount);
        drawPosts(ctx);
    }

    @Override
    public Point getPost(int n) {
        return (n == 0) ? point1 : (n == 1) ? src[0] : drn[0];
    }

    @Override
    public double getCurrent() {
        return ids;
    }

    @Override
    public double getPower() {
        return ids * (volts[2] - volts[1]);
    }

    @Override
    public int getPostCount() {
        return 3;
    }

    @Override
    public void setPoints() {
        super.setPoints();

        // find the coordinates of the various points we need to draw
        // the MOSFET.
        int hs2 = hs * dsign;
        src = newPointArray(3);
        drn = newPointArray(3);
        interpPoint2(point1, point2, src[0], drn[0], 1, -hs2);
        interpPoint2(point1, point2, src[1], drn[1], 1 - 22 / dn, -hs2);
        interpPoint2(point1, point2, src[2], drn[2], 1 - 22 / dn, -hs2 * 4 / 3);

        gate = newPointArray(3);
        interpPoint2(point1, point2, gate[0], gate[2], 1 - 28 / dn, hs2 / 2); // was 1-20/dn
        interpPoint(gate[0], gate[2], gate[1], .5);

        if (!drawDigital()) {
            if (pnp == 1)
                arrowPoly = calcArrow(src[1], src[0], 10, 4);
            else
                arrowPoly = calcArrow(drn[0], drn[1], 12, 5);
        } else if (pnp == -1) {
            interpPoint(point1, point2, gate[1], 1 - 36 / dn);
            int dist = (dsign < 0) ? 32 : 31;
            pcircle = interpPoint(point1, point2, 1 - dist / dn);
            pcircler = 3;
        }
    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[1]);
        engine.stampNonLinear(nodes[2]);
    }

    @Override
    public void doStep() {
        double vs[] = new double[3];
        vs[0] = volts[0];
        vs[1] = volts[1];
        vs[2] = volts[2];
        if (vs[1] > lastv1 + .5)
            vs[1] = lastv1 + .5;
        if (vs[1] < lastv1 - .5)
            vs[1] = lastv1 - .5;
        if (vs[2] > lastv2 + .5)
            vs[2] = lastv2 + .5;
        if (vs[2] < lastv2 - .5)
            vs[2] = lastv2 - .5;
        int source = 1;
        int drain = 2;
        if (pnp * vs[1] > pnp * vs[2]) {
            source = 2;
            drain = 1;
        }
        int gate = 0;
        double vgs = vs[gate] - vs[source];
        double vds = vs[drain] - vs[source];
        if (Math.abs(lastv1 - vs[1]) > .01 ||
                Math.abs(lastv2 - vs[2]) > .01)
            engine.converged = false;
        lastv1 = vs[1];
        lastv2 = vs[2];
        double realvgs = vgs;
        double realvds = vds;
        vgs *= pnp;
        vds *= pnp;
        ids = 0;
        gm = 0;
        double Gds;
        double beta = getBeta();
        if (vgs > .5 && this instanceof JfetElm) {
            try {
                engine.stop("JFET is reverse biased!", this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        if (vgs < vt) {
            // should be all zero, but that causes a singular matrix,
            // so instead we treat it as a large resistor
            Gds = 1e-8;
            ids = vds * Gds;
            mode = 0;
        } else if (vds < vgs - vt) {
            // linear
            ids = beta * ((vgs - vt) * vds - vds * vds * .5);
            gm = beta * vds;
            Gds = beta * (vgs - vds - vt);
            mode = 1;
        } else {
            // saturation; Gds = 0
            gm = beta * (vgs - vt);
            // use very small Gds to avoid nonconvergence
            Gds = 1e-8;
            ids = .5 * beta * (vgs - vt) * (vgs - vt) + (vds - (vgs - vt)) * Gds;
            mode = 2;
        }
        double rs = -pnp * ids + Gds * realvds + gm * realvgs;
        //System.out.println("M " + vds + " " + vgs + " " + ids + " " + gm + " "+ Gds + " " + volts[0] + " " + volts[1] + " " + volts[2] + " " + source + " " + rs + " " + this);
        engine.stampMatrix(nodes[drain], nodes[drain], Gds);
        engine.stampMatrix(nodes[drain], nodes[source], -Gds - gm);
        engine.stampMatrix(nodes[drain], nodes[gate], gm);

        engine.stampMatrix(nodes[source], nodes[drain], -Gds);
        engine.stampMatrix(nodes[source], nodes[source], Gds + gm);
        engine.stampMatrix(nodes[source], nodes[gate], -gm);

        engine.stampRightSide(nodes[drain], rs);
        engine.stampRightSide(nodes[source], -rs);
        if (source == 2 && pnp == 1 ||
                source == 1 && pnp == -1)
            ids = -ids;
    }

    public void getFetInfo(DrawContext ctx, String arr[], String n) {
        arr[0] = ((pnp == -1) ? "p-" : "n-") + n;
        arr[0] += " (Vt = " + ctx.getVoltageText(pnp * vt) + ")";
        arr[1] = ((pnp == 1) ? "Ids = " : "Isd = ") + ctx.getCurrentText(ids);
        arr[2] = "Vgs = " + ctx.getVoltageText(volts[0] - volts[pnp == -1 ? 2 : 1]);
        arr[3] = ((pnp == 1) ? "Vds = " : "Vsd = ") + ctx.getVoltageText(volts[2] - volts[1]);
        arr[4] = (mode == 0) ? "off" :
                (mode == 1) ? "linear" : "saturation";
        arr[5] = "gm = " + ctx.getUnitText(gm, "A/V");
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        getFetInfo(ctx, arr, "MOSFET");
    }

    @Override
    public boolean canViewInScope() {
        return true;
    }

    @Override
    public double getVoltageDiff() {
        return volts[2] - volts[1];
    }

    @Override
    public boolean getConnection(int n1, int n2) {
        return !(n1 == 0 || n2 == 0);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Threshold Voltage", pnp * vt, .01, 5);
        if (n == 1) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Digital Symbol", drawDigital()));
            return ei;
        }

        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            vt = pnp * ei.getValue();
        if (n == 1) {
            flags = (ei.getCheckbox().getState()) ? (flags | FLAG_DIGITAL) :
                    (flags & ~FLAG_DIGITAL);
            setPoints();
        }
    }

    public void setThresholdVoltage(double value) {
        vt = pnp * value;
    }

    public double getThresholdVoltage() {
        return pnp * vt;
    }
}
