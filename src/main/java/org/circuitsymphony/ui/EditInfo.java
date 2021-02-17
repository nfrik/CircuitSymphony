package org.circuitsymphony.ui;

import java.awt.*;

public class EditInfo {

    private final String name;
    private final double maxval;
    private final boolean forceLargeM;
    private String text;
    private double value;
    private double minval;
    private TextField textf;
    private Scrollbar bar;
    private Choice choice;
    private Checkbox checkbox;
    private boolean newDialog;
    private boolean dimensionless;

    public EditInfo(String n, double val, double mn, double mx) {
        name = n;
        value = val;
        if (mn == 0 && mx == 0 && val > 0) {
            minval = 1e10;
            while (minval > val / 100)
                minval /= 10.;
            maxval = minval * 1000;
        } else {
            minval = mn;
            maxval = mx;
        }
        forceLargeM = name.indexOf("(ohms)") > 0 ||
                name.indexOf("(Hz)") > 0;
        dimensionless = false;
    }

    public String getName() {
        return name;
    }

    public double getMaxval() {
        return maxval;
    }

    public boolean isForceLargeM() {
        return forceLargeM;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getMinval() {
        return minval;
    }

    public void setMinval(double minval) {
        this.minval = minval;
    }

    public TextField getTextf() {
        return textf;
    }

    public void setTextf(TextField textf) {
        this.textf = textf;
    }

    public Scrollbar getBar() {
        return bar;
    }

    public void setBar(Scrollbar bar) {
        this.bar = bar;
    }

    public Choice getChoice() {
        return choice;
    }

    public void setChoice(Choice choice) {
        this.choice = choice;
    }

    public Checkbox getCheckbox() {
        return checkbox;
    }

    public void setCheckbox(Checkbox checkbox) {
        this.checkbox = checkbox;
    }

    public boolean isNewDialog() {
        return newDialog;
    }

    public void setNewDialog(boolean newDialog) {
        this.newDialog = newDialog;
    }

    public boolean isDimensionless() {
        return dimensionless;
    }

    public EditInfo setDimensionless() {
        dimensionless = true;
        return this;
    }
}
    
