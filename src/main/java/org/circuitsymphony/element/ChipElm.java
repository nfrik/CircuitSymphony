package org.circuitsymphony.element;

import org.circuitsymphony.element.chips.CounterElm;
import org.circuitsymphony.element.chips.DecadeElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public abstract class ChipElm extends CircuitElm {

    public final int SIDE_W = 2;
    public final int SIDE_E = 3;
    protected final int SIDE_N = 0;
    protected final int SIDE_S = 1;
    private final int FLAG_SMALL = 1;
    private final int FLAG_FLIP_X = 1024;
    private final int FLAG_FLIP_Y = 2048;
    public int bits;
    public Pin pins[];
    public int sizeX, sizeY;
    protected int csize, cspc, cspc2;
    protected boolean lastClock;
    private int[] rectPointsX;
    private int[] rectPointsY;
    private int[] clockPointsX;
    private int[] clockPointsY;

    public ChipElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);

        if (needsBits()) {
            bits = 4;
            if (this instanceof DecadeElm)
                bits = 10;
            if (this instanceof CounterElm)
                bits = 4;
        }

        noDiagonal = true;
        setupPins();
        setSize(1);
    }

    public ChipElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public ChipElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2, StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        if (needsBits())
            bits = new Integer(st.nextToken());
        noDiagonal = true;
        setupPins();
        setSize((f & FLAG_SMALL) != 0 ? 1 : 2);
        int i;
        for (i = 0; i != getPostCount(); i++) {
            if (pins[i].state) {
                volts[i] = new Double(st.nextToken());
                pins[i].value = volts[i] > 2.5;
            }
        }
    }

    public boolean needsBits() {
        return false;
    }

    private void setSize(int s) {
        csize = s;
        cspc = 8 * s;
        cspc2 = cspc * 2;
        flags &= ~FLAG_SMALL;
        flags |= (s == 1) ? FLAG_SMALL : 0;
    }

    public abstract void setupPins();

    @Override
    public void draw(DrawContext ctx) {
        drawChip(ctx);
    }

    public void drawChip(DrawContext ctx) {
        int i;
        Font f = new Font("SansSerif", 0, 10 * csize);
        ctx.setFont(f);
        FontMetrics fm = ctx.getFontMetrics();
        for (i = 0; i != getPostCount(); i++) {
            Pin p = pins[i];
            setVoltageColor(ctx, volts[i]);
            Point a = p.post;
            Point b = p.stub;
            ctx.drawThickLine(a, b);
            p.curcount = updateDotCount(ctx, p.current, p.curcount);
            drawDots(ctx, b, a, p.curcount);

            if (p.bubble) {
                ctx.setColor(Color.white);
                ctx.drawThickCircle(p.bubbleX, p.bubbleY, 1);
                ctx.setColor(ctx.lightGrayColor);
                ctx.drawThickCircle(p.bubbleX, p.bubbleY, 3);
            }
            ctx.setColor(ctx.whiteColor);
            int sw = fm.stringWidth(p.text);
            ctx.drawString(p.text, p.textloc.x - sw / 2, p.textloc.y + fm.getAscent() / 2);

            if (p.lineOver) {
                int ya = p.textloc.y - fm.getAscent() / 2;
                ctx.drawLine(p.textloc.x - sw / 2, ya, p.textloc.x + sw / 2, ya);
            }
        }
        ctx.setColor(needsHighlight(ctx) ? ctx.selectColor : ctx.lightGrayColor);
        ctx.drawThickPolygon(rectPointsX, rectPointsY, 4);
        if (clockPointsX != null)
            ctx.drawPolyline(clockPointsX, clockPointsY, 3);
        for (i = 0; i != getPostCount(); i++)
            drawPost(ctx, pins[i].post.x, pins[i].post.y, nodes[i]);
    }

    @Override
    public void drag(CirSim sim, int xx, int yy) {
        yy = sim.snapGrid(yy);
        if (xx >= x) {
            y = y2 = yy;
            x2 = sim.snapGrid(xx);
        }
        setPoints();
    }

    @Override
    public void setPoints() {
        if (x2 - x > sizeX * cspc2 && this == engine.getDraggedElement())
            setSize(2);
        //int hs = cspc;
        int x0 = x + cspc2;
        int y0 = y;
        int xr = x0 - cspc;
        int yr = y0 - cspc;
        int xs = sizeX * cspc2;
        int ys = sizeY * cspc2;
        rectPointsX = new int[]{xr, xr + xs, xr + xs, xr};
        rectPointsY = new int[]{yr, yr, yr + ys, yr + ys};
        setBbox(xr, yr, rectPointsX[2], rectPointsY[2]);
        int i;
        for (i = 0; i != getPostCount(); i++) {
            Pin p = pins[i];
            switch (p.side) {
                case SIDE_N:
                    p.setPoint(x0, y0, 1, 0, 0, -1, 0, 0);
                    break;
                case SIDE_S:
                    p.setPoint(x0, y0, 1, 0, 0, 1, 0, ys - cspc2);
                    break;
                case SIDE_W:
                    p.setPoint(x0, y0, 0, 1, -1, 0, 0, 0);
                    break;
                case SIDE_E:
                    p.setPoint(x0, y0, 0, 1, 1, 0, xs - cspc2, 0);
                    break;
            }
        }
    }

    @Override
    public Point getPost(int n) {
        return pins[n].post;
    }

    @Override
    public abstract int getVoltageSourceCount(); // output count

    @Override
    public void setVoltageSource(int j, int vs) {
        int i;
        for (i = 0; i != getPostCount(); i++) {
            Pin p = pins[i];
            if (p.output && j-- == 0) {
                p.voltSource = vs;
                return;
            }
        }
        System.out.println("setVoltageSource failed for " + this);
    }

    @Override
    public void stamp() {
        int i;
        for (i = 0; i != getPostCount(); i++) {
            Pin p = pins[i];
            if (p.output)
                engine.stampVoltageSource(0, nodes[i], p.voltSource);
        }
    }

    public void execute() {
    }

    @Override
    public void doStep() {
        int i;
        for (i = 0; i != getPostCount(); i++) {
            Pin p = pins[i];
            if (!p.output)
                p.value = volts[i] > 2.5;
        }
        execute();
        for (i = 0; i != getPostCount(); i++) {
            Pin p = pins[i];
            if (p.output)
                engine.updateVoltageSource(p.voltSource, p.value ? 5 : 0);
        }
    }

    @Override
    public void reset() {
        int i;
        for (i = 0; i != getPostCount(); i++) {
            pins[i].value = false;
            pins[i].curcount = 0;
            volts[i] = 0;
        }
        lastClock = false;
    }

    @Override
    public String dump(boolean newFormat) {
        //int t = getDumpType();
        String s = super.dump(newFormat);
        if (needsBits())
            s += " " + bits;
        int i;
        for (i = 0; i != getPostCount(); i++) {
            if (pins[i].state)
                s += " " + volts[i];
        }
        return s;
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = getChipName();
        int i, a = 1;
        for (i = 0; i != getPostCount(); i++) {
            Pin p = pins[i];
            if (arr[a] != null)
                arr[a] += "; ";
            else
                arr[a] = "";
            String t = p.text;
            if (p.lineOver)
                t += '\'';
            if (p.clock)
                t = "Clk";
            arr[a] += t + " = " + ctx.getVoltageText(volts[i]);
            if (i % 2 == 1)
                a++;
        }
    }

    @Override
    public void setCurrent(int x, double c) {
        int i;
        for (i = 0; i != getPostCount(); i++)
            if (pins[i].output && pins[i].voltSource == x)
                pins[i].current = c;
    }

    public String getChipName() {
        return "chip";
    }

    @Override
    public boolean getConnection(int n1, int n2) {
        return false;
    }

    @Override
    public boolean hasGroundConnection(int n1) {
        return pins[n1].output;
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Flip X", (flags & FLAG_FLIP_X) != 0));
            return ei;
        }
        if (n == 1) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Flip Y", (flags & FLAG_FLIP_Y) != 0));
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0) {
            if (ei.getCheckbox().getState())
                flags |= FLAG_FLIP_X;
            else
                flags &= ~FLAG_FLIP_X;
            setPoints();
        }
        if (n == 1) {
            if (ei.getCheckbox().getState())
                flags |= FLAG_FLIP_Y;
            else
                flags &= ~FLAG_FLIP_Y;
            setPoints();
        }
    }

    public class Pin {
        public final int pos;
        public final int side;
        public final String text;
        public Point post, stub;
        public Point textloc;
        public int voltSource;
        public int bubbleX;
        public int bubbleY;
        public boolean lineOver, bubble, clock, output, value, state;
        public double curcount, current;

        public Pin(int p, int s, String t) {
            pos = p;
            side = s;
            text = t;
        }

        public void setPoint(int px, int py, int dx, int dy, int dax, int day, int sx,
                             int sy) {
            if ((flags & FLAG_FLIP_X) != 0) {
                dx = -dx;
                dax = -dax;
                px += cspc2 * (sizeX - 1);
                sx = -sx;
            }
            if ((flags & FLAG_FLIP_Y) != 0) {
                dy = -dy;
                day = -day;
                py += cspc2 * (sizeY - 1);
                sy = -sy;
            }
            int xa = px + cspc2 * dx * pos + sx;
            int ya = py + cspc2 * dy * pos + sy;
            post = new Point(xa + dax * cspc2, ya + day * cspc2);
            stub = new Point(xa + dax * cspc, ya + day * cspc);
            textloc = new Point(xa, ya);
            if (bubble) {
                bubbleX = xa + dax * 10 * csize;
                bubbleY = ya + day * 10 * csize;
            }
            if (clock) {
                clockPointsX = new int[3];
                clockPointsY = new int[3];
                clockPointsX[0] = xa + dax * cspc - dx * cspc / 2;
                clockPointsY[0] = ya + day * cspc - dy * cspc / 2;
                clockPointsX[1] = xa;
                clockPointsY[1] = ya;
                clockPointsX[2] = xa + dax * cspc + dx * cspc / 2;
                clockPointsY[2] = ya + day * cspc + dy * cspc / 2;
            }
        }
    }

}
