package org.circuitsymphony.element.devices;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class ProbeElm extends CircuitElm {
    private static final int FLAG_SHOWVOLTAGE = 1;
    private Point center;
    private int hs = 8;

    public ProbeElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public ProbeElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public ProbeElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2, StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
    }

    @Override
    public int getDumpType() {
        return 'p';
    }

    @Override
    public void setPoints() {
        super.setPoints();
        // swap points so that we subtract higher from lower
        if (point2.y < point1.y) {
            Point x = point1;
            point1 = point2;
            point2 = x;
        }
        center = interpPoint(point1, point2, .5);
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(point1, point2, hs);
        boolean selected = (needsHighlight(ctx) || ctx.getSim().plotYElm == this);
        double len = (selected || engine.getDraggedElement() == this) ? 16 : dn - 32;
        calcLeads((int) len);
        setVoltageColor(ctx, volts[0]);
        if (selected)
            ctx.setColor(ctx.selectColor);
        ctx.drawThickLine(point1, lead1);
        setVoltageColor(ctx, volts[1]);
        if (selected)
            ctx.setColor(ctx.selectColor);
        ctx.drawThickLine(lead2, point2);
        Font f = new Font("SansSerif", Font.BOLD, 14);
        ctx.setFont(f);
        if (this == ctx.getSim().plotXElm)
            drawCenteredText(ctx, "X", center.x, center.y, true);
        if (this == ctx.getSim().plotYElm)
            drawCenteredText(ctx, "Y", center.x, center.y, true);
        if (mustShowVoltage()) {
            String s = ctx.getShortUnitText(volts[0], "V");
            drawValues(ctx, s, 4);
        }
        drawPosts(ctx);
    }

    @Override
    public boolean isBasicBoundingBoxSupported() {
        return true;
    }

    @Override
    public void getBasicBoundingBox(Point tempP1, Point tempP2, Rectangle result) {
        setBbox(result, tempP1, tempP2, hs);
    }

    private boolean mustShowVoltage() {
        return (flags & FLAG_SHOWVOLTAGE) != 0;
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "scope probe";
        arr[1] = "Vd = " + ctx.getVoltageText(getVoltageDiff());
    }

    @Override
    public boolean getConnection(int n1, int n2) {
        return false;
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Show Voltage", mustShowVoltage()));
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0) {
            if (ei.getCheckbox().getState())
                flags = FLAG_SHOWVOLTAGE;
            else
                flags &= ~FLAG_SHOWVOLTAGE;
        }
    }
}

