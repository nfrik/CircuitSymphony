package org.circuitsymphony.element.devices;

import org.circuitsymphony.element.active.DiodeElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class LEDElm extends DiodeElm {
    private double colorR;
    private double colorG;
    private double colorB;
    private Point ledLead1;
    private Point ledLead2;
    private Point ledCenter;

    public LEDElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        fwdrop = 2.1024259;
        setup();
        colorR = 1;
        colorG = colorB = 0;
    }

    public LEDElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                  StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public LEDElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                  StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
        if ((f & FLAG_FWDROP) == 0)
            fwdrop = 2.1024259;
        setup();
        colorR = new Double(st.nextToken());
        colorG = new Double(st.nextToken());
        colorB = new Double(st.nextToken());
    }

    @Override
    public int getDumpType() {
        return 162;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + colorR + " " + colorG + " " + colorB;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        int cr = 12;
        ledLead1 = interpPoint(point1, point2, .5 - cr / dn);
        ledLead2 = interpPoint(point1, point2, .5 + cr / dn);
        ledCenter = interpPoint(point1, point2, .5);
    }

    @Override
    public void draw(DrawContext ctx) {
        if (needsHighlight(ctx) || this == engine.getDraggedElement()) {
            super.draw(ctx);
            return;
        }
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(point1, ledLead1);
        setVoltageColor(ctx, volts[1]);
        ctx.drawThickLine(ledLead2, point2);

        ctx.setColor(Color.gray);
        int cr = 12;
        ctx.drawThickCircle(ledCenter.x, ledCenter.y, cr);
        cr -= 4;
        double w = 255 * current / .01;
        if (w > 255)
            w = 255;
        Color cc = new Color((int) (colorR * w), (int) (colorG * w),
                (int) (colorB * w));
        ctx.setColor(cc);
        ctx.fillOval(ledCenter.x - cr, ledCenter.y - cr, cr * 2, cr * 2);
        setBbox(point1, point2, cr);
        updateDotCount(ctx);
        drawDots(ctx, point1, ledLead1, curcount);
        drawDots(ctx, point2, ledLead2, -curcount);
        drawPosts(ctx);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        super.getInfo(ctx, arr);
        arr[0] = "LED";
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return super.getEditInfo(n);
        if (n == 1)
            return new EditInfo("Red Value (0-1)", colorR, 0, 1).
                    setDimensionless();
        if (n == 2)
            return new EditInfo("Green Value (0-1)", colorG, 0, 1).
                    setDimensionless();
        if (n == 3)
            return new EditInfo("Blue Value (0-1)", colorB, 0, 1).
                    setDimensionless();
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            super.setEditValue(0, ei);
        if (n == 1)
            colorR = ei.getValue();
        if (n == 2)
            colorG = ei.getValue();
        if (n == 3)
            colorB = ei.getValue();
    }
}
