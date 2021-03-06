package org.circuitsymphony.element.active;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;
import org.circuitsymphony.util.Diode;

import java.awt.*;
import java.util.StringTokenizer;

// Silicon-Controlled Rectifier
// 3 nodes, 1 internal node
// 0 = anode, 1 = cathode, 2 = gate
// 0, 3 = variable resistor
// 3, 2 = diode
// 2, 1 = 50 ohm resistor

public class SCRElm extends CircuitElm {
    private final int anode = 0;
    private final int cnode = 1;
    private final int gnode = 2;
    private final int inode = 3;
    private final int hs = 8;
    private Diode diode;
    private double ia;
    private double ic;
    private double ig;
    private double curcount_a;
    private double curcount_c;
    private double curcount_g;
    private double lastvac;
    private double lastvag;
    private double cresistance;
    private double triggerI;
    private double holdingI;
    private Polygon poly;
    private Point[] cathode;
    private Point[] gate;
    private double aresistance;

    public SCRElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        setDefaults();
        setup();
    }

    public SCRElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                  StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public SCRElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                  StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        setDefaults();
        try {
            lastvac = new Double(st.nextToken());
            lastvag = new Double(st.nextToken());
            volts[anode] = 0;
            volts[cnode] = -lastvac;
            volts[gnode] = -lastvag;
            triggerI = new Double(st.nextToken());
            holdingI = new Double(st.nextToken());
            cresistance = new Double(st.nextToken());
        } catch (Exception e) {
        }
        setup();
    }

    private void setDefaults() {
        cresistance = 50;
        holdingI = .0082;
        triggerI = .01;
    }

    private void setup() {
        diode = new Diode(engine);
        diode.setup(.8, 0);
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public void reset() {
        volts[anode] = volts[cnode] = volts[gnode] = 0;
        diode.reset();
        lastvag = lastvac = curcount_a = curcount_c = curcount_g = 0;
    }

    @Override
    public int getDumpType() {
        return 177;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + (volts[anode] - volts[cnode]) + " " +
                (volts[anode] - volts[gnode]) + " " + triggerI + " " + holdingI + " " +
                cresistance;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        int dir;
        if (abs(dx) > abs(dy)) {
            dir = -sign(dx) * sign(dy);
            point2.y = point1.y;
        } else {
            dir = sign(dy) * sign(dx);
            point2.x = point1.x;
        }
        if (dir == 0)
            dir = 1;
        calcLeads(16);
        cathode = newPointArray(2);
        Point pa[] = newPointArray(2);
        interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
        interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
        poly = createPolygon(pa[0], pa[1], lead2);

        gate = newPointArray(2);
        double leadlen = (dn - 16) / 2;
        int gatelen = engine.getGridSize();
        gatelen += leadlen % engine.getGridSize();
        if (leadlen < gatelen) {
            x2 = x;
            y2 = y;
            return;
        }
        interpPoint(lead2, point2, gate[0], gatelen / leadlen, gatelen * dir);
        interpPoint(lead2, point2, gate[1], gatelen / leadlen, engine.getGridSize() * 2 * dir);
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(point1, point2, hs);
        adjustBbox(gate[0], gate[1]);

        double v1 = volts[anode];
        double v2 = volts[cnode];

        draw2Leads(ctx);

        // draw arrow thingy
        setPowerColor(ctx, true);
        setVoltageColor(ctx, v1);
        ctx.fillPolygon(poly);

        // draw thing arrow is pointing to
        setVoltageColor(ctx, v2);
        ctx.drawThickLine(cathode[0], cathode[1]);

        ctx.drawThickLine(lead2, gate[0]);
        ctx.drawThickLine(gate[0], gate[1]);

        curcount_a = updateDotCount(ctx, ia, curcount_a);
        curcount_c = updateDotCount(ctx, ic, curcount_c);
        curcount_g = updateDotCount(ctx, ig, curcount_g);
        if (engine.getDraggedElement() != this) {
            drawDots(ctx, point1, lead2, curcount_a);
            drawDots(ctx, point2, lead2, curcount_c);
            drawDots(ctx, gate[1], gate[0], curcount_g);
            drawDots(ctx, gate[0], lead2, curcount_g + distance(gate[1], gate[0]));
        }
        drawPosts(ctx);
    }

    @Override
    public Point getPost(int n) {
        return (n == 0) ? point1 : (n == 1) ? point2 : gate[1];
    }

    @Override
    public int getPostCount() {
        return 3;
    }

    @Override
    public int getInternalNodeCount() {
        return 1;
    }

    @Override
    public double getPower() {
        return (volts[anode] - volts[gnode]) * ia + (volts[cnode] - volts[gnode]) * ic;
    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[anode]);
        engine.stampNonLinear(nodes[cnode]);
        engine.stampNonLinear(nodes[gnode]);
        engine.stampNonLinear(nodes[inode]);
        engine.stampResistor(nodes[gnode], nodes[cnode], cresistance);
        diode.stamp(nodes[inode], nodes[gnode]);
    }

    @Override
    public void doStep() {
        double vac = volts[anode] - volts[cnode]; // typically negative
        double vag = volts[anode] - volts[gnode]; // typically positive
        if (Math.abs(vac - lastvac) > .01 ||
                Math.abs(vag - lastvag) > .01)
            engine.converged = false;
        lastvac = vac;
        lastvag = vag;
        diode.doStep(volts[inode] - volts[gnode]);
        double icmult = 1 / triggerI;
        double iamult = 1 / holdingI - icmult;
        //System.out.println(icmult + " " + iamult);
        aresistance = (-icmult * ic + ia * iamult > 1) ? .0105 : 10e5;
        //System.out.println(vac + " " + vag + " " + sim.converged + " " + ic + " " + ia + " " + aresistance + " " + volts[inode] + " " + volts[gnode] + " " + volts[anode]);
        engine.stampResistor(nodes[anode], nodes[inode], aresistance);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "SCR";
        double vac = volts[anode] - volts[cnode];
        double vag = volts[anode] - volts[gnode];
        double vgc = volts[gnode] - volts[cnode];
        arr[1] = "Ia = " + ctx.getCurrentText(ia);
        arr[2] = "Ig = " + ctx.getCurrentText(ig);
        arr[3] = "Vac = " + ctx.getVoltageText(vac);
        arr[4] = "Vag = " + ctx.getVoltageText(vag);
        arr[5] = "Vgc = " + ctx.getVoltageText(vgc);
    }

    @Override
    public void calculateCurrent() {
        ic = (volts[cnode] - volts[gnode]) / cresistance;
        ia = (volts[anode] - volts[inode]) / aresistance;
        ig = -ic - ia;
    }

    @Override
    public EditInfo getEditInfo(int n) {
        // ohmString doesn't work here on linux
        if (n == 0)
            return new EditInfo("Trigger Current (A)", triggerI, 0, 0);
        if (n == 1)
            return new EditInfo("Holding Current (A)", holdingI, 0, 0);
        if (n == 2)
            return new EditInfo("Gate-Cathode Resistance (ohms)", cresistance, 0, 0);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0 && ei.getValue() > 0)
            triggerI = ei.getValue();
        if (n == 1 && ei.getValue() > 0)
            holdingI = ei.getValue();
        if (n == 2 && ei.getValue() > 0)
            cresistance = ei.getValue();
    }
}

