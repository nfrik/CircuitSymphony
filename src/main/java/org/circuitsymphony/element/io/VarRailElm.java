package org.circuitsymphony.element.io;

import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.EditInfo;
import org.circuitsymphony.util.Accessor;

import java.awt.*;
import java.util.StringTokenizer;

public class VarRailElm extends RailElm {
    private Scrollbar slider;
    private Accessor<Integer> sliderValueAccessor;
    private Label label;
    private String sliderText;

    public VarRailElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy, VoltageElm.WF_VAR);
        sliderText = "Voltage";
        frequency = maxVoltage;
        createSlider();
    }

    public VarRailElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                      StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public VarRailElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                      StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
        sliderText = st.nextToken();
        while (st.hasMoreTokens())
            sliderText += ' ' + st.nextToken();
        createSlider();
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + sliderText;
    }

    @Override
    public int getDumpType() {
        return 172;
    }

    private void createSlider() {
        waveform = WF_VAR;
        int value = (int) ((frequency - bias) * 100 / (maxVoltage - bias));
        if (engine.isUISupported()) {
            engine.createUI(label = new Label(sliderText, Label.CENTER));
            engine.createUI(slider = new Scrollbar(Scrollbar.HORIZONTAL, value, 1, 0, 101));
            sliderValueAccessor = () -> slider.getValue();
        } else {
            sliderValueAccessor = () -> value;
        }
    }

    @Override
    public double getVoltage() {
        frequency = sliderValueAccessor.get() * (maxVoltage - bias) / 100. + bias;
        return frequency;
    }

    @Override
    public void delete() {
        if (engine.isUISupported()) {
            engine.removeUI(label);
            engine.removeUI(slider);
        }
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Min Voltage", bias, -20, 20);
        if (n == 1)
            return new EditInfo("Max Voltage", maxVoltage, -20, 20);
        if (n == 2) {
            EditInfo ei = new EditInfo("Slider Text", 0, -1, -1);
            ei.setText(sliderText);
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            bias = ei.getValue();
        if (n == 1)
            maxVoltage = ei.getValue();
        if (n == 2) {
            sliderText = ei.getTextf().getText();
            label.setText(sliderText);
        }
    }
}
