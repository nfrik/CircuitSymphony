package org.circuitsymphony.element.passive;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;
import org.circuitsymphony.util.Inductor;

import java.awt.*;
import java.util.StringTokenizer;

// 0 = switch
// 1 = switch end 1
// 2 = switch end 2
// ...
// 3n   = coil
// 3n+1 = coil
// 3n+2 = end of coil resistor

public class RelayElm extends CircuitElm {
    private final Inductor ind;
    private final int nSwitch0 = 0;
    private final int nSwitch1 = 1;
    private final int nSwitch2 = 2;
    private final int FLAG_SWAP_COIL = 1;
    public double a1, a2, a3, a4;
    private double inductance;
    private double r_on;
    private double r_off;
    private double onCurrent;
    private Point[] coilPosts;
    private Point[] coilLeads;
    private Point[][] swposts;
    private Point[][] swpoles;
    private Point[] ptSwitch;
    private Point[] lines;
    private double coilCurrent;
    private double[] switchCurrent;
    private double coilCurCount;
    private double[] switchCurCount;
    private double d_position;
    private double coilR;
    private int i_position;
    private int poleCount;
    private int openhs;
    private int nCoil1;
    private int nCoil2;
    private int nCoil3;

    public RelayElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        ind = new Inductor(engine);
        inductance = .2;
        ind.setup(inductance, 0, Inductor.FLAG_BACK_EULER);
        noDiagonal = true;
        onCurrent = .02;
        r_on = .05;
        r_off = 1e6;
        coilR = 20;
        coilCurrent = coilCurCount = 0;
        poleCount = 1;
        setupPoles();
    }

    public RelayElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                    StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public RelayElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                    StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        poleCount = new Integer(st.nextToken());
        inductance = new Double(st.nextToken());
        coilCurrent = new Double(st.nextToken());
        r_on = new Double(st.nextToken());
        r_off = new Double(st.nextToken());
        onCurrent = new Double(st.nextToken());
        coilR = new Double(st.nextToken());
        noDiagonal = true;
        ind = new Inductor(engine);
        ind.setup(inductance, coilCurrent, Inductor.FLAG_BACK_EULER);
        setupPoles();
    }

    private void setupPoles() {
        nCoil1 = 3 * poleCount;
        nCoil2 = nCoil1 + 1;
        nCoil3 = nCoil1 + 2;
        if (switchCurrent == null || switchCurrent.length != poleCount) {
            switchCurrent = new double[poleCount];
            switchCurCount = new double[poleCount];
        }
    }

    @Override
    public int getDumpType() {
        return 178;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + poleCount + " " +
                inductance + " " + coilCurrent + " " +
                r_on + " " + r_off + " " + onCurrent + " " + coilR;
    }

    @Override
    public void draw(DrawContext ctx) {
        int i, p;
        for (i = 0; i != 2; i++) {
            setVoltageColor(ctx, volts[nCoil1 + i]);
            ctx.drawThickLine(coilLeads[i], coilPosts[i]);
        }
        int x = ((flags & FLAG_SWAP_COIL) != 0) ? 1 : 0;
        drawCoil(ctx, dsign * 6, coilLeads[x], coilLeads[1 - x],
                volts[nCoil1 + x], volts[nCoil2 - x]);

        // draw lines
        ctx.setColor(Color.darkGray);
        for (i = 0; i != poleCount; i++) {
            if (i == 0)
                interpPoint(point1, point2, lines[i * 2], .5,
                        openhs * 2 + 5 * dsign - i * openhs * 3);
            else
                interpPoint(point1, point2, lines[i * 2], .5,
                        (int) (openhs * (-i * 3 + 3 - .5 + d_position)) + 5 * dsign);
            interpPoint(point1, point2, lines[i * 2 + 1], .5,
                    (int) (openhs * (-i * 3 - .5 + d_position)) - 5 * dsign);
            ctx.drawLine(lines[i * 2].x, lines[i * 2].y, lines[i * 2 + 1].x, lines[i * 2 + 1].y);
        }

        for (p = 0; p != poleCount; p++) {
            int po = p * 3;
            for (i = 0; i != 3; i++) {
                // draw lead
                setVoltageColor(ctx, volts[nSwitch0 + po + i]);
                ctx.drawThickLine(swposts[p][i], swpoles[p][i]);
            }

            interpPoint(swpoles[p][1], swpoles[p][2], ptSwitch[p], d_position);
            //setVoltageColor(g, volts[nSwitch0]);
            ctx.setColor(Color.lightGray);
            ctx.drawThickLine(swpoles[p][0], ptSwitch[p]);
            switchCurCount[p] = updateDotCount(ctx, switchCurrent[p],
                    switchCurCount[p]);
            drawDots(ctx, swposts[p][0], swpoles[p][0], switchCurCount[p]);

            if (i_position != 2)
                drawDots(ctx, swpoles[p][i_position + 1], swposts[p][i_position + 1],
                        switchCurCount[p]);
        }

        coilCurCount = updateDotCount(ctx, coilCurrent, coilCurCount);

        drawDots(ctx, coilPosts[0], coilLeads[0], coilCurCount);
        drawDots(ctx, coilLeads[0], coilLeads[1], coilCurCount);
        drawDots(ctx, coilLeads[1], coilPosts[1], coilCurCount);

        drawPosts(ctx);
        setBbox(coilPosts[0], coilLeads[1], 0);
        adjustBbox(swpoles[poleCount - 1][0], swposts[poleCount - 1][1]); // XXX
    }

    @Override
    public void setPoints() {
        super.setPoints();
        setupPoles();
        allocNodes();
        openhs = -dsign * 16;

        // switch
        calcLeads(32);
        swposts = new Point[poleCount][3];
        swpoles = new Point[poleCount][3];
        int i, j;
        for (i = 0; i != poleCount; i++) {
            for (j = 0; j != 3; j++) {
                swposts[i][j] = new Point();
                swpoles[i][j] = new Point();
            }
            interpPoint(lead1, lead2, swpoles[i][0], 0, -openhs * 3 * i);
            interpPoint(lead1, lead2, swpoles[i][1], 1, -openhs * 3 * i - openhs);
            interpPoint(lead1, lead2, swpoles[i][2], 1, -openhs * 3 * i + openhs);
            interpPoint(point1, point2, swposts[i][0], 0, -openhs * 3 * i);
            interpPoint(point1, point2, swposts[i][1], 1, -openhs * 3 * i - openhs);
            interpPoint(point1, point2, swposts[i][2], 1, -openhs * 3 * i + openhs);
        }

        // coil
        coilPosts = newPointArray(2);
        coilLeads = newPointArray(2);
        ptSwitch = newPointArray(poleCount);

        int x = ((flags & FLAG_SWAP_COIL) != 0) ? 1 : 0;
        interpPoint(point1, point2, coilPosts[0], x, openhs * 2);
        interpPoint(point1, point2, coilPosts[1], x, openhs * 3);
        interpPoint(point1, point2, coilLeads[0], .5, openhs * 2);
        interpPoint(point1, point2, coilLeads[1], .5, openhs * 3);

        // lines
        lines = newPointArray(poleCount * 2);
    }

    @Override
    public Point getPost(int n) {
        if (n < 3 * poleCount)
            return swposts[n / 3][n % 3];
        return coilPosts[n - 3 * poleCount];
    }

    @Override
    public int getPostCount() {
        return 2 + poleCount * 3;
    }

    @Override
    public int getInternalNodeCount() {
        return 1;
    }

    @Override
    public void reset() {
        super.reset();
        ind.reset();
        coilCurrent = coilCurCount = 0;
        int i;
        for (i = 0; i != poleCount; i++)
            switchCurrent[i] = switchCurCount[i] = 0;
    }

    @Override
    public void stamp() {
        // inductor from coil post 1 to internal node
        ind.stamp(nodes[nCoil1], nodes[nCoil3]);
        // resistor from internal node to coil post 2
        engine.stampResistor(nodes[nCoil3], nodes[nCoil2], coilR);

        int i;
        for (i = 0; i != poleCount * 3; i++)
            engine.stampNonLinear(nodes[nSwitch0 + i]);
    }

    @Override
    public void startIteration() {
        ind.startIteration(volts[nCoil1] - volts[nCoil3]);

        // magic value to balance operate speed with reset speed semi-realistically
        double magic = 1.3;
        double pmult = Math.sqrt(magic + 1);
        double p = coilCurrent * pmult / onCurrent;
        d_position = Math.abs(p * p) - 1.3;
        if (d_position < 0)
            d_position = 0;
        if (d_position > 1)
            d_position = 1;
        if (d_position < .1)
            i_position = 0;
        else if (d_position > .9)
            i_position = 1;
        else
            i_position = 2;
        //System.out.println("ind " + this + " " + current + " " + voltdiff);
    }

    // we need this to be able to change the matrix for each step
    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public void doStep() {
        ind.doStep();
        int p;
        for (p = 0; p != poleCount * 3; p += 3) {
            engine.stampResistor(nodes[nSwitch0 + p], nodes[nSwitch1 + p],
                    i_position == 0 ? r_on : r_off);
            engine.stampResistor(nodes[nSwitch0 + p], nodes[nSwitch2 + p],
                    i_position == 1 ? r_on : r_off);
        }
    }

    @Override
    public void calculateCurrent() {
        double voltdiff = volts[nCoil1] - volts[nCoil3];
        coilCurrent = ind.calculateCurrent(voltdiff);

        // actually this isn't correct, since there is a small amount
        // of current through the switch when off
        int p;
        for (p = 0; p != poleCount; p++) {
            if (i_position == 2)
                switchCurrent[p] = 0;
            else
                switchCurrent[p] =
                        (volts[nSwitch0 + p * 3] - volts[nSwitch1 + p * 3 + i_position]) / r_on;
        }
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = i_position == 0 ? "relay (off)" :
                i_position == 1 ? "relay (on)" : "relay";
        int i;
        int ln = 1;
        for (i = 0; i != poleCount; i++)
            arr[ln++] = "I" + (i + 1) + " = " + ctx.getCurrentDText(switchCurrent[i]);
        arr[ln++] = "coil I = " + ctx.getCurrentDText(coilCurrent);
        arr[ln++] = "coil Vd = " + ctx.getVoltageDText(volts[nCoil1] - volts[nCoil2]);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Inductance (H)", inductance, 0, 0);
        if (n == 1)
            return new EditInfo("On Resistance (ohms)", r_on, 0, 0);
        if (n == 2)
            return new EditInfo("Off Resistance (ohms)", r_off, 0, 0);
        if (n == 3)
            return new EditInfo("On Current (A)", onCurrent, 0, 0);
        if (n == 4)
            return new EditInfo("Number of Poles", poleCount, 1, 4).
                    setDimensionless();
        if (n == 5)
            return new EditInfo("Coil Resistance (ohms)", coilR, 0, 0);
        if (n == 6) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Swap Coil Direction", (flags & FLAG_SWAP_COIL) != 0));
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0 && ei.getValue() > 0) {
            inductance = ei.getValue();
            ind.setup(inductance, coilCurrent, Inductor.FLAG_BACK_EULER);
        }
        if (n == 1 && ei.getValue() > 0)
            r_on = ei.getValue();
        if (n == 2 && ei.getValue() > 0)
            r_off = ei.getValue();
        if (n == 3 && ei.getValue() > 0)
            onCurrent = ei.getValue();
        if (n == 4 && ei.getValue() >= 1) {
            poleCount = (int) ei.getValue();
            setPoints();
        }
        if (n == 5 && ei.getValue() > 0)
            coilR = ei.getValue();
        if (n == 6) {
            if (ei.getCheckbox().getState())
                flags |= FLAG_SWAP_COIL;
            else
                flags &= ~FLAG_SWAP_COIL;
            setPoints();
        }
    }

    @Override
    public boolean getConnection(int n1, int n2) {
        return (n1 / 3 == n2 / 3);
    }
}
    
