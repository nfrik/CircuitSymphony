package org.circuitsymphony.element.passive;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;
import org.circuitsymphony.util.Accessor;

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.StringTokenizer;

public class PotElm extends CircuitElm implements AdjustmentListener {
    private double position;
    private double maxResistance;
    private double resistance1;
    private double resistance2;
    private double current1;
    private double current2;
    private double current3;
    private double curcount1;
    private double curcount2;
    private double curcount3;
    private Scrollbar slider;
    private Accessor<Integer> sliderValueAccessor;
    private Label label;
    private String sliderText;
    private Point post3;
    private Point corner2;
    private Point arrowPoint;
    private Point midpoint;
    private Point arrow1;
    private Point arrow2;
    private Point ps3;
    private Point ps4;

    public PotElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        maxResistance = 1000;
        position = .5;
        sliderText = "Resistance";
        createSlider();
    }

    public PotElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                  StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public PotElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                  StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        maxResistance = new Double(st.nextToken());
        position = new Double(st.nextToken());
        sliderText = st.nextToken();
        while (st.hasMoreTokens())
            sliderText += ' ' + st.nextToken();
        createSlider();
    }

    @Override
    public int getPostCount() {
        return 3;
    }

    @Override
    public int getDumpType() {
        return 174;
    }

    @Override
    public Point getPost(int n) {
        return (n == 0) ? point1 : (n == 1) ? point2 : post3;
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + maxResistance + " " +
                position + " " + sliderText;
    }

    private void createSlider() {
        int value = (int) (position * 100);
        if (engine.isUISupported()) {
            engine.createUI(label = new Label(sliderText, Label.CENTER));
            engine.createUI(slider = new Scrollbar(Scrollbar.HORIZONTAL, value, 1, 0, 101));
            slider.addAdjustmentListener(this);
            sliderValueAccessor = () -> slider.getValue();
        } else {
            sliderValueAccessor = () -> value;
        }
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        engine.setAnalyzeFlag();
        setPoints();
    }

    @Override
    public void delete() {
        if (engine.isUISupported()) {
            engine.removeUI(label);
            engine.removeUI(slider);
        }
    }

    @Override
    public void setPoints() {
        super.setPoints();
        int offset;
        if (abs(dx) > abs(dy)) {
            dx = engine.snapGrid(dx / 2) * 2;
            point2.x = x2 = point1.x + dx;
            offset = (dx < 0) ? dy : -dy;
            point2.y = point1.y;
        } else {
            dy = engine.snapGrid(dy / 2) * 2;
            point2.y = y2 = point1.y + dy;
            offset = (dy > 0) ? dx : -dx;
            point2.x = point1.x;
        }
        if (offset == 0)
            offset = engine.getGridSize();
        dn = distance(point1, point2);
        int bodyLen = 32;
        calcLeads(bodyLen);
        position = sliderValueAccessor.get() * .0099 + .005;
        int soff = (int) ((position - .5) * bodyLen);
        //int offset2 = offset - sign(offset)*4;
        post3 = interpPoint(point1, point2, .5, offset);
        corner2 = interpPoint(point1, point2, soff / dn + .5, offset);
        arrowPoint = interpPoint(point1, point2, soff / dn + .5,
                8 * sign(offset));
        midpoint = interpPoint(point1, point2, soff / dn + .5);
        arrow1 = new Point();
        arrow2 = new Point();
        double clen = abs(offset) - 8;
        interpPoint2(corner2, arrowPoint, arrow1, arrow2, (clen - 8) / clen, 8);
        ps3 = new Point();
        ps4 = new Point();
    }

    @Override
    public void draw(DrawContext ctx) {
        int segments = 16;
        int i;
        int ox = 0;
        int hs = ctx.getSim().getEuroResistorCheckItem().getState() ? 6 : 8;
        double v1 = volts[0];
        double v2 = volts[1];
        double v3 = volts[2];
        setBbox(point1, point2, hs);
        draw2Leads(ctx);
        setPowerColor(ctx, true);
        double segf = 1. / segments;
        int divide = (int) (segments * position);
        if (!ctx.getSim().getEuroResistorCheckItem().getState()) {
            // draw zigzag
            for (i = 0; i != segments; i++) {
                int nx = 0;
                switch (i & 3) {
                    case 0:
                        nx = 1;
                        break;
                    case 2:
                        nx = -1;
                        break;
                    default:
                        nx = 0;
                        break;
                }
                double v = v1 + (v3 - v1) * i / divide;
                if (i >= divide)
                    v = v3 + (v2 - v3) * (i - divide) / (segments - divide);
                setVoltageColor(ctx, v);
                interpPoint(lead1, lead2, ps1, i * segf, hs * ox);
                interpPoint(lead1, lead2, ps2, (i + 1) * segf, hs * nx);
                ctx.drawThickLine(ps1, ps2);
                ox = nx;
            }
        } else {
            // draw rectangle
            setVoltageColor(ctx, v1);
            interpPoint2(lead1, lead2, ps1, ps2, 0, hs);
            ctx.drawThickLine(ps1, ps2);
            for (i = 0; i != segments; i++) {
                double v = v1 + (v3 - v1) * i / divide;
                if (i >= divide)
                    v = v3 + (v2 - v3) * (i - divide) / (segments - divide);
                setVoltageColor(ctx, v);
                interpPoint2(lead1, lead2, ps1, ps2, i * segf, hs);
                interpPoint2(lead1, lead2, ps3, ps4, (i + 1) * segf, hs);
                ctx.drawThickLine(ps1, ps3);
                ctx.drawThickLine(ps2, ps4);
            }
            interpPoint2(lead1, lead2, ps1, ps2, 1, hs);
            ctx.drawThickLine(ps1, ps2);
        }
        setVoltageColor(ctx, v3);
        ctx.drawThickLine(post3, corner2);
        ctx.drawThickLine(corner2, arrowPoint);
        ctx.drawThickLine(arrow1, arrowPoint);
        ctx.drawThickLine(arrow2, arrowPoint);
        curcount1 = updateDotCount(ctx, current1, curcount1);
        curcount2 = updateDotCount(ctx, current2, curcount2);
        curcount3 = updateDotCount(ctx, current3, curcount3);
        if (engine.getDraggedElement() != this) {
            drawDots(ctx, point1, midpoint, curcount1);
            drawDots(ctx, point2, midpoint, curcount2);
            drawDots(ctx, post3, corner2, curcount3);
            drawDots(ctx, corner2, midpoint, curcount3 + distance(post3, corner2));
        }
        drawPosts(ctx);
    }

    @Override
    public void calculateCurrent() {
        current1 = (volts[0] - volts[2]) / resistance1;
        current2 = (volts[1] - volts[2]) / resistance2;
        current3 = -current1 - current2;
    }

    @Override
    public void stamp() {
        resistance1 = maxResistance * position;
        resistance2 = maxResistance * (1 - position);
        engine.stampResistor(nodes[0], nodes[2], resistance1);
        engine.stampResistor(nodes[2], nodes[1], resistance2);
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = "potentiometer";
        arr[1] = "Vd = " + ctx.getVoltageDText(getVoltageDiff());
        arr[2] = "R1 = " + ctx.getUnitText(resistance1, CirSim.ohmString);
        arr[3] = "R2 = " + ctx.getUnitText(resistance2, CirSim.ohmString);
        arr[4] = "I1 = " + ctx.getCurrentDText(current1);
        arr[5] = "I2 = " + ctx.getCurrentDText(current2);
    }

    @Override
    public EditInfo getEditInfo(int n) {
        // ohmString doesn't work here on linux
        if (n == 0)
            return new EditInfo("Resistance (ohms)", maxResistance, 0, 0);
        if (n == 1) {
            EditInfo ei = new EditInfo("Slider Text", 0, -1, -1);
            ei.setText(sliderText);
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            maxResistance = ei.getValue();
        if (n == 1) {
            sliderText = ei.getTextf().getText();
            label.setText(sliderText);
        }
    }
}

