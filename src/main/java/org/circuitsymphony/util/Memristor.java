package org.circuitsymphony.util;

/**
 * Created by nfrik on 10/8/16.
 */
public class Memristor {

    private final double r_on;
    private final double r_off;
    private final double totalWidth;
    private final double mobility;
    private double dopeWidth;
    private double resistance;
    private double current;

    public Memristor() {
        r_on = 100;
        r_off = 160 * r_on;
        dopeWidth = 0;
        totalWidth = 10e-9; // meters
        mobility = 1e-10;   // m^2/sV
        resistance = 100;
    }

    public void startIteration(double timeStep) {
        double wd = dopeWidth / totalWidth;
        dopeWidth += timeStep * mobility * r_on * current / totalWidth;
        if (dopeWidth < 0)
            dopeWidth = 0;
        if (dopeWidth > totalWidth)
            dopeWidth = totalWidth;
        resistance = r_on * wd + r_off * (1 - wd);
    }
}
