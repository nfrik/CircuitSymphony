package org.circuitsymphony.element.active;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;

import java.awt.*;
import java.util.StringTokenizer;

public class TriodeElm extends CircuitElm {
    private final double mu;
    private final double kg1;
    private final double gridCurrentR = 6000;
    private double curcountp;
    private double curcountc;
    private double curcountg;
    private double currentp;
    private double currentg;
    private double currentc;
    private Point[] plate;
    private Point[] grid;
    private Point[] cath;
    private Point midgrid;
    private Point midcath;
    private int circler;
    private double lastv0;
    private double lastv1;
    private double lastv2;

    public TriodeElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        mu = 93;
        kg1 = 680;
        setup();
    }

    public TriodeElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                     StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public TriodeElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                     StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        mu = new Double(st.nextToken());
        kg1 = new Double(st.nextToken());
        setup();
    }

    private void setup() {
        noDiagonal = true;
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public void reset() {
        volts[0] = volts[1] = volts[2] = 0;
        curcount = 0;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + mu + " " + kg1;
    }

    @Override
    public int getDumpType() {
        return 173;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        plate = newPointArray(4);
        grid = newPointArray(8);
        cath = newPointArray(4);
        grid[0] = point1;
        int nearw = 8;
        interpPoint(point1, point2, plate[1], 1, nearw);
        int farw = 32;
        interpPoint(point1, point2, plate[0], 1, farw);
        int platew = 18;
        interpPoint2(point2, plate[1], plate[2], plate[3], 1, platew);

        circler = 24;
        interpPoint(point1, point2, grid[1], (dn - circler) / dn, 0);
        int i;
        for (i = 0; i != 3; i++) {
            interpPoint(grid[1], point2, grid[2 + i * 2], (i * 3 + 1) / 4.5, 0);
            interpPoint(grid[1], point2, grid[3 + i * 2], (i * 3 + 2) / 4.5, 0);
        }
        midgrid = point2;

        int cathw = 16;
        midcath = interpPoint(point1, point2, 1, -nearw);
        interpPoint2(point2, plate[1], cath[1], cath[2], -1, cathw);
        interpPoint(point2, plate[1], cath[3], -1.2, -cathw);
        interpPoint(point2, plate[1], cath[0], -farw / (double) nearw, cathw);
    }

    @Override
    public void draw(DrawContext ctx) {
        ctx.setColor(Color.gray);
        ctx.drawThickCircle(point2.x, point2.y, circler);
        setBbox(point1, plate[0], 16);
        adjustBbox(cath[0].x, cath[1].y, point2.x + circler, point2.y + circler);
        setPowerColor(ctx, true);
        // draw plate
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(plate[0], plate[1]);
        ctx.drawThickLine(plate[2], plate[3]);
        // draw grid
        setVoltageColor(ctx, volts[1]);
        int i;
        for (i = 0; i != 8; i += 2)
            ctx.drawThickLine(grid[i], grid[i + 1]);
        // draw cathode
        setVoltageColor(ctx, volts[2]);
        for (i = 0; i != 3; i++)
            ctx.drawThickLine(cath[i], cath[i + 1]);
        // draw dots
        curcountp = updateDotCount(ctx, currentp, curcountp);
        curcountc = updateDotCount(ctx, currentc, curcountc);
        curcountg = updateDotCount(ctx, currentg, curcountg);
        if (engine.getDraggedElement() != this) {
            drawDots(ctx, plate[0], midgrid, curcountp);
            drawDots(ctx, midgrid, midcath, curcountc);
            drawDots(ctx, midcath, cath[1], curcountc + 8);
            drawDots(ctx, cath[1], cath[0], curcountc + 8);
            drawDots(ctx, point1, midgrid, curcountg);
        }
        drawPosts(ctx);
    }

    @Override
    public Point getPost(int n) {
        return (n == 0) ? plate[0] : (n == 1) ? grid[0] : cath[0];
    }

    @Override
    public int getPostCount() {
        return 3;
    }

    @Override
    public double getPower() {
        return (volts[0] - volts[2]) * current;
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
        int grid = 1;
        int cath = 2;
        int plate = 0;
        double vgk = vs[grid] - vs[cath];
        double vpk = vs[plate] - vs[cath];
        if (Math.abs(lastv0 - vs[0]) > .01 ||
                Math.abs(lastv1 - vs[1]) > .01 ||
                Math.abs(lastv2 - vs[2]) > .01)
            engine.converged = false;
        lastv0 = vs[0];
        lastv1 = vs[1];
        lastv2 = vs[2];
        double ids;
        double gm = 0;
        double Gds;
        double ival = vgk + vpk / mu;
        currentg = 0;
        if (vgk > .01) {
            engine.stampResistor(nodes[grid], nodes[cath], gridCurrentR);
            currentg = vgk / gridCurrentR;
        }
        if (ival < 0) {
            // should be all zero, but that causes a singular matrix,
            // so instead we treat it as a large resistor
            Gds = 1e-8;
            ids = vpk * Gds;
        } else {
            ids = Math.pow(ival, 1.5) / kg1;
            double q = 1.5 * Math.sqrt(ival) / kg1;
            // gm = dids/dgk;
            // Gds = dids/dpk;
            Gds = q;
            gm = q / mu;
        }
        currentp = ids;
        currentc = ids + currentg;
        double rs = -ids + Gds * vpk + gm * vgk;
        engine.stampMatrix(nodes[plate], nodes[plate], Gds);
        engine.stampMatrix(nodes[plate], nodes[cath], -Gds - gm);
        engine.stampMatrix(nodes[plate], nodes[grid], gm);

        engine.stampMatrix(nodes[cath], nodes[plate], -Gds);
        engine.stampMatrix(nodes[cath], nodes[cath], Gds + gm);
        engine.stampMatrix(nodes[cath], nodes[grid], -gm);

        engine.stampRightSide(nodes[plate], rs);
        engine.stampRightSide(nodes[cath], -rs);
    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[0]);
        engine.stampNonLinear(nodes[1]);
        engine.stampNonLinear(nodes[2]);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "triode";
        double vbc = volts[0] - volts[1];
        double vbe = volts[0] - volts[2];
        double vce = volts[1] - volts[2];
        arr[1] = "Vbe = " + ctx.getVoltageText(vbe);
        arr[2] = "Vbc = " + ctx.getVoltageText(vbc);
        arr[3] = "Vce = " + ctx.getVoltageText(vce);
    }

    // grid not connected to other terminals
    @Override
    public boolean getConnection(int n1, int n2) {
        return !(n1 == 1 || n2 == 1);
    }
}

