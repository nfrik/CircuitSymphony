package org.circuitsymphony.element.active;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;
import org.circuitsymphony.util.Scope;

import java.awt.*;
import java.util.StringTokenizer;

public class TransistorElm extends CircuitElm {
    private static final double leakage = 1e-13; // 1e-6;
    private static final double vt = .025;
    private static final double vdcoef = 1 / vt;
    private static final double rgain = .5;
    private final int pnp;
    private final int FLAG_FLIP = 1;
    private double beta;
    private double fgain;
    private double gmin;
    private double ic;
    private double ie;
    private double ib;
    private double curcount_c;
    private double curcount_e;
    private double curcount_b;
    private Polygon rectPoly;
    private Polygon arrowPoly;
    private Point[] rect;
    private Point[] coll;
    private Point[] emit;
    private Point base;
    private double vcrit;
    private double lastvbc;
    private double lastvbe;

    public TransistorElm(CircuitEngine engine, int xx, int yy, boolean pnpflag) {
        super(engine, xx, yy);
        pnp = (pnpflag) ? -1 : 1;
        beta = 100;
        setup();
    }

    public TransistorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                         StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public TransistorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                         StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        pnp = new Integer(st.nextToken());
        beta = 100;
        try {
            lastvbe = new Double(st.nextToken());
            lastvbc = new Double(st.nextToken());
            volts[0] = 0;
            volts[1] = -lastvbe;
            volts[2] = -lastvbc;
            beta = new Double(st.nextToken());
        } catch (Exception e) {
        }
        setup();
    }

    private void setup() {
        vcrit = vt * Math.log(vt / (Math.sqrt(2) * leakage));
        fgain = beta / (beta + 1);
        noDiagonal = true;
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public void reset() {
        volts[0] = volts[1] = volts[2] = 0;
        lastvbc = lastvbe = curcount_c = curcount_e = curcount_b = 0;
    }

    @Override
    public int getDumpType() {
        return 't';
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + pnp + " " + (volts[0] - volts[1]) + " " +
                (volts[0] - volts[2]) + " " + beta;
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(point1, point2, 16);
        setPowerColor(ctx, true);
        // draw collector
        setVoltageColor(ctx, volts[1]);
        ctx.drawThickLine(coll[0], coll[1]);
        // draw emitter
        setVoltageColor(ctx, volts[2]);
        ctx.drawThickLine(emit[0], emit[1]);
        // draw arrow
        ctx.setColor(ctx.lightGrayColor);
        ctx.fillPolygon(arrowPoly);
        // draw base
        setVoltageColor(ctx, volts[0]);
//        if (sim.powerCheckItem.getState())
//            g.setColor(Color.gray);
        ctx.drawThickLine(point1, base);
        // draw dots
        curcount_b = updateDotCount(ctx, -ib, curcount_b);
        drawDots(ctx, base, point1, curcount_b);
        curcount_c = updateDotCount(ctx, -ic, curcount_c);
        drawDots(ctx, coll[1], coll[0], curcount_c);
        curcount_e = updateDotCount(ctx, -ie, curcount_e);
        drawDots(ctx, emit[1], emit[0], curcount_e);
        // draw base rectangle
        setVoltageColor(ctx, volts[0]);
        setPowerColor(ctx, true);
        ctx.fillPolygon(rectPoly);

        if ((needsHighlight(ctx) || engine.getDraggedElement() == this) && dy == 0) {
            ctx.setColor(Color.white);
            ctx.setFont(ctx.unitsFont);
            int ds = sign(dx);
            ctx.drawString("B", base.x - 10 * ds, base.y - 5);
            ctx.drawString("C", coll[0].x - 3 + 9 * ds, coll[0].y + 4); // x+6 if ds=1, -12 if -1
            ctx.drawString("E", emit[0].x - 3 + 9 * ds, emit[0].y + 4);
        }
        drawPosts(ctx);
    }

    @Override
    public Point getPost(int n) {
        return (n == 0) ? point1 : (n == 1) ? coll[0] : emit[0];
    }

    @Override
    public int getPostCount() {
        return 3;
    }

    @Override
    public double getPower() {
        return (volts[0] - volts[2]) * ib + (volts[1] - volts[2]) * ic;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        int hs = 16;
        if ((flags & FLAG_FLIP) != 0)
            dsign = -dsign;
        int hs2 = hs * dsign * pnp;
        // calc collector, emitter posts
        coll = newPointArray(2);
        emit = newPointArray(2);
        interpPoint2(point1, point2, coll[0], emit[0], 1, hs2);
        // calc rectangle edges
        rect = newPointArray(4);
        interpPoint2(point1, point2, rect[0], rect[1], 1 - 16 / dn, hs);
        interpPoint2(point1, point2, rect[2], rect[3], 1 - 13 / dn, hs);
        // calc points where collector/emitter leads contact rectangle
        interpPoint2(point1, point2, coll[1], emit[1], 1 - 13 / dn, 6 * dsign * pnp);
        // calc point where base lead contacts rectangle
        base = new Point();
        interpPoint(point1, point2, base, 1 - 16 / dn);

        // rectangle
        rectPoly = createPolygon(rect[0], rect[2], rect[3], rect[1]);

        // arrow
        if (pnp == 1)
            arrowPoly = calcArrow(emit[1], emit[0], 8, 4);
        else {
            Point pt = interpPoint(point1, point2, 1 - 11 / dn, -5 * dsign * pnp);
            arrowPoly = calcArrow(emit[0], pt, 8, 4);
        }
    }

    private double limitStep(double vnew, double vold) {
        double arg;

        if (vnew > vcrit && Math.abs(vnew - vold) > (vt + vt)) {
            if (vold > 0) {
                arg = 1 + (vnew - vold) / vt;
                if (arg > 0) {
                    vnew = vold + vt * Math.log(arg);
                } else {
                    vnew = vcrit;
                }
            } else {
                vnew = vt * Math.log(vnew / vt);
            }
            engine.converged = false;
            //System.out.println(vnew + " " + oo + " " + vold);
        }
        return (vnew);
    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[0]);
        engine.stampNonLinear(nodes[1]);
        engine.stampNonLinear(nodes[2]);
    }

    @Override
    public void doStep() {
        double vbc = volts[0] - volts[1]; // typically negative
        double vbe = volts[0] - volts[2]; // typically positive
        if (Math.abs(vbc - lastvbc) > .01 || // .01
                Math.abs(vbe - lastvbe) > .01)
            engine.converged = false;
        gmin = 0;
        if (engine.subIterations > 100) {
            // if we have trouble converging, put a conductance in parallel with all P-N junctions.
            // Gradually increase the conductance value for each iteration.
            gmin = Math.exp(-9 * Math.log(10) * (1 - engine.subIterations / 3000.));
            if (gmin > .1)
                gmin = .1;
        }
        //System.out.print("T " + vbc + " " + vbe + "\n");
        vbc = pnp * limitStep(pnp * vbc, pnp * lastvbc);
        vbe = pnp * limitStep(pnp * vbe, pnp * lastvbe);
        lastvbc = vbc;
        lastvbe = vbe;
        double pcoef = vdcoef * pnp;
        double expbc = Math.exp(vbc * pcoef);
        /*if (expbc > 1e13 || Double.isInfinite(expbc))
          expbc = 1e13;*/
        double expbe = Math.exp(vbe * pcoef);
        if (expbe < 1)
            expbe = 1;
        /*if (expbe > 1e13 || Double.isInfinite(expbe))
          expbe = 1e13;*/
        ie = pnp * leakage * (-(expbe - 1) + rgain * (expbc - 1));
        ic = pnp * leakage * (fgain * (expbe - 1) - (expbc - 1));
        ib = -(ie + ic);
        //System.out.println("gain " + ic/ib);
        //System.out.print("T " + vbc + " " + vbe + " " + ie + " " + ic + "\n");
        double gee = -leakage * vdcoef * expbe;
        double gec = rgain * leakage * vdcoef * expbc;
        double gce = -gee * fgain;
        double gcc = -gec * (1 / rgain);

	    /*System.out.print("gee = " + gee + "\n");
        System.out.print("gec = " + gec + "\n");
	    System.out.print("gce = " + gce + "\n");
	    System.out.print("gcc = " + gcc + "\n");
	    System.out.print("gce+gcc = " + (gce+gcc) + "\n");
	    System.out.print("gee+gec = " + (gee+gec) + "\n");*/

        // stamps from page 302 of Pillage.  Node 0 is the base,
        // node 1 the collector, node 2 the emitter.  Also stamp
        // minimum conductance (gmin) between b,e and b,c
        engine.stampMatrix(nodes[0], nodes[0], -gee - gec - gce - gcc + gmin * 2);
        engine.stampMatrix(nodes[0], nodes[1], gec + gcc - gmin);
        engine.stampMatrix(nodes[0], nodes[2], gee + gce - gmin);
        engine.stampMatrix(nodes[1], nodes[0], gce + gcc - gmin);
        engine.stampMatrix(nodes[1], nodes[1], -gcc + gmin);
        engine.stampMatrix(nodes[1], nodes[2], -gce);
        engine.stampMatrix(nodes[2], nodes[0], gee + gec - gmin);
        engine.stampMatrix(nodes[2], nodes[1], -gec);
        engine.stampMatrix(nodes[2], nodes[2], -gee + gmin);

        // we are solving for v(k+1), not delta v, so we use formula
        // 10.5.13, multiplying J by v(k)
        engine.stampRightSide(nodes[0], -ib - (gec + gcc) * vbc - (gee + gce) * vbe);
        engine.stampRightSide(nodes[1], -ic + gce * vbe + gcc * vbc);
        engine.stampRightSide(nodes[2], -ie + gee * vbe + gec * vbc);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "transistor (" + ((pnp == -1) ? "PNP)" : "NPN)") + " beta=" + ctx.getShowFormat().format(beta);
        double vbc = volts[0] - volts[1];
        double vbe = volts[0] - volts[2];
        double vce = volts[1] - volts[2];
        if (vbc * pnp > .2)
            arr[1] = vbe * pnp > .2 ? "saturation" : "reverse active";
        else
            arr[1] = vbe * pnp > .2 ? "fwd active" : "cutoff";
        arr[2] = "Ic = " + ctx.getCurrentText(ic);
        arr[3] = "Ib = " + ctx.getCurrentText(ib);
        arr[4] = "Vbe = " + ctx.getVoltageText(vbe);
        arr[5] = "Vbc = " + ctx.getVoltageText(vbc);
        arr[6] = "Vce = " + ctx.getVoltageText(vce);
    }

    @Override
    public double getScopeValue(int x) {
        switch (x) {
            case Scope.VAL_IB:
                return ib;
            case Scope.VAL_IC:
                return ic;
            case Scope.VAL_IE:
                return ie;
            case Scope.VAL_VBE:
                return volts[0] - volts[2];
            case Scope.VAL_VBC:
                return volts[0] - volts[1];
            case Scope.VAL_VCE:
                return volts[1] - volts[2];
        }
        return 0;
    }

    @Override
    public String getScopeUnits(int x) {
        switch (x) {
            case Scope.VAL_IB:
            case Scope.VAL_IC:
            case Scope.VAL_IE:
                return "A";
            default:
                return "V";
        }
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Beta/hFE", beta, 10, 1000).
                    setDimensionless();
        if (n == 1) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Swap E/C", (flags & FLAG_FLIP) != 0));
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0) {
            beta = ei.getValue();
            setup();
        }
        if (n == 1) {
            if (ei.getCheckbox().getState())
                flags |= FLAG_FLIP;
            else
                flags &= ~FLAG_FLIP;
            setPoints();
        }
    }

    @Override
    public boolean canViewInScope() {
        return true;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }
}
