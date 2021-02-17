package org.circuitsymphony.element.active;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;
import org.circuitsymphony.util.Accessor;

import java.awt.*;
import java.util.StringTokenizer;

public class ThermistorElm extends CircuitElm {
    private double minresistance;
    private double maxresistance;
    private double resistance;
    private Scrollbar slider;
    private Accessor<Integer> sliderValueAccessor;
    private Label label;
    private Point ps3;
    private Point ps4;
    private int hs = 6;

    public ThermistorElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        maxresistance = 1e9;
        minresistance = 1e3;
        createSlider();
    }

    public ThermistorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                         StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public ThermistorElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                         StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        minresistance = new Double(st.nextToken());
        maxresistance = new Double(st.nextToken());
        createSlider();
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public int getDumpType() {
        return 188;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + minresistance + " " + maxresistance;
    }

    private void createSlider() {
        int value = 50;
        if (engine.isUISupported()) {
            engine.createUI(label = new Label("Temperature", Label.CENTER));
            engine.createUI(slider = new Scrollbar(Scrollbar.HORIZONTAL, value, 1, 0, 101));
            sliderValueAccessor = () -> slider.getValue();
        } else {
            sliderValueAccessor = () -> value;
        }
    }

    @Override
    public void setPoints() {
        super.setPoints();
        calcLeads(32);
        ps3 = new Point();
        ps4 = new Point();
    }

    @Override
    public void delete() {
        if (engine.isUISupported()) {
            engine.removeUI(label);
            engine.removeUI(slider);
        }
    }

    @Override
    public void draw(DrawContext ctx) {
        setBbox(point1, point2, hs);
        draw2Leads(ctx);
        // FIXME need to draw properly, see ResistorElm.java
        setPowerColor(ctx, true);
        doDots(ctx);
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

    @Override
    public void calculateCurrent() {
        double vd = volts[0] - volts[1];
        current = vd / resistance;
    }

    @Override
    public void startIteration() {
        // FIXME set resistance as appropriate, using slider.getValue()
        resistance = minresistance;
        //System.out.print(this + " res current set to " + current + "\n");
    }

    @Override
    public void doStep() {
        engine.stampResistor(nodes[0], nodes[1], resistance);
    }

    @Override
    public void stamp() {
        engine.stampNonLinear(nodes[0]);
        engine.stampNonLinear(nodes[1]);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        // FIXME
        arr[0] = "spark gap";
        getBasicInfo(ctx, arr);
        arr[3] = "R = " + ctx.getUnitText(resistance, CirSim.ohmString);
        arr[4] = "Ron = " + ctx.getUnitText(minresistance, CirSim.ohmString);
        arr[5] = "Roff = " + ctx.getUnitText(maxresistance, CirSim.ohmString);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        // ohmString doesn't work here on linux
        if (n == 0)
            return new EditInfo("Min resistance (ohms)", minresistance, 0, 0);
        if (n == 1)
            return new EditInfo("Max resistance (ohms)", maxresistance, 0, 0);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (ei.getValue() > 0 && n == 0)
            minresistance = ei.getValue();
        if (ei.getValue() > 0 && n == 1)
            maxresistance = ei.getValue();
    }
}

