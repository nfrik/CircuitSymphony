package org.circuitsymphony.element.chips;

import org.circuitsymphony.element.ChipElm;
import org.circuitsymphony.engine.CircuitEngine;

import java.util.StringTokenizer;

public class TimerElm extends ChipElm {
    private final int FLAG_RESET = 2;
    private final int N_DIS = 0;
    private final int N_TRIG = 1;
    private final int N_THRES = 2;
    private final int N_VIN = 3;
    private final int N_CTL = 4;
    private final int N_OUT = 5;
    private final int N_RST = 6;
    private boolean setOut;
    private boolean out;

    public TimerElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public TimerElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                    StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public TimerElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                    StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public int getDefaultFlags() {
        return FLAG_RESET;
    }

    @Override
    public String getChipName() {
        return "555 Timer";
    }

    @Override
    public void setupPins() {
        sizeX = 3;
        sizeY = 5;
        pins = new Pin[7];
        pins[N_DIS] = new Pin(1, SIDE_W, "dis");
        pins[N_TRIG] = new Pin(3, SIDE_W, "tr");
        pins[N_TRIG].lineOver = true;
        pins[N_THRES] = new Pin(4, SIDE_W, "th");
        pins[N_VIN] = new Pin(1, SIDE_N, "Vin");
        pins[N_CTL] = new Pin(1, SIDE_S, "ctl");
        pins[N_OUT] = new Pin(2, SIDE_E, "out");
        pins[N_OUT].output = pins[N_OUT].state = true;
        pins[N_RST] = new Pin(1, SIDE_E, "rst");
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    private boolean hasReset() {
        return (flags & FLAG_RESET) != 0;
    }

    @Override
    public void stamp() {
        // stamp voltage divider to put ctl pin at 2/3 V
        engine.stampResistor(nodes[N_VIN], nodes[N_CTL], 5000);
        engine.stampResistor(nodes[N_CTL], 0, 10000);
        // output pin
        engine.stampVoltageSource(0, nodes[N_OUT], pins[N_OUT].voltSource);
        // discharge pin
        engine.stampNonLinear(nodes[N_DIS]);
    }

    @Override
    public void calculateCurrent() {
        // need current for V, discharge, control; output current is
        // calculated for us, and other pins have no current
        pins[N_VIN].current = (volts[N_CTL] - volts[N_VIN]) / 5000;
        pins[N_CTL].current = -volts[N_CTL] / 10000 - pins[N_VIN].current;
        pins[N_DIS].current = (!out && !setOut) ? -volts[N_DIS] / 10 : 0;
    }

    @Override
    public void startIteration() {
        out = volts[N_OUT] > volts[N_VIN] / 2;
        setOut = false;
        // check comparators
        if (volts[N_CTL] / 2 > volts[N_TRIG])
            setOut = out = true;
        if (volts[N_THRES] > volts[N_CTL] || (hasReset() && volts[N_RST] < .7))
            out = false;
    }

    @Override
    public void doStep() {
        // if output is low, discharge pin 0.  we use a small
        // resistor because it's easier, and sometimes people tie
        // the discharge pin to the trigger and threshold pins.
        // We check setOut to properly emulate the case where
        // trigger is low and threshold is high.
        if (!out && !setOut)
            engine.stampResistor(nodes[N_DIS], 0, 10);
        // output
        engine.updateVoltageSource(pins[N_OUT].voltSource, out ? volts[N_VIN] : 0);
    }

    @Override
    public int getPostCount() {
        return hasReset() ? 7 : 6;
    }

    @Override
    public int getVoltageSourceCount() {
        return 1;
    }

    @Override
    public int getDumpType() {
        return 165;
    }
}
    
