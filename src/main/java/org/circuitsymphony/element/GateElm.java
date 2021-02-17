package org.circuitsymphony.element;

import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public abstract class GateElm extends CircuitElm {
    private final int FLAG_SMALL = 1;
    protected int inputCount = 2;
    protected int hs2;
    protected int ww;
    protected Polygon gatePoly;
    protected Point pcircle, linePoints[];
    private boolean lastOutput;
    private int gsize;
    private int gwidth;
    private int gwidth2;
    private int gheight;
    private Point[] inPosts;
    private Point[] inGates;

    public GateElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        noDiagonal = true;
        inputCount = 2;
        setSize(1);
    }

    public GateElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                   StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public GateElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                   StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        inputCount = new Integer(st.nextToken());
        lastOutput = new Double(st.nextToken()) > 2.5;
        noDiagonal = true;
        setSize((f & FLAG_SMALL) != 0 ? 1 : 2);
    }

    public boolean isInverting() {
        return false;
    }

    private void setSize(int s) {
        gsize = s;
        gwidth = 7 * s;
        gwidth2 = 14 * s;
        gheight = 8 * s;
        flags = (s == 1) ? FLAG_SMALL : 0;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + inputCount + " " + volts[inputCount];
    }

    @Override
    public void setPoints() {
        super.setPoints();
        if (dn > 150 && this == engine.getDraggedElement())
            setSize(2);
        int hs = gheight;
        int i;
        ww = gwidth2; // was 24
        if (ww > dn / 2)
            ww = (int) (dn / 2);
        if (isInverting() && ww + 8 > dn / 2)
            ww = (int) (dn / 2 - 8);
        calcLeads(ww * 2);
        inPosts = new Point[inputCount];
        inGates = new Point[inputCount];
        allocNodes();
        int i0 = -inputCount / 2;
        for (i = 0; i != inputCount; i++, i0++) {
            if (i0 == 0 && (inputCount & 1) == 0)
                i0++;
            inPosts[i] = interpPoint(point1, point2, 0, hs * i0);
            inGates[i] = interpPoint(lead1, lead2, 0, hs * i0);
            volts[i] = (lastOutput ^ isInverting()) ? 5 : 0;
        }
        hs2 = gwidth * (inputCount / 2 + 1);
        setBbox(point1, point2, hs2);
    }

    @Override
    public void draw(DrawContext ctx) {
        int i;
        for (i = 0; i != inputCount; i++) {
            setVoltageColor(ctx, volts[i]);
            ctx.drawThickLine(inPosts[i], inGates[i]);
        }
        setVoltageColor(ctx, volts[inputCount]);
        ctx.drawThickLine(lead2, point2);
        ctx.setColor(needsHighlight(ctx) ? ctx.selectColor : ctx.lightGrayColor);
        ctx.drawThickPolygon(gatePoly);
        if (linePoints != null)
            for (i = 0; i != linePoints.length - 1; i++)
                ctx.drawThickLine(linePoints[i], linePoints[i + 1]);
        if (isInverting())
            ctx.drawThickCircle(pcircle.x, pcircle.y, 3);
        curcount = updateDotCount(ctx, current, curcount);
        drawDots(ctx, lead2, point2, curcount);
        drawPosts(ctx);
    }

    @Override
    public int getPostCount() {
        return inputCount + 1;
    }

    @Override
    public Point getPost(int n) {
        if (n == inputCount)
            return point2;
        return inPosts[n];
    }

    @Override
    public int getVoltageSourceCount() {
        return 1;
    }

    public abstract String getGateName();

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = getGateName();
        arr[1] = "Vout = " + ctx.getVoltageText(volts[inputCount]);
        arr[2] = "Iout = " + ctx.getCurrentText(getCurrent());
    }

    @Override
    public void stamp() {
        engine.stampVoltageSource(0, nodes[inputCount], voltSource);
    }

    public boolean getInput(int x) {
        return volts[x] > 2.5;
    }

    public abstract boolean calcFunction();

    @Override
    public void doStep() {
        boolean f = calcFunction();
        if (isInverting())
            f = !f;
        lastOutput = f;
        double res = f ? 5 : 0;
        engine.updateVoltageSource(voltSource, res);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("# of Inputs", inputCount, 1, 8).
                    setDimensionless();
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        inputCount = (int) ei.getValue();
        setPoints();
    }

    // there is no current path through the gate inputs, but there
    // is an indirect path through the output to ground.
    @Override
    public boolean getConnection(int n1, int n2) {
        return false;
    }

    @Override
    public boolean hasGroundConnection(int n1) {
        return (n1 == inputCount);
    }
}

