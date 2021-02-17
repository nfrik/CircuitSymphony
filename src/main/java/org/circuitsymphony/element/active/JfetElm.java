package org.circuitsymphony.element.active;

import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;

import java.awt.*;
import java.util.StringTokenizer;

public class JfetElm extends MosfetElm {
    private Polygon gatePoly;
    private Polygon arrowPoly;
    private Point gatePt;

    public JfetElm(CircuitEngine engine, int xx, int yy, boolean pnpflag) {
        super(engine, xx, yy, pnpflag);
        noDiagonal = true;
    }

    public JfetElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                   StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public JfetElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                   StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
        noDiagonal = true;
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(point1, point2, hs);
        setVoltageColor(ctx, volts[1]);
        ctx.drawThickLine(src[0], src[1]);
        ctx.drawThickLine(src[1], src[2]);
        setVoltageColor(ctx, volts[2]);
        ctx.drawThickLine(drn[0], drn[1]);
        ctx.drawThickLine(drn[1], drn[2]);
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(point1, gatePt);
        ctx.fillPolygon(arrowPoly);
        setPowerColor(ctx, true);
        ctx.fillPolygon(gatePoly);
        curcount = updateDotCount(ctx, -ids, curcount);
        if (curcount != 0) {
            drawDots(ctx, src[0], src[1], curcount);
            drawDots(ctx, src[1], src[2], curcount + 8);
            drawDots(ctx, drn[0], drn[1], -curcount);
            drawDots(ctx, drn[1], drn[2], -(curcount + 8));
        }
        drawPosts(ctx);
    }

    @Override
    public void setPoints() {
        super.setPoints();

        // find the coordinates of the various points we need to draw
        // the JFET.
        int hs2 = hs * dsign;
        src = newPointArray(3);
        drn = newPointArray(3);
        interpPoint2(point1, point2, src[0], drn[0], 1, hs2);
        interpPoint2(point1, point2, src[1], drn[1], 1, hs2 / 2);
        interpPoint2(point1, point2, src[2], drn[2], 1 - 10 / dn, hs2 / 2);

        gatePt = interpPoint(point1, point2, 1 - 14 / dn);

        Point ra[] = newPointArray(4);
        interpPoint2(point1, point2, ra[0], ra[1], 1 - 13 / dn, hs);
        interpPoint2(point1, point2, ra[2], ra[3], 1 - 10 / dn, hs);
        gatePoly = createPolygon(ra[0], ra[1], ra[3], ra[2]);
        if (pnp == -1) {
            Point x = interpPoint(gatePt, point1, 18 / dn);
            arrowPoly = calcArrow(gatePt, x, 8, 3);
        } else
            arrowPoly = calcArrow(point1, gatePt, 8, 3);
    }

    @Override
    public int getDumpType() {
        return 'j';
    }

    // these values are taken from Hayes+Horowitz p155
    @Override
    public double getDefaultThreshold() {
        return -4;
    }

    @Override
    public double getBeta() {
        return .00125;
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        getFetInfo(ctx, arr, "JFET");
    }
}
