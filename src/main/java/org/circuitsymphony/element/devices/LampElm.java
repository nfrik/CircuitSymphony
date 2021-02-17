package org.circuitsymphony.element.devices;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;

public class LampElm extends CircuitElm {
    private final double roomTemp = 300;
    private final int filament_len = 24;
    private double resistance;
    private double temp;
    private double nom_pow;
    private double nom_v;
    private double warmTime;
    private double coolTime;
    private Point[] bulbLead;
    private Point[] filament;
    private Point bulb;
    private int bulbR;

    public LampElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        temp = roomTemp;
        nom_pow = 100;
        nom_v = 120;
        warmTime = .4;
        coolTime = .4;
    }

    public LampElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                   StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public LampElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                   StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        temp = new Double(st.nextToken());
        nom_pow = new Double(st.nextToken());
        nom_v = new Double(st.nextToken());
        warmTime = new Double(st.nextToken());
        coolTime = new Double(st.nextToken());
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + temp + " " + nom_pow + " " + nom_v +
                " " + warmTime + " " + coolTime;
    }

    @Override
    public int getDumpType() {
        return 181;
    }

    @Override
    public void reset() {
        super.reset();
        temp = roomTemp;
    }

    @Override
    public void setPoints() {
        super.setPoints();
        int llen = 16;
        calcLeads(llen);
        bulbLead = newPointArray(2);
        filament = newPointArray(2);
        bulbR = 20;
        filament[0] = interpPoint(lead1, lead2, 0, filament_len);
        filament[1] = interpPoint(lead1, lead2, 1, filament_len);
        double br = filament_len - Math.sqrt(bulbR * bulbR - llen * llen);
        bulbLead[0] = interpPoint(lead1, lead2, 0, br);
        bulbLead[1] = interpPoint(lead1, lead2, 1, br);
        bulb = interpPoint(filament[0], filament[1], .5);
    }

    private Color getTempColor() {
        if (temp < 1200) {
            int x = (int) (255 * (temp - 800) / 400);
            if (x < 0)
                x = 0;
            return new Color(x, 0, 0);
        }
        if (temp < 1700) {
            int x = (int) (255 * (temp - 1200) / 500);
            if (x < 0)
                x = 0;
            return new Color(255, x, 0);
        }
        if (temp < 2400) {
            int x = (int) (255 * (temp - 1700) / 700);
            if (x < 0)
                x = 0;
            return new Color(255, 255, x);
        }
        return Color.white;
    }

    @Override
    public void draw(DrawContext ctx) {
        double v1 = volts[0];
        double v2 = volts[1];
        setBbox(point1, point2, 4);
        adjustBbox(bulb.x - bulbR, bulb.y - bulbR,
                bulb.x + bulbR, bulb.y + bulbR);
        // adjustbbox
        draw2Leads(ctx);
        setPowerColor(ctx, true);
        ctx.setColor(getTempColor());
        ctx.fillOval(bulb.x - bulbR, bulb.y - bulbR, bulbR * 2, bulbR * 2);
        ctx.setColor(Color.white);
        ctx.drawThickCircle(bulb.x, bulb.y, bulbR);
        setVoltageColor(ctx, v1);
        ctx.drawThickLine(lead1, filament[0]);
        setVoltageColor(ctx, v2);
        ctx.drawThickLine(lead2, filament[1]);
        setVoltageColor(ctx, (v1 + v2) * .5);
        ctx.drawThickLine(filament[0], filament[1]);
        updateDotCount(ctx);
        if (engine.getDraggedElement() != this) {
            drawDots(ctx, point1, lead1, curcount);
            double cc = curcount + (dn - 16) / 2;
            drawDots(ctx, lead1, filament[0], cc);
            cc += filament_len;
            drawDots(ctx, filament[0], filament[1], cc);
            cc += 16;
            drawDots(ctx, filament[1], lead2, cc);
            drawDots(ctx, lead2, point2, curcount);
        }
        drawPosts(ctx);
    }

    @Override
    public void calculateCurrent() {
        current = (volts[0] - volts[1]) / resistance;
        //System.out.print(this + " res current set to " + current + "\n");
    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[0]);
        engine.stampNonLinear(nodes[1]);
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public void startIteration() {
        // based on http://www.intusoft.com/nlpdf/nl11.pdf
        double nom_r = nom_v * nom_v / nom_pow;
        // this formula doesn't work for values over 5390
        double tp = (temp > 5390) ? 5390 : temp;
        resistance = nom_r * (1.26104 -
                4.90662 * Math.sqrt(17.1839 / tp - 0.00318794) -
                7.8569 / (tp - 187.56));
        double cap = 1.57e-4 * nom_pow;
        double capw = cap * warmTime / .4;
        double capc = cap * coolTime / .4;
        //System.out.println(nom_r + " " + (resistance/nom_r));
        temp += getPower() * engine.timeStep / capw;
        double cr = 2600 / nom_pow;
        temp -= engine.timeStep * (temp - roomTemp) / (capc * cr);
        //System.out.println(capw + " " + capc + " " + temp + " " +resistance);
    }

    @Override
    public void doStep() {
        engine.stampResistor(nodes[0], nodes[1], resistance);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "lamp";
        getBasicInfo(ctx, arr);
        arr[3] = "R = " + ctx.getUnitText(resistance, CirSim.ohmString);
        arr[4] = "P = " + ctx.getUnitText(getPower(), "W");
        arr[5] = "T = " + ((int) temp) + " K";
    }

    @Override
    public EditInfo getEditInfo(int n) {
        // ohmString doesn't work here on linux
        if (n == 0)
            return new EditInfo("Nominal Power", nom_pow, 0, 0);
        if (n == 1)
            return new EditInfo("Nominal Voltage", nom_v, 0, 0);
        if (n == 2)
            return new EditInfo("Warmup Time (s)", warmTime, 0, 0);
        if (n == 3)
            return new EditInfo("Cooldown Time (s)", coolTime, 0, 0);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0 && ei.getValue() > 0)
            nom_pow = ei.getValue();
        if (n == 1 && ei.getValue() > 0)
            nom_v = ei.getValue();
        if (n == 2 && ei.getValue() > 0)
            warmTime = ei.getValue();
        if (n == 3 && ei.getValue() > 0)
            coolTime = ei.getValue();
    }
}
