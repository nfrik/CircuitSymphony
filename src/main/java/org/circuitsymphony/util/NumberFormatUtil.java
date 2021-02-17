package org.circuitsymphony.util;

import org.circuitsymphony.ui.CirSim;

import java.text.NumberFormat;

public class NumberFormatUtil {
    public static String getUnitText(NumberFormat showFormat, double value, String unit) {
        double va = Math.abs(value);
        if (va < 1e-14)
            return "0 " + unit;
        if (va < 1e-9)
            return showFormat.format(value * 1e12) + " p" + unit;
        if (va < 1e-6)
            return showFormat.format(value * 1e9) + " n" + unit;
        if (va < 1e-3)
            return showFormat.format(value * 1e6) + " " + CirSim.muString + unit;
        if (va < 1)
            return showFormat.format(value * 1e3) + " m" + unit;
        if (va < 1e3)
            return showFormat.format(value) + " " + unit;
        if (va < 1e6)
            return showFormat.format(value * 1e-3) + " k" + unit;
        if (va < 1e9)
            return showFormat.format(value * 1e-6) + " M" + unit;
        return showFormat.format(value * 1e-9) + " G" + unit;
    }
}
