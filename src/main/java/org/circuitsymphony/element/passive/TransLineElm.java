package org.circuitsymphony.element.passive;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class TransLineElm extends CircuitElm {
    private double delay;
    private double imped;
    private double[] voltageL;
    private double[] voltageR;
    private int lenSteps;
    private int ptr;
    private int width;
    private Point[] posts;
    private Point[] inner;
    private int voltSource1;
    private int voltSource2;
    private double current1;
    private double current2;
    private double curCount1;
    private double curCount2;

    public TransLineElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        delay = 1000 * engine.timeStep;
        imped = 75;
        noDiagonal = true;
        reset();
    }

    public TransLineElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                        StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public TransLineElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                        StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        delay = new Double(st.nextToken());
        imped = new Double(st.nextToken());
        width = new Integer(st.nextToken());
        // next slot is for resistance (losses), which is not implemented
        st.nextToken();
        noDiagonal = true;
        reset();
    }

    @Override
    public int getDumpType() {
        return 171;
    }

    @Override
    public int getPostCount() {
        return 4;
    }

    @Override
    public int getInternalNodeCount() {
        return 2;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + delay + " " + imped + " " + width + " " + 0.;
    }

    @Override
    public void drag(CirSim sim, int xx, int yy) {
        xx = sim.snapGrid(xx);
        yy = sim.snapGrid(yy);
        int w1 = max(sim.getGridSize(), abs(yy - y));
        int w2 = max(sim.getGridSize(), abs(xx - x));
        if (w1 > w2) {
            xx = x;
            width = w2;
        } else {
            yy = y;
            width = w1;
        }
        x2 = xx;
        y2 = yy;
        setPoints();
    }

    @Override
    public void reset() {
        if (engine.timeStep == 0)
            return;
        lenSteps = (int) (delay / engine.timeStep);
        System.out.println(lenSteps + " steps");
        if (lenSteps > 100000)
            voltageL = voltageR = null;
        else {
            voltageL = new double[lenSteps];
            voltageR = new double[lenSteps];
        }
        ptr = 0;
        super.reset();
    }

    @Override
    public void setPoints() {
        super.setPoints();
        int ds = (dy == 0) ? sign(dx) : -sign(dy);
        Point p3 = interpPoint(point1, point2, 0, -width * ds);
        Point p4 = interpPoint(point1, point2, 1, -width * ds);
        int sep = engine.getGridSize() / 2;
        Point p5 = interpPoint(point1, point2, 0, -(width / 2 - sep) * ds);
        Point p6 = interpPoint(point1, point2, 1, -(width / 2 - sep) * ds);
        Point p7 = interpPoint(point1, point2, 0, -(width / 2 + sep) * ds);
        Point p8 = interpPoint(point1, point2, 1, -(width / 2 + sep) * ds);

        // we number the posts like this because we want the lower-numbered
        // points to be on the bottom, so that if some of them are unconnected
        // (which is often true) then the bottom ones will get automatically
        // attached to ground.
        posts = new Point[]{p3, p4, point1, point2};
        inner = new Point[]{p7, p8, p5, p6};
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(posts[0], posts[3], 0);
        int segments = (int) (dn / 2);
        int ix0 = ptr - 1 + lenSteps;
        double segf = 1. / segments;
        int i;
        ctx.setColor(Color.darkGray);
        ctx.fillRect(inner[2].x, inner[2].y, inner[1].x - inner[2].x + 2, inner[1].y - inner[2].y + 2);
        for (i = 0; i != 4; i++) {
            setVoltageColor(ctx, volts[i]);
            ctx.drawThickLine(posts[i], inner[i]);
        }
        if (voltageL != null) {
            for (i = 0; i != segments; i++) {
                int ix1 = (ix0 - lenSteps * i / segments) % lenSteps;
                int ix2 = (ix0 - lenSteps * (segments - 1 - i) / segments) % lenSteps;
                double v = (voltageL[ix1] + voltageR[ix2]) / 2;
                setVoltageColor(ctx, v);
                interpPoint(inner[0], inner[1], ps1, i * segf);
                interpPoint(inner[2], inner[3], ps2, i * segf);
                ctx.drawLine(ps1.x, ps1.y, ps2.x, ps2.y);
                interpPoint(inner[2], inner[3], ps1, (i + 1) * segf);
                ctx.drawThickLine(ps1, ps2);
            }
        }
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(inner[0], inner[1]);
        drawPosts(ctx);

        curCount1 = updateDotCount(ctx, -current1, curCount1);
        curCount2 = updateDotCount(ctx, current2, curCount2);
        if (engine.getDraggedElement() != this) {
            drawDots(ctx, posts[0], inner[0], curCount1);
            drawDots(ctx, posts[2], inner[2], -curCount1);
            drawDots(ctx, posts[1], inner[1], -curCount2);
            drawDots(ctx, posts[3], inner[3], curCount2);
        }
    }

    @Override
    public void setVoltageSource(int n, int v) {
        if (n == 0)
            voltSource1 = v;
        else
            voltSource2 = v;
    }

    @Override
    public void setCurrent(int v, double c) {
        if (v == voltSource1)
            current1 = c;
        else
            current2 = c;
    }

    @Override
    public void stamp() {
        engine.stampVoltageSource(nodes[4], nodes[0], voltSource1);
        engine.stampVoltageSource(nodes[5], nodes[1], voltSource2);
        engine.stampResistor(nodes[2], nodes[4], imped);
        engine.stampResistor(nodes[3], nodes[5], imped);
    }

    @Override
    public void startIteration() {
        // calculate voltages, currents sent over wire
        if (voltageL == null) {
            try {
                engine.stop("Transmission line delay too large!", this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        voltageL[ptr] = volts[2] - volts[0] + volts[2] - volts[4];
        voltageR[ptr] = volts[3] - volts[1] + volts[3] - volts[5];
        //System.out.println(volts[2] + " " + volts[0] + " " + (volts[2]-volts[0]) + " " + (imped*current1) + " " + voltageL[ptr]);
    /*System.out.println("sending fwd  " + currentL[ptr] + " " + current1);
      System.out.println("sending back " + currentR[ptr] + " " + current2);*/
        //System.out.println("sending back " + voltageR[ptr]);
        ptr = (ptr + 1) % lenSteps;
    }

    @Override
    public void doStep() {
        if (voltageL == null) {
            try {
                engine.stop("Transmission line delay too large!", this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        engine.updateVoltageSource(voltSource1, -voltageR[ptr]);
        engine.updateVoltageSource(voltSource2, -voltageL[ptr]);
        if (Math.abs(volts[0]) > 1e-5 || Math.abs(volts[1]) > 1e-5) {
            try {
                engine.stop("Need to ground transmission line!", this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Point getPost(int n) {
        return posts[n];
    }

    //double getVoltageDiff() { return volts[0]; }
    @Override
    public int getVoltageSourceCount() {
        return 2;
    }

    @Override
    public boolean hasGroundConnection(int n1) {
        return false;
    }

    @Override
    public boolean getConnection(int n1, int n2) {
        return false;
    /*if (comparePair(n1, n2, 0, 1))
      return true;
	  if (comparePair(n1, n2, 2, 3))
	  return true;
	  return false;*/
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "transmission line";
        arr[1] = ctx.getUnitText(imped, CirSim.ohmString);
        arr[2] = "length = " + ctx.getUnitText(2.9979e8 * delay, "m");
        arr[3] = "delay = " + ctx.getUnitText(delay, "s");
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Delay (s)", delay, 0, 0);
        if (n == 1)
            return new EditInfo("Impedance (ohms)", imped, 0, 0);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0) {
            delay = ei.getValue();
            reset();
        }
        if (n == 1) {
            imped = ei.getValue();
            reset();
        }
    }
}

