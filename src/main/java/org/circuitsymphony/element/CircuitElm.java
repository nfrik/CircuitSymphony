package org.circuitsymphony.element;


import org.circuitsymphony.Editable;
import org.circuitsymphony.element.io.RailElm;
import org.circuitsymphony.element.io.SweepElm;
import org.circuitsymphony.element.io.VoltageElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;

/*
 * Components for Mod start in 501
 * */

public abstract class CircuitElm implements Editable {
    protected static final double PI = CirSim.PI;
    public boolean selected;
    public int x, y, x2, y2, flags, flags2, nodes[], voltSource;
    public double dn;
    public Point point1, point2, lead1, lead2;
    public double volts[];
    public double current, curcount;
    public Rectangle boundingBox;
    public boolean noDiagonal;
    protected Point ps1, ps2;
    protected int dx, dy, dsign;
    protected CircuitEngine engine;
    private double dpx1;
    private double dpy1;

    public CircuitElm(CircuitEngine engine, int xx, int yy) {
        this.engine = engine;
        this.ps1 = engine.ps1;
        this.ps2 = engine.ps2;

        x = x2 = xx;
        y = y2 = yy;
        flags = getDefaultFlags();
        allocNodes();
        initBoundingBox();
    }

    public CircuitElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f) {
        this(engine, xa, ya, xb, yb, f, 0);
    }

    public CircuitElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2) {
        this.engine = engine;
        this.ps1 = engine.ps1;
        this.ps2 = engine.ps2;

        x = xa;
        y = ya;
        x2 = xb;
        y2 = yb;
        flags = f;
        flags2 = f2;
        allocNodes();
        initBoundingBox();
    }

    public static int abs(int x) {
        return x < 0 ? -x : x;
    }

    public static int sign(int x) {
        return (x < 0) ? -1 : (x == 0) ? 0 : 1;
    }

    public static int min(int a, int b) {
        return (a < b) ? a : b;
    }

    public static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    public static double distance(Point p1, Point p2) {
        double x = p1.x - p2.x;
        double y = p1.y - p2.y;
        return Math.sqrt(x * x + y * y);
    }

    public int getDumpType() {
        return 0;
    }

    public Class getDumpClass() {
        return getClass();
    }

    public int getDefaultFlags() {
        return 0;
    }

    public int getFlags2() {
        return flags2;
    }

    private void initBoundingBox() {
        boundingBox = new Rectangle();
        boundingBox.setBounds(min(x, x2), min(y, y2),
                abs(x2 - x) + 1, abs(y2 - y) + 1);
    }

    public void allocNodes() {
        nodes = new int[getPostCount() + getInternalNodeCount()];
        volts = new double[getPostCount() + getInternalNodeCount()];
    }

    public String dump(boolean newFormat) {
        int t = getDumpType();
        String dump = (t < 127 ? ((char) t) + " " : t + " ") + x + " " + y + " " + x2 + " " + y2 + " " + flags;
        if (newFormat) dump += " " + flags2;
        return dump;
    }

    public void reset() {
        int i;
        for (i = 0; i != getPostCount() + getInternalNodeCount(); i++)
            volts[i] = 0;
        curcount = 0;
    }

    public void draw(DrawContext ctx) {
    }

    public void setCurrent(int x, double c) {
        current = c;
    }

    public double getCurrent() {
        return current;
    }

    public void doStep() {
    }

    public void delete() {
    }

    public void startIteration() {
    }

    public double getPostVoltage(int x) {
        return volts[x];
    }

    public void setNodeVoltage(int n, double c) {
        volts[n] = c;
        calculateCurrent();
    }

    public void calculateCurrent() {
    }

    public void setPoints() {
        dx = x2 - x;
        dy = y2 - y;
        dn = Math.sqrt(dx * dx + dy * dy);
        dpx1 = calcDpx1(dy, dn);
        dpy1 = calcDpy1(dx, dn);
        dsign = (dy == 0) ? sign(dx) : sign(dy);
        point1 = new Point(x, y);
        point2 = new Point(x2, y2);
    }

    private double calcDpx1(int x, int y, int x2, int y2) {
        double dx = x2 - x;
        double dy = y2 - y;
        double dn = Math.sqrt(dx * dx + dy * dy);
        return calcDpx1(dy, dn);
    }

    private double calcDpx1(double dy, double dn) {
        return dy / dn;
    }

    private double calcDpy1(int x, int y, int x2, int y2) {
        double dx = x2 - x;
        double dy = y2 - y;
        double dn = Math.sqrt(dx * dx + dy * dy);
        return calcDpy1(dx, dn);
    }

    private double calcDpy1(double dx, double dn) {
        return -dx / dn;
    }

    public void calcLeads(int len) {
        if (dn < len || len == 0) {
            lead1 = point1;
            lead2 = point2;
            return;
        }
        lead1 = interpPoint(point1, point2, (dn - len) / (2 * dn));
        lead2 = interpPoint(point1, point2, (dn + len) / (2 * dn));
    }

    public Point interpPoint(Point a, Point b, double f) {
        Point p = new Point();
        interpPoint(a, b, p, f);
        return p;
    }

    public void interpPoint(Point a, Point b, Point c, double f) {
        c.x = (int) Math.floor(a.x * (1 - f) + b.x * f + .48);
        c.y = (int) Math.floor(a.y * (1 - f) + b.y * f + .48);
    }

    public void interpPoint(Point a, Point b, Point c, double f, double g) {
        int gx = b.y - a.y;
        int gy = a.x - b.x;
        g /= Math.sqrt(gx * gx + gy * gy);
        c.x = (int) Math.floor(a.x * (1 - f) + b.x * f + g * gx + .48);
        c.y = (int) Math.floor(a.y * (1 - f) + b.y * f + g * gy + .48);
    }

    public Point interpPoint(Point a, Point b, double f, double g) {
        Point p = new Point();
        interpPoint(a, b, p, f, g);
        return p;
    }

    public void interpPoint2(Point a, Point b, Point c, Point d, double f, double g) {
        int gx = b.y - a.y;
        int gy = a.x - b.x;
        g /= Math.sqrt(gx * gx + gy * gy);
        c.x = (int) Math.floor(a.x * (1 - f) + b.x * f + g * gx + .48);
        c.y = (int) Math.floor(a.y * (1 - f) + b.y * f + g * gy + .48);
        d.x = (int) Math.floor(a.x * (1 - f) + b.x * f - g * gx + .48);
        d.y = (int) Math.floor(a.y * (1 - f) + b.y * f - g * gy + .48);
    }

    public void draw2Leads(DrawContext ctx) {
        // draw first lead
        setVoltageColor(ctx, volts[0]);
        ctx.drawThickLine(point1, lead1);

        // draw second lead
        setVoltageColor(ctx, volts[1]);
        ctx.drawThickLine(lead2, point2);
    }

    public Point[] newPointArray(int n) {
        Point a[] = new Point[n];
        while (n > 0)
            a[--n] = new Point();
        return a;
    }

    public void drawDots(DrawContext ctx, Point pa, Point pb, double pos) {
        if (ctx.getSim().stoppedCheck.getState() || pos == 0 || !ctx.getSim().getDotsCheckItem().getState())
            return;
        int dx = pb.x - pa.x;
        int dy = pb.y - pa.y;
        double dn = Math.sqrt(dx * dx + dy * dy);
        ctx.setColor(Color.yellow);
        int ds = 16;
        pos %= ds;
        if (pos < 0)
            pos += ds;
        double di;
        for (di = pos; di < dn; di += ds) {
            int x0 = (int) (pa.x + di * dx / dn);
            int y0 = (int) (pa.y + di * dy / dn);
            ctx.fillRect((x0 - 1), (y0 - 1), 4, 4);
        }
    }

    public Polygon calcArrow(Point a, Point b, double al, double aw) {
        Polygon poly = new Polygon();
        Point p1 = new Point();
        Point p2 = new Point();
        int adx = b.x - a.x;
        int ady = b.y - a.y;
        double l = Math.sqrt(adx * adx + ady * ady);
        poly.addPoint(b.x, b.y);
        interpPoint2(a, b, p1, p2, 1 - al / l, aw);
        poly.addPoint(p1.x, p1.y);
        poly.addPoint(p2.x, p2.y);
        return poly;
    }

    public Polygon createPolygon(Point a, Point b, Point c) {
        Polygon p = new Polygon();
        p.addPoint(a.x, a.y);
        p.addPoint(b.x, b.y);
        p.addPoint(c.x, c.y);
        return p;
    }

    public Polygon createPolygon(Point a, Point b, Point c, Point d) {
        Polygon p = new Polygon();
        p.addPoint(a.x, a.y);
        p.addPoint(b.x, b.y);
        p.addPoint(c.x, c.y);
        p.addPoint(d.x, d.y);
        return p;
    }

    public Polygon createPolygon(Point a[]) {
        Polygon p = new Polygon();
        int i;
        for (i = 0; i != a.length; i++)
            p.addPoint(a[i].x, a[i].y);
        return p;
    }

    public void drag(CirSim sim, int xx, int yy) {
        xx = sim.snapGrid(xx);
        yy = sim.snapGrid(yy);
        if (noDiagonal) {
            if (Math.abs(x - xx) < Math.abs(y - yy)) {
                xx = x;
            } else {
                yy = y;
            }
        }
        x2 = xx;
        y2 = yy;
        setPoints();
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
        x2 += dx;
        y2 += dy;
        boundingBox.setLocation(dx, dy);
        setPoints();
    }

    public void moveWithGrid(CirSim sim, int dx, int dy) {
        x = sim.snapGrid(x + dx);
        y = sim.snapGrid(y + dy);
        x2 = sim.snapGrid(x2 + dx);
        y2 = sim.snapGrid(y2 + dy);
        boundingBox.translate(dx, dy);
        setPoints();
    }

    // determine if moving this element by (dx,dy) will put it on top of another element
    public boolean allowMove(int dx, int dy) {
        int nx = x + dx;
        int ny = y + dy;
        int nx2 = x2 + dx;
        int ny2 = y2 + dy;
        int i;
        for (i = 0; i != engine.elmList.size(); i++) {
            CircuitElm ce = engine.getElm(i);
            if (ce.x == nx && ce.y == ny && ce.x2 == nx2 && ce.y2 == ny2)
                return false;
            if (ce.x == nx2 && ce.y == ny2 && ce.x2 == nx && ce.y2 == ny)
                return false;
        }
        return true;
    }

    public void movePoint(int n, int dx, int dy) {
        if (n == 0) {
            x += dx;
            y += dy;
        } else {
            x2 += dx;
            y2 += dy;
        }
        setPoints();
    }

    public void drawPosts(DrawContext ctx) {
        int i;
        for (i = 0; i != getPostCount(); i++) {
            Point p = getPost(i);
            drawPost(ctx, p.x, p.y, nodes[i]);
        }
    }

    public void stamp() {
    }

    public int getVoltageSourceCount() {
        return 0;
    }

    public int getInternalNodeCount() {
        return 0;
    }

    public void setNode(int p, int n) {
        nodes[p] = n;
    }

    public void setVoltageSource(int n, int v) {
        voltSource = v;
    }

    public int getVoltageSource() {
        return voltSource;
    }

    public double getVoltageDiff() {
        return volts[0] - volts[1];
    }

    public boolean nonLinear() {
        return false;
    }

    public int getPostCount() {
        return 2;
    }

    public int getNode(int n) {
        return nodes[n];
    }

    public Point getPost(int n) {
        return (n == 0) ? point1 : (n == 1) ? point2 : null;
    }

    public void drawPost(DrawContext ctx, int x0, int y0, int n) {
        if (engine.getDraggedElement() == null && !needsHighlight(ctx) &&
                ctx.getSim().getCircuitNode(n).getLinks().size() == 2)
            return;
//        if (sim.mouseMode == CirSim.MODE_DRAG_ROW ||
//                sim.mouseMode == CirSim.MODE_DRAG_COLUMN)
//            return;
        drawPost(ctx, x0, y0);
    }

    public void drawPost(DrawContext ctx, int x0, int y0) {
        ctx.setColor(ctx.whiteColor);
        ctx.fillOval(x0 - 3, y0 - 3, 7, 7);
    }

    public void setBbox(int x1, int y1, int x2, int y2) {
        setBbox(boundingBox, x1, y1, x2, y2);
    }

    protected void setBbox(Rectangle rect, int x1, int y1, int x2, int y2) {
        if (x1 > x2) {
            int q = x1;
            x1 = x2;
            x2 = q;
        }
        if (y1 > y2) {
            int q = y1;
            y1 = y2;
            y2 = q;
        }
        rect.setBounds(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
    }

    public void setBbox(Point p1, Point p2, double w) {
        setBbox(boundingBox, p1, p2, w);
    }

    protected void setBbox(Rectangle rect, Point p1, Point p2, double w) {
        setBbox(rect, p1.x, p1.y, p2.x, p2.y);
        int dpx = (int) (calcDpx1(p1.x, p1.y, p2.x, p2.y) * w);
        int dpy = (int) (calcDpy1(p1.x, p1.y, p2.x, p2.y) * w);
        adjustBbox(rect, p1.x + dpx, p1.y + dpy, p1.x - dpx, p1.y - dpy);
    }

    public void adjustBbox(int x1, int y1, int x2, int y2) {
        adjustBbox(boundingBox, x1, y1, x2, y2);
    }

    protected void adjustBbox(Rectangle rect, int x1, int y1, int x2, int y2) {
        if (x1 > x2) {
            int q = x1;
            x1 = x2;
            x2 = q;
        }
        if (y1 > y2) {
            int q = y1;
            y1 = y2;
            y2 = q;
        }
        x1 = min(rect.x, x1);
        y1 = min(rect.y, y1);
        x2 = max(rect.x + rect.width - 1, x2);
        y2 = max(rect.y + rect.height - 1, y2);
        rect.setBounds(x1, y1, x2 - x1, y2 - y1);
    }

    public void adjustBbox(Point p1, Point p2) {
        adjustBBox(boundingBox, p1, p2);
    }

    protected void adjustBBox(Rectangle rect, Point p1, Point p2) {
        adjustBbox(rect, p1.x, p1.y, p2.x, p2.y);
    }

    public boolean isCenteredText() {
        return false;
    }

    public void drawCenteredText(DrawContext ctx, String s, int x, int y, boolean cx) {
        FontMetrics fm = ctx.getFontMetrics();
        int w = fm.stringWidth(s);
        if (cx)
            x -= w / 2;
        ctx.drawString(s, x, y + fm.getAscent() / 2);
        adjustBbox(x, y - fm.getAscent() / 2, x + w, y + fm.getAscent() / 2 + fm.getDescent());
    }

    public void drawValues(DrawContext ctx, String s, double hs) {
        if (s == null)
            return;
        ctx.setFont(ctx.unitsFont);
        FontMetrics fm = ctx.getFontMetrics();
        int w = fm.stringWidth(s);
        ctx.setColor(ctx.whiteColor);
        int ya = fm.getAscent() / 2;
        int xc, yc;
        if (this instanceof RailElm || this instanceof SweepElm) {
            xc = x2;
            yc = y2;
        } else {
            xc = (x2 + x) / 2;
            yc = (y2 + y) / 2;
        }
        int dpx = (int) (dpx1 * hs);
        int dpy = (int) (dpy1 * hs);
        if (dpx == 0) { //horizontal text
            ctx.drawString(s, xc - w / 2, yc - abs(dpy) - 2);
        } else { // vertical text
            int xx = xc + abs(dpx) + 2;
            if (this instanceof VoltageElm || (x < x2 && y > y2))
                xx = xc - (w + abs(dpx) + 2);
            ctx.drawString(s, xx, yc + dpy + ya);
        }
    }

    public void drawCoil(DrawContext ctx, int hs, Point p1, Point p2, double v1, double v2) {
        int segments = 30; // 10*(int) (len/10);
        int i;
        double segf = 1. / segments;

        ps1.setLocation(p1);
        for (i = 0; i != segments; i++) {
            double cx = (((i + 1) * 6. * segf) % 2) - 1;
            double hsx = Math.sqrt(1 - cx * cx);
            if (hsx < 0)
                hsx = -hsx;
            interpPoint(p1, p2, ps2, i * segf, hsx * hs);
            double v = v1 + (v2 - v1) * i / segments;
            setVoltageColor(ctx, v);
            ctx.drawThickLine(ps1, ps2);
            ps1.setLocation(ps2);
        }
    }

    public void updateDotCount(DrawContext ctx) {
        curcount = updateDotCount(ctx, current, curcount);
    }

    public double updateDotCount(DrawContext ctx, double cur, double cc) {
//        if (sim.stoppedCheck.getState())
//            return cc;
        double cadd = cur * ctx.currentMult;
    /*if (cur != 0 && cadd <= .05 && cadd >= -.05)
      cadd = (cadd < 0) ? -.05 : .05;*/
        cadd %= 8;
    /*if (cadd > 8)
      cadd = 8;
	  if (cadd < -8)
	  cadd = -8;*/
        return cc + cadd;
    }

    public void doDots(DrawContext ctx) {
        updateDotCount(ctx);
        if (engine.getDraggedElement() != this)
            drawDots(ctx, point1, point2, curcount);
    }

    public void getInfo(DrawContext ctx, String arr[]) {
    }

    public int getBasicInfo(DrawContext ctx, String arr[]) {
        arr[1] = "I = " + ctx.getCurrentDText(getCurrent());
        arr[2] = "Vd = " + ctx.getVoltageDText(getVoltageDiff());
        return 3;
    }

    public void setVoltageColor(DrawContext ctx, double volts) {
        if (needsHighlight(ctx)) {
            ctx.setColor(ctx.selectColor);
            return;
        }

        if (!ctx.getSim().getVoltsCheckItem().getState()) {
            if (!ctx.getSim().getPowerCheckItem().getState()) // && !conductanceCheckItem.getState())
                ctx.setColor(ctx.whiteColor);
            return;
        }

        int c = (int) ((volts + engine.voltageRange) * (DrawContext.colorScaleCount - 1) /
                (engine.voltageRange * 2));
        if (c < 0)
            c = 0;
        if (c >= DrawContext.colorScaleCount)
            c = DrawContext.colorScaleCount - 1;
        ctx.setColor(ctx.colorScale[c]);
    }

    public void setPowerColor(DrawContext ctx) {
    /*if (conductanceCheckItem.getState()) {
      setConductanceColor(g, current/getVoltageDiff());
	  return;
	  }*/
//        if (!sim.powerCheckItem.getState())
//            return;
        setPowerColor(ctx, getPower());
    }

    public void setPowerColor(DrawContext ctx, @SuppressWarnings("unused") boolean yellow) {
        /*
         * if (conductanceCheckItem.getState()) { setConductanceColor(g,
         * current/getVoltageDiff()); return; }
         */
        if (!ctx.getSim().getPowerCheckItem().getState())
            return;
        setPowerColor(ctx, getPower());
    }

    public void setPowerColor(DrawContext ctx, double w0) {
        w0 *= ctx.powerMult;
        //System.out.println(w);
        double w = (w0 < 0) ? -w0 : w0;
        if (w > 1)
            w = 1;
        int rg = 128 + (int) (w * 127);
        int b = (int) (128 * (1 - w));
    /*if (yellow)
      g.setColor(new Color(rg, rg, b));
	  else */
        if (w0 > 0)
            ctx.setColor(new Color(rg, b, b));
        else
            ctx.setColor(new Color(b, rg, b));
    }

    public void setConductanceColor(DrawContext ctx, double w0) {
        w0 *= ctx.powerMult;
        //System.out.println(w);
        double w = (w0 < 0) ? -w0 : w0;
        if (w > 1)
            w = 1;
        int rg = (int) (w * 255);
        ctx.setColor(new Color(rg, rg, rg));
    }

    public double getPower() {
        return getVoltageDiff() * current;
    }

    public double getScopeValue(int x) {
        return (x == 1) ? getPower() : getVoltageDiff();
    }

    public String getScopeUnits(int x) {
        return (x == 1) ? "W" : "V";
    }

    @Override
    public EditInfo getEditInfo(int n) {
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
    }

    public boolean getConnection(int n1, int n2) {
        return true;
    }

    public boolean hasGroundConnection(int n1) {
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isWire() {
        return false;
    }

    public boolean canViewInScope() {
        return getPostCount() <= 2;
    }

    public boolean comparePair(int x1, int x2, int y1, int y2) {
        return ((x1 == y1 && x2 == y2) || (x1 == y2 && x2 == y1));
    }

    public boolean needsHighlight(DrawContext ctx) {
        return ctx.getSim().mouseElm == this || selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean x) {
        selected = x;
    }

    public void selectRect(Rectangle r) {
        selected = r.intersects(boundingBox);
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public boolean isBasicBoundingBoxSupported() {
        return false;
    }

    /**
     * Basic bounding box is defined as bounding box of an element that is placed horizontally. This provides simple
     * way to check whether given point is inside bounds of an rotated element.
     *
     * @param tempP1 point defining center left side of the bounds
     * @param tempP2 point defining center right side of the bounds
     * @param result result of this method should be stored here
     */
    public void getBasicBoundingBox(Point tempP1, Point tempP2, Rectangle result) {
        throw new UnsupportedOperationException("Basic rounding box is not supported for this CircuitElm");
    }

    public boolean needsShortcut() {
        return false;
    }
}
