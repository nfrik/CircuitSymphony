package org.circuitsymphony.element.io;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class VoltageElm extends CircuitElm {

    protected static final int WF_DC = 0;
    protected static final int WF_AC = 1;
    protected static final int WF_SQUARE = 2;
    protected static final int WF_VAR = 7;
    private static final int FLAG_COS = 2;
    private static final int WF_TRIANGLE = 3;
    private static final int WF_SAWTOOTH = 4;
    private static final int WF_PULSE = 5;
    private static final int WF_PULSE_SWEEP = 6;
    protected final int circleSize = 17;
    protected int waveform;
    protected double frequency;
    protected double maxVoltage;
    protected double bias;
    private double freqTimeZero;
    private double phaseShift;
    private double dutyCycle;
    private double pulseNumber;

    public VoltageElm(CircuitEngine engine, int xx, int yy, int wf) {
        super(engine, xx, yy);
        waveform = wf;
        maxVoltage = 5;
        frequency = 40;
        dutyCycle = .5;
        pulseNumber = 1000;
        reset();
    }

    public VoltageElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                      StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public VoltageElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                      StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        maxVoltage = 5;
        frequency = 40;
        waveform = WF_DC;
        dutyCycle = .5;
        pulseNumber = 1000;
        try {
            waveform = new Integer(st.nextToken());
            frequency = new Double(st.nextToken());
            maxVoltage = new Double(st.nextToken());
            bias = new Double(st.nextToken());
            phaseShift = new Double(st.nextToken());
            dutyCycle = new Double(st.nextToken());
        } catch (Exception e) {
        }
        if ((flags & FLAG_COS) != 0) {
            flags &= ~FLAG_COS;
            phaseShift = PI / 2;
        }
        reset();
    }

    @Override
    public int getDumpType() {
        return 'v';
    }
    /*void setCurrent(double c) {
      current = c;
      System.out.print("v current set to " + c + "\n");
      }*/

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + waveform + " " + frequency + " " +
                maxVoltage + " " + bias + " " + phaseShift + " " +
                dutyCycle;
    }

    @Override
    public void reset() {
        freqTimeZero = 0;
        curcount = 0;
    }

    private double triangleFunc(double x) {
        if (x < PI)
            return x * (2 / PI) - 1;
        return 1 - (x - PI) * (2 / PI);
    }

    @Override
    public void stamp() {
        if (waveform == WF_DC)
            engine.stampVoltageSource(nodes[0], nodes[1], voltSource,
                    getVoltage());
        else
            engine.stampVoltageSource(nodes[0], nodes[1], voltSource);
    }

    @Override
    public void doStep() {
        if (waveform != WF_DC)
            engine.updateVoltageSource(voltSource, getVoltage());
    }

    public double getVoltage() {
        double w = 2 * PI * (engine.t - freqTimeZero) * frequency + phaseShift;
        switch (waveform) {
            case WF_DC:
                return maxVoltage + bias;
            case WF_AC:
                return Math.sin(w) * maxVoltage + bias;
            case WF_SQUARE:
                return bias + ((w % (2 * PI) > (2 * PI * dutyCycle)) ? -maxVoltage : maxVoltage);
            case WF_TRIANGLE:
                return bias + triangleFunc(w % (2 * PI)) * maxVoltage;
            case WF_SAWTOOTH:
                return bias + (w % (2 * PI)) * (maxVoltage / PI) - maxVoltage;
            case WF_PULSE:
                return ((w % (2 * PI)) < 1) ? maxVoltage + bias : bias;
            case WF_PULSE_SWEEP:
                return bias + triangleFunc(w % (2 * PI)) * maxVoltage * (Math.round((w % (2 * PI)) * pulseNumber / (2 * PI)) % 2 == 0 ? 0 : 1);
            default:
                return 0;
        }
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads((waveform == WF_DC || waveform == WF_VAR) ? 8 : circleSize * 2);
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(x, y, x2, y2);
        draw2Leads(ctx);
        if (waveform == WF_DC) {
            setPowerColor(ctx, false);
            setVoltageColor(ctx, volts[0]);
            interpPoint2(lead1, lead2, ps1, ps2, 0, 10);
            ctx.drawThickLine(ps1, ps2);
            setVoltageColor(ctx, volts[1]);
            int hs = 16;
            setBbox(point1, point2, hs);
            interpPoint2(lead1, lead2, ps1, ps2, 1, hs);
            ctx.drawThickLine(ps1, ps2);
        } else {
            setBbox(point1, point2, circleSize);
            interpPoint(lead1, lead2, ps1, .5);
            drawWaveform(ctx, ps1);
        }
        updateDotCount(ctx);
        if (engine.getDraggedElement() != this) {
            if (waveform == WF_DC)
                drawDots(ctx, point1, point2, curcount);
            else {
                drawDots(ctx, point1, lead1, curcount);
                drawDots(ctx, point2, lead2, -curcount);
            }
        }
        drawPosts(ctx);
    }

    public void drawWaveform(DrawContext ctx, Point center) {
        ctx.setColor(needsHighlight(ctx) ? ctx.selectColor : Color.gray);
        setPowerColor(ctx, false);
        int xc = center.x;
        int yc = center.y;
        ctx.drawThickCircle(xc, yc, circleSize);
        int wl = 8;
        adjustBbox(xc - circleSize, yc - circleSize,
                xc + circleSize, yc + circleSize);
        int xc2;
        switch (waveform) {
            case WF_DC: {
                break;
            }
            case WF_SQUARE:
                xc2 = (int) (wl * 2 * dutyCycle - wl + xc);
                xc2 = max(xc - wl + 3, min(xc + wl - 3, xc2));
                ctx.drawThickLine(xc - wl, yc - wl, xc - wl, yc);
                ctx.drawThickLine(xc - wl, yc - wl, xc2, yc - wl);
                ctx.drawThickLine(xc2, yc - wl, xc2, yc + wl);
                ctx.drawThickLine(xc + wl, yc + wl, xc2, yc + wl);
                ctx.drawThickLine(xc + wl, yc, xc + wl, yc + wl);
                break;
            case WF_PULSE:
                yc += wl / 2;
                ctx.drawThickLine(xc - wl, yc - wl, xc - wl, yc);
                ctx.drawThickLine(xc - wl, yc - wl, xc - wl / 2, yc - wl);
                ctx.drawThickLine(xc - wl / 2, yc - wl, xc - wl / 2, yc);
                ctx.drawThickLine(xc - wl / 2, yc, xc + wl, yc);
                break;
            case WF_PULSE_SWEEP:
                int j;
                int pl = 10;
                int oox = -1, ooy = -1;
                for (j = -pl; j <= pl; j++) {
                    int yy = yc + (int) (.95 * Math.sin(Math.pow(j, 2) * PI / pl) * wl);
                    if (oox != -1)
                        ctx.drawThickLine(oox, ooy, xc + j, yy);
                    oox = xc + j;
                    ooy = yy;
                }
                break;
            case WF_SAWTOOTH:
                ctx.drawThickLine(xc, yc - wl, xc - wl, yc);
                ctx.drawThickLine(xc, yc - wl, xc, yc + wl);
                ctx.drawThickLine(xc, yc + wl, xc + wl, yc);
                break;
            case WF_TRIANGLE: {
                int xl = 5;
                ctx.drawThickLine(xc - xl * 2, yc, xc - xl, yc - wl);
                ctx.drawThickLine(xc - xl, yc - wl, xc, yc);
                ctx.drawThickLine(xc, yc, xc + xl, yc + wl);
                ctx.drawThickLine(xc + xl, yc + wl, xc + xl * 2, yc);
                break;
            }
            case WF_AC: {
                int i;
                int xl = 10;
                int ox = -1, oy = -1;
                for (i = -xl; i <= xl; i++) {
                    int yy = yc + (int) (.95 * Math.sin(i * PI / xl) * wl);
                    if (ox != -1)
                        ctx.drawThickLine(ox, oy, xc + i, yy);
                    ox = xc + i;
                    oy = yy;
                }
                break;
            }
        }
        if (ctx.getSim().getShowValuesCheckItem().getState()) {
            String s = ctx.getShortUnitText(frequency, "Hz");
            if (dx == 0 || dy == 0)
                drawValues(ctx, s, circleSize);
        }
    }

    @Override
    public boolean isBasicBoundingBoxSupported() {
        return true;
    }

    @Override
    public void getBasicBoundingBox(Point tempP1, Point tempP2, Rectangle result) {
        if (waveform == WF_DC) {
            setBbox(result, tempP1, tempP2, 16);
        } else {
            setBbox(result, tempP1, tempP2, circleSize);
            int xc = tempP1.x;
            int yc = tempP1.y;
            adjustBbox(result, xc - circleSize, yc - circleSize,
                    xc + circleSize, yc + circleSize);
        }
    }

    @Override
    public int getVoltageSourceCount() {
        return 1;
    }

    @Override
    public double getPower() {
        return -getVoltageDiff() * current;
    }

    @Override
    public double getVoltageDiff() {
        return volts[1] - volts[0];
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        switch (waveform) {
            case WF_DC:
            case WF_VAR:
                arr[0] = "voltage source";
                break;
            case WF_AC:
                arr[0] = "A/C source";
                break;
            case WF_SQUARE:
                arr[0] = "square wave gen";
                break;
            case WF_PULSE:
                arr[0] = "pulse gen";
                break;
            case WF_PULSE_SWEEP:
                arr[0] = "pulse sweep";
                break;
            case WF_SAWTOOTH:
                arr[0] = "sawtooth gen";
                break;
            case WF_TRIANGLE:
                arr[0] = "triangle gen";
                break;
        }
        arr[1] = "I = " + ctx.getCurrentText(getCurrent());
        arr[2] = ((this instanceof RailElm) ? "V = " : "Vd = ") +
                ctx.getVoltageText(getVoltageDiff());
        if (waveform != WF_DC && waveform != WF_VAR) {
            arr[3] = "f = " + ctx.getUnitText(frequency, "Hz");
            arr[4] = "Vmax = " + ctx.getVoltageText(maxVoltage);
            int i = 5;
            if (bias != 0)
                arr[i++] = "Voff = " + ctx.getVoltageText(bias);
            else if (frequency > 500)
                arr[i++] = "wavelength = " + ctx.getUnitText(2.9979e8 / frequency, "m");
            arr[i++] = "P = " + ctx.getUnitText(getPower(), "W");
        }
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo(waveform == WF_DC ? "Voltage" :
                    "Max Voltage", maxVoltage, -20, 20);
        if (n == 1) {
            EditInfo ei = new EditInfo("Waveform", waveform, -1, -1);
            ei.setChoice(new Choice());
            ei.getChoice().add("D/C");
            ei.getChoice().add("A/C");
            ei.getChoice().add("Square Wave");
            ei.getChoice().add("Triangle");
            ei.getChoice().add("Sawtooth");
            ei.getChoice().add("Pulse");
            ei.getChoice().add("Pulse Sweep");
            ei.getChoice().select(waveform);
            return ei;
        }
        if (waveform == WF_DC)
            return null;
        if (n == 2)
            return new EditInfo("Frequency (Hz)", frequency, 4, 500);
        if (n == 3)
            return new EditInfo("DC Offset (V)", bias, -20, 20);
        if (n == 4)
            return new EditInfo("Phase Offset (degrees)", phaseShift * 180 / PI,
                    -180, 180).setDimensionless();
        if (n == 5)
            if (waveform == WF_PULSE_SWEEP) {
                return new EditInfo("Number of pulses in period", pulseNumber, 1, 1000).setDimensionless();
            } else if (waveform == WF_SQUARE) {
                return new EditInfo("Duty Cycle", dutyCycle * 100, 0, 100).
                        setDimensionless();
            }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            maxVoltage = ei.getValue();
        if (n == 3)
            bias = ei.getValue();
        if (n == 2) {
            // adjust time zero to maintain continuity ind the waveform
            // even though the frequency has changed.
            double oldfreq = frequency;
            frequency = ei.getValue();
            double maxfreq = 1 / (8 * engine.timeStep);
            if (frequency > maxfreq)
                frequency = maxfreq;
            freqTimeZero = engine.t - oldfreq * (engine.t - freqTimeZero) / frequency;
        }
        if (n == 1) {
            int ow = waveform;
            waveform = ei.getChoice().getSelectedIndex();
            if (waveform == WF_DC && ow != WF_DC) {
                ei.setNewDialog(true);
                bias = 0;
            } else if (waveform != WF_DC && ow == WF_DC) {
                ei.setNewDialog(true);
            }
            if ((waveform == WF_SQUARE || ow == WF_SQUARE) &&
                    waveform != ow)
                ei.setNewDialog(true);
            setPoints();
        }
        if (n == 4)
            phaseShift = ei.getValue() * PI / 180;
        if (n == 5) {
            if (waveform == WF_PULSE_SWEEP) {
                pulseNumber = ei.getValue();
            } else if (waveform == WF_SQUARE) {
                dutyCycle = ei.getValue() * .01;
            }
        }
    }

    public void setMaxVoltage(double maxVoltage) {
        this.maxVoltage = maxVoltage;
    }
    public double getMaxVoltage() {
        return this.maxVoltage;
    }
}
