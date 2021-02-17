package org.circuitsymphony.util;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.element.active.TransistorElm;
import org.circuitsymphony.element.devices.ProbeElm;
import org.circuitsymphony.element.io.LogicOutputElm;
import org.circuitsymphony.element.io.OutputElm;
import org.circuitsymphony.element.passive.MemristorElm;
import org.circuitsymphony.element.passive.WindowMemristorElm;
import org.circuitsymphony.element.passive.VTEAMMemristorElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.MemoryImageSource;
import java.util.StringTokenizer;


public class Scope {

    public static final int VAL_IB = 1;
    public static final int VAL_IC = 2;
    public static final int VAL_IE = 3;
    public static final int VAL_VBE = 4;
    public static final int VAL_VBC = 5;
    public static final int VAL_VCE = 6;

    private static final int VAL_POWER = 1;
    private static final int VAL_R = 2;
    private final int FLAG_YELM = 32;
    private final CirSim sim;
    private double[] minV;
    private double[] maxV;
    private double minMaxV;
    private double[] minI;
    private double[] maxI;
    private double minMaxI;
    private int scopePointCount = 128;
    private int ptr;
    private int ctr;
    private int value;
    private int ivalue;
    private String text;
    private boolean showI;
    private boolean showV;
    private boolean showFreq;
    private boolean lockScale;
    private boolean plot2d;
    private boolean plotXY;
    private CircuitElm yElm;
    private MemoryImageSource imageSource;
    private Image image;
    private int[] pixels;
    private int draw_ox;
    private int draw_oy;
    private float[] dpixels;
    private int speed;
    private int position;
    private Rectangle rect;
    private boolean showMax;
    private boolean showMin;
    private CircuitElm elm;

    public final static int SCOPE_DATA_ENTRY_COUNT = 10000;
    private CircularList<ScopeDataEntry> dataEntries;

    public Scope(CirSim sim) {
        rect = new Rectangle();
        dataEntries = new CircularList<>(SCOPE_DATA_ENTRY_COUNT);
        reset();
        this.sim = sim;
    }

    private void showCurrent(boolean b) {
        showI = b;
        value = ivalue = 0;
    }

    private void showVoltage(boolean b) {
        showV = b;
        value = ivalue = 0;
    }

    private void showMax(boolean b) {
        showMax = b;
    }

    private void showMin(boolean b) {
        showMin = b;
    }

    private void showFreq(boolean b) {
        showFreq = b;
    }

    public void setLockScale(boolean b) {
        lockScale = b;
    }

    public void resetGraph() {
        dataEntries.clear();
        scopePointCount = 1;
        while (scopePointCount <= rect.width)
            scopePointCount *= 2;
        minV = new double[scopePointCount];
        maxV = new double[scopePointCount];
        minI = new double[scopePointCount];
        maxI = new double[scopePointCount];
        ptr = ctr = 0;
        allocImage();
    }

    public boolean active() {
        return elm != null;
    }

    private void reset() {
        resetGraph();
        dataEntries.clear();
        minMaxV = 5;
        minMaxI = .1;
        speed = 64;
        showI = showV = showMax = true;
        showFreq = lockScale = showMin = false;
        plot2d = false;
        // no showI for Output
        if (elm != null && (elm instanceof OutputElm ||
                elm instanceof LogicOutputElm ||
                elm instanceof ProbeElm))
            showI = false;
        value = ivalue = 0;
        if (elm instanceof TransistorElm)
            value = VAL_VCE;
    }

    public int getWidth() {
        return rect.width;
    }

    public int rightEdge() {
        return rect.x + rect.width;
    }

    public void timeStep() {
        if (elm == null)
            return;
        double v = elm.getScopeValue(value);
        if (v < minV[ptr])
            minV[ptr] = v;
        if (v > maxV[ptr])
            maxV[ptr] = v;
        double i = 0;
        if (value == 0 || ivalue != 0) {
            i = (ivalue == 0) ? elm.getCurrent() : elm.getScopeValue(ivalue);
            if (i < minI[ptr])
                minI[ptr] = i;
            if (i > maxI[ptr])
                maxI[ptr] = i;
        }

        if (plot2d && dpixels != null) {
            boolean newscale = false;
            while (v > minMaxV || v < -minMaxV) {
                minMaxV *= 2;
                newscale = true;
            }
            double yval = i;
            if (plotXY)
                yval = (yElm == null) ? 0 : yElm.getVoltageDiff();
            while (yval > minMaxI || yval < -minMaxI) {
                minMaxI *= 2;
                newscale = true;
            }
            if (newscale)
                clear2dView();
            double xa = v / minMaxV;
            double ya = yval / minMaxI;
            int x = (int) (rect.width * (1 + xa) * .499);
            int y = (int) (rect.height * (1 - ya) * .499);
            drawTo(x, y);
        } else {
            ctr++;
            if (ctr >= speed) {
                ptr = (ptr + 1) & (scopePointCount - 1);
                dataEntries.put(new ScopeDataEntry(sim.getCircuitEngine().t, v, i));
                minV[ptr] = maxV[ptr] = v;
                minI[ptr] = maxI[ptr] = i;
                ctr = 0;
            }
        }
    }

    private void drawTo(int x2, int y2) {
        if (draw_ox == -1) {
            draw_ox = x2;
            draw_oy = y2;
        }
        // need to draw a line from x1,y1 to x2,y2
        if (draw_ox == x2 && draw_oy == y2) {
            dpixels[x2 + rect.width * y2] = 1;
        } else if (CircuitElm.abs(y2 - draw_oy) > CircuitElm.abs(x2 - draw_ox)) {
            // y difference is greater, so we step along y's
            // from min to max y and calculate x for each step
            double sgn = CircuitElm.sign(y2 - draw_oy);
            int x, y;
            for (y = draw_oy; y != y2 + sgn; y += sgn) {
                x = draw_ox + (x2 - draw_ox) * (y - draw_oy) / (y2 - draw_oy);
                dpixels[x + rect.width * y] = 1;
            }
        } else {
            // x difference is greater, so we step along x's
            // from min to max x and calculate y for each step
            double sgn = CircuitElm.sign(x2 - draw_ox);
            int x, y;
            for (x = draw_ox; x != x2 + sgn; x += sgn) {
                y = draw_oy + (y2 - draw_oy) * (x - draw_ox) / (x2 - draw_ox);
                dpixels[x + rect.width * y] = 1;
            }
        }
        draw_ox = x2;
        draw_oy = y2;
    }

    private void clear2dView() {
        int i;
        for (i = 0; i != dpixels.length; i++)
            dpixels[i] = 0;
        draw_ox = draw_oy = -1;
    }

    public void adjustScale(double x) {
        minMaxV *= x;
        minMaxI *= x;
    }

    private void draw2d(DrawContext ctx, Graphics g) {
        int i;
        if (pixels == null || dpixels == null)
            return;
        int col = (sim.printableCheckItem.getState()) ? 0xFFFFFFFF : 0;
        for (i = 0; i != pixels.length; i++)
            pixels[i] = col;
        for (i = 0; i != rect.width; i++)
            pixels[i + rect.width * (rect.height / 2)] = 0xFF00FF00;
        int ycol = (plotXY) ? 0xFF00FF00 : 0xFFFFFF00;
        for (i = 0; i != rect.height; i++)
            pixels[rect.width / 2 + rect.width * i] = ycol;
        for (i = 0; i != pixels.length; i++) {
            int q = (int) (255 * dpixels[i]);
            if (q > 0)
                pixels[i] = 0xFF000000 | (0x10101 * q);
            dpixels[i] *= .997;
        }
        g.drawImage(image, rect.x, rect.y, null);
        g.setColor(ctx.whiteColor);
        g.fillOval(rect.x + draw_ox - 2, rect.y + draw_oy - 2, 5, 5);
        int yt = rect.y + 10;
        int x = rect.x;
        if (text != null && rect.y + rect.height > yt + 5) {
            g.drawString(text, x, yt);
        }
    }

    public void draw(DrawContext ctx, Graphics g) {
        if (elm == null)
            return;
        if (plot2d) {
            draw2d(ctx, g);
            return;
        }
        if (pixels == null)
            return;
        int i;
        int col = (sim.printableCheckItem.getState()) ? 0xFFFFFFFF : 0;
        for (i = 0; i != pixels.length; i++)
            pixels[i] = col;
        int x = 0;
        int maxy = (rect.height - 1) / 2;

        boolean gotI = false;
        boolean gotV = false;
        int minRange = 4;
        double realMaxV = -1e8;
        double realMaxI = -1e8;
        double realMinV = 1e8;
        double realMinI = 1e8;
        int curColor = 0xFFFFFF00;
        int voltColor = (value > 0) ? 0xFFFFFFFF : 0xFF00FF00;
        if (sim.scopeSelected == -1 && elm == sim.mouseElm)
            curColor = voltColor = 0xFF00FFFF;
        int ipa = ptr + scopePointCount - rect.width;
        for (i = 0; i != rect.width; i++) {
            int ip = (i + ipa) & (scopePointCount - 1);
            while (maxV[ip] > minMaxV)
                minMaxV *= 2;
            while (minV[ip] < -minMaxV)
                minMaxV *= 2;
            while (maxI[ip] > minMaxI)
                minMaxI *= 2;
            while (minI[ip] < -minMaxI)
                minMaxI *= 2;
        }

        double gridStep = 1e-8;
        double gridMax = (showI ? minMaxI : minMaxV);
        while (gridStep * 100 < gridMax)
            gridStep *= 10;
        if (maxy * gridStep / gridMax < .3)
            gridStep = 0;

        int ll;
        boolean sublines = (maxy * gridStep / gridMax > 3);
        for (ll = -100; ll <= 100; ll++) {
            // don't show gridlines if plotting multiple values,
            // or if lines are too close together (except for center line)
            if (ll != 0 && ((showI && showV) || gridStep == 0))
                continue;
            int yl = maxy - (int) (maxy * ll * gridStep / gridMax);
            if (yl < 0 || yl >= rect.height - 1)
                continue;
            col = ll == 0 ? 0xFF909090 : 0xFF404040;
            if (ll % 10 != 0) {
                col = 0xFF101010;
                if (!sublines)
                    continue;
            }
            for (i = 0; i != rect.width; i++)
                pixels[i + yl * rect.width] = col;
        }

        gridStep = 1e-15;
        CircuitEngine engine = sim.getCircuitEngine();
        double ts = engine.timeStep * speed;
        while (gridStep < ts * 5)
            gridStep *= 10;
        double tstart = engine.t - engine.timeStep * speed * rect.width;
        double tx = engine.t - (engine.t % gridStep);
        for (ll = 0; ; ll++) {
            double tl = tx - gridStep * ll;
            int gx = (int) ((tl - tstart) / ts);
            if (gx < 0)
                break;
            if (gx >= rect.width)
                continue;
            if (tl < 0)
                continue;
            col = 0xFF202020;
            if (((tl + gridStep / 4) % (gridStep * 10)) < gridStep) {
                col = 0xFF909090;
                if (((tl + gridStep / 4) % (gridStep * 100)) < gridStep)
                    col = 0xFF4040D0;
            }
            for (i = 0; i < pixels.length; i += rect.width)
                pixels[i + gx] = col;
        }

        // these two loops are pretty much the same, and should be
        // combined!
        if (value == 0 && showI) {
            int ox = -1, oy = -1;
            int j;
            for (i = 0; i != rect.width; i++) {
                int ip = (i + ipa) & (scopePointCount - 1);
                int miniy = (int) ((maxy / minMaxI) * minI[ip]);
                int maxiy = (int) ((maxy / minMaxI) * maxI[ip]);
                if (maxI[ip] > realMaxI)
                    realMaxI = maxI[ip];
                if (minI[ip] < realMinI)
                    realMinI = minI[ip];
                if (miniy <= maxy) {
                    if (miniy < -minRange || maxiy > minRange)
                        gotI = true;
                    if (ox != -1) {
                        if (miniy == oy && maxiy == oy)
                            continue;
                        for (j = ox; j != x + i; j++)
                            pixels[j + rect.width * (maxy - oy)] = curColor;
                        ox = oy = -1;
                    }
                    if (miniy == maxiy) {
                        ox = x + i;
                        oy = miniy;
                        continue;
                    }
                    for (j = miniy; j <= maxiy; j++)
                        pixels[x + i + rect.width * (maxy - j)] = curColor;
                }
            }
            if (ox != -1)
                for (j = ox; j != x + i; j++)
                    pixels[j + rect.width * (maxy - oy)] = curColor;
        }
        if (value != 0 || showV) {
            int ox = -1, oy = -1, j;
            for (i = 0; i != rect.width; i++) {
                int ip = (i + ipa) & (scopePointCount - 1);
                int minvy = (int) ((maxy / minMaxV) * minV[ip]);
                int maxvy = (int) ((maxy / minMaxV) * maxV[ip]);
                if (maxV[ip] > realMaxV)
                    realMaxV = maxV[ip];
                if (minV[ip] < realMinV)
                    realMinV = minV[ip];
                if ((value != 0 || showV) && minvy <= maxy) {
                    if (minvy < -minRange || maxvy > minRange)
                        gotV = true;
                    if (ox != -1) {
                        if (minvy == oy && maxvy == oy)
                            continue;
                        for (j = ox; j != x + i; j++)
                            pixels[j + rect.width * (maxy - oy)] = voltColor;
                        ox = oy = -1;
                    }
                    if (minvy == maxvy) {
                        ox = x + i;
                        oy = minvy;
                        continue;
                    }
                    for (j = minvy; j <= maxvy; j++)
                        pixels[x + i + rect.width * (maxy - j)] = voltColor;
                }
            }
            if (ox != -1)
                for (j = ox; j != x + i; j++)
                    pixels[j + rect.width * (maxy - oy)] = voltColor;
        }
        double freq = 0;
        if (showFreq) {
            // try to get frequency
            // get average
            double avg = 0;
            for (i = 0; i != rect.width; i++) {
                int ip = (i + ipa) & (scopePointCount - 1);
                avg += minV[ip] + maxV[ip];
            }
            avg /= i * 2;
            int state = 0;
            double thresh = avg * .05;
            int oi = 0;
            double avperiod = 0;
            int periodct = -1;
            double avperiod2 = 0;
            // count period lengths
            for (i = 0; i != rect.width; i++) {
                int ip = (i + ipa) & (scopePointCount - 1);
                double q = maxV[ip] - avg;
                int os = state;
                if (q < thresh)
                    state = 1;
                else if (q > -thresh)
                    state = 2;
                if (state == 2 && os == 1) {
                    int pd = i - oi;
                    oi = i;
                    // short periods can't be counted properly
                    if (pd < 12)
                        continue;
                    // skip first period, it might be too short
                    if (periodct >= 0) {
                        avperiod += pd;
                        avperiod2 += pd * pd;
                    }
                    periodct++;
                }
            }
            avperiod /= periodct;
            avperiod2 /= periodct;
            double periodstd = Math.sqrt(avperiod2 - avperiod * avperiod);
            freq = 1 / (avperiod * engine.timeStep * speed);
            // don't show freq if standard deviation is too great
            if (periodct < 1 || periodstd > 2)
                freq = 0;
            // System.out.println(freq + " " + periodstd + " " + periodct);
        }
        g.drawImage(image, rect.x, rect.y, null);
        g.setColor(ctx.whiteColor);
        int yt = rect.y + 10;
        x += rect.x;
        if (showMax) {
            if (value != 0)
                g.drawString(ctx.getUnitText(realMaxV,
                        elm.getScopeUnits(value)),
                        x, yt);
            else if (showV)
                g.drawString(ctx.getVoltageText(realMaxV), x, yt);
            else if (showI)
                g.drawString(ctx.getCurrentText(realMaxI), x, yt);
            yt += 15;
        }
        if (showMin) {
            int ym = rect.y + rect.height - 5;
            if (value != 0)
                g.drawString(ctx.getUnitText(realMinV,
                        elm.getScopeUnits(value)),
                        x, ym);
            else if (showV)
                g.drawString(ctx.getVoltageText(realMinV), x, ym);
            else if (showI)
                g.drawString(ctx.getCurrentText(realMinI), x, ym);
        }
        if (text != null && rect.y + rect.height > yt + 5) {
            g.drawString(text, x, yt);
            yt += 15;
        }
        if (showFreq && freq != 0 && rect.y + rect.height > yt + 5)
            g.drawString(ctx.getUnitText(freq, "Hz"), x, yt);
        if (ptr > 5 && !lockScale) {
            if (!gotI && minMaxI > 1e-4)
                minMaxI /= 2;
            if (!gotV && minMaxV > 1e-4)
                minMaxV /= 2;
        }
    }

    public void speedUp() {
        if (speed > 1) {
            speed /= 2;
            resetGraph();
        }
    }

    public void slowDown() {
        speed *= 2;
        resetGraph();
    }

    public PopupMenu getMenu() {
        if (elm == null)
            return null;
        if (elm instanceof TransistorElm) {
            sim.scopeIbMenuItem.setState(value == VAL_IB);
            sim.scopeIcMenuItem.setState(value == VAL_IC);
            sim.scopeIeMenuItem.setState(value == VAL_IE);
            sim.scopeVbeMenuItem.setState(value == VAL_VBE);
            sim.scopeVbcMenuItem.setState(value == VAL_VBC);
            sim.scopeVceMenuItem.setState(value == VAL_VCE && ivalue != VAL_IC);
            sim.scopeVceIcMenuItem.setState(value == VAL_VCE && ivalue == VAL_IC);
            return sim.transScopeMenu;
        } else {
            sim.scopeVMenuItem.setState(showV && value == 0);
            sim.scopeIMenuItem.setState(showI && value == 0);
            sim.scopeMaxMenuItem.setState(showMax);
            sim.scopeMinMenuItem.setState(showMin);
            sim.scopeFreqMenuItem.setState(showFreq);
            sim.scopePowerMenuItem.setState(value == VAL_POWER);
            sim.scopeVIMenuItem.setState(plot2d && !plotXY);
            sim.scopeXYMenuItem.setState(plotXY);
            sim.scopeSelectYMenuItem.setEnabled(plotXY);
            sim.scopeResistMenuItem.setState(value == VAL_R);
//            sim.scopeResistMenuItem.setEnabled(elm instanceof MemristorElm || elm instanceof WindowMemristorElm || elm instanceof  VTEAMMemristorElm);
            sim.scopeResistMenuItem.setEnabled(true);
            return sim.scopeMenu;
        }
    }

    private void setValue(int x) {
        reset();
        value = x;
    }

    public String dump() {
        if (elm == null)
            return null;
        int flags = (showI ? 1 : 0) | (showV ? 2 : 0) |
                (showMax ? 0 : 4) |   // showMax used to be always on
                (showFreq ? 8 : 0) |
                (lockScale ? 16 : 0) | (plot2d ? 64 : 0) |
                (plotXY ? 128 : 0) | (showMin ? 256 : 0);
        flags |= FLAG_YELM; // yelm present
        int eno = sim.locateElm(elm);
        if (eno < 0)
            return null;
        int yno = yElm == null ? -1 : sim.locateElm(yElm);
        String x = "o " + eno + " " +
                speed + " " + value + " " + flags + " " +
                minMaxV + " " + minMaxI + " " + position + " " + yno;
        if (text != null)
            x += " " + text;
        return x;
    }

    public void undump(StringTokenizer st, boolean initElm) {
        reset();
        int e = new Integer(st.nextToken());
        if (e == -1)
            return;
        if (initElm) elm = sim.getElm(e);
        speed = new Integer(st.nextToken());
        value = new Integer(st.nextToken());
        int flags = new Integer(st.nextToken());
        minMaxV = new Double(st.nextToken());
        minMaxI = new Double(st.nextToken());
        if (minMaxV == 0)
            minMaxV = .5;
        if (minMaxI == 0)
            minMaxI = 1;
        text = null;
        yElm = null;
        try {
            position = new Integer(st.nextToken());
            int ye;
            if ((flags & FLAG_YELM) != 0) {
                ye = new Integer(st.nextToken());
                if (ye != -1)
                    yElm = sim.getElm(ye);
            }
            while (st.hasMoreTokens()) {
                if (text == null)
                    text = st.nextToken();
                else
                    text += " " + st.nextToken();
            }
        } catch (Exception ignored) {
        }
        showI = (flags & 1) != 0;
        showV = (flags & 2) != 0;
        showMax = (flags & 4) == 0;
        showFreq = (flags & 8) != 0;
        lockScale = (flags & 16) != 0;
        plot2d = (flags & 64) != 0;
        plotXY = (flags & 128) != 0;
        showMin = (flags & 256) != 0;
    }

    @SuppressWarnings("unchecked")
    private void allocImage() {
        pixels = null;
        int w = rect.width;
        int h = rect.height;
        if (w == 0 || h == 0)
            return;
        try {
            BufferedImage bufImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            image = bufImage;
            pixels = ((DataBufferInt) bufImage.getRaster().getDataBuffer()).getData();
        } catch (Exception ee) {
            System.out.println("BufferedImage failed");
        }
        if (pixels == null) {
            pixels = new int[w * h];
            int i;
            for (i = 0; i != w * h; i++)
                pixels[i] = 0xFF000000;
            imageSource = new MemoryImageSource(w, h, pixels, 0, w);
            imageSource.setAnimated(true);
            imageSource.setFullBufferUpdates(true);
            image = sim.getCircuitCanvas().createImage(imageSource);
        }
        dpixels = new float[w * h];
        draw_ox = draw_oy = -1;
    }

    public void handleMenu(Object mi) {
        if (mi == sim.scopeVMenuItem)
            showVoltage(sim.scopeVMenuItem.getState());
        if (mi == sim.scopeIMenuItem)
            showCurrent(sim.scopeIMenuItem.getState());
        if (mi == sim.scopeMaxMenuItem)
            showMax(sim.scopeMaxMenuItem.getState());
        if (mi == sim.scopeMinMenuItem)
            showMin(sim.scopeMinMenuItem.getState());
        if (mi == sim.scopeFreqMenuItem)
            showFreq(sim.scopeFreqMenuItem.getState());
        if (mi == sim.scopePowerMenuItem)
            setValue(VAL_POWER);
        if (mi == sim.scopeIbMenuItem)
            setValue(VAL_IB);
        if (mi == sim.scopeIcMenuItem)
            setValue(VAL_IC);
        if (mi == sim.scopeIeMenuItem)
            setValue(VAL_IE);
        if (mi == sim.scopeVbeMenuItem)
            setValue(VAL_VBE);
        if (mi == sim.scopeVbcMenuItem)
            setValue(VAL_VBC);
        if (mi == sim.scopeVceMenuItem)
            setValue(VAL_VCE);
        if (mi == sim.scopeVceIcMenuItem) {
            plot2d = true;
            plotXY = false;
            value = VAL_VCE;
            ivalue = VAL_IC;
            resetGraph();
        }

        if (mi == sim.scopeVIMenuItem) {
            plot2d = sim.scopeVIMenuItem.getState();
            plotXY = false;
            resetGraph();
        }
        if (mi == sim.scopeXYMenuItem) {
            plotXY = plot2d = sim.scopeXYMenuItem.getState();
            if (yElm == null)
                selectY();
            resetGraph();
        }
        if (mi == sim.scopeResistMenuItem)
            setValue(VAL_R);
    }

    public void select() {
        sim.mouseElm = elm;
        if (plotXY) {
            sim.plotXElm = elm;
            sim.plotYElm = yElm;
        }
    }

    public void selectY() {
        int e = yElm == null ? -1 : sim.locateElm(yElm);
        int firstE = e;
        while (true) {
            for (e++; e < sim.getCircuitEngine().elmList.size(); e++) {
                CircuitElm ce = sim.getElm(e);
                if ((ce instanceof OutputElm || ce instanceof ProbeElm) &&
                        ce != elm) {
                    yElm = ce;
                    return;
                }
            }
            if (firstE == -1)
                return;
            e = firstE = -1;
        }
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public Rectangle getRect() {
        return rect;
    }

    public void setRect(Rectangle r) {
        rect = r;
        resetGraph();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isShowMax() {
        return showMax;
    }

    public void setShowMax(boolean showMax) {
        this.showMax = showMax;
    }

    public CircuitElm getElm() {
        return elm;
    }

    public void setElm(CircuitElm ce) {
        elm = ce;
        reset();
    }

    public boolean isShowMin() {
        return showMin;
    }

    public void setShowMin(boolean showMin) {
        this.showMin = showMin;
    }

    public CircularList<ScopeDataEntry> getDataEntries() {
        return dataEntries;
    }

    public class ScopeDataEntry {
        public double t;
        public double v;
        public double i;

        public ScopeDataEntry(double t, double v, double i) {
            this.t = t;
            this.v = v;
            this.i = i;
        }
    }
}
