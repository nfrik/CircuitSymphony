package org.circuitsymphony.element.chips;

import org.circuitsymphony.element.ChipElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;

import java.util.StringTokenizer;

public class VCOElm extends ChipElm {
    private final double cResistance = 1e6;
    private int cDir;

    public VCOElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public VCOElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                  StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public VCOElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                  StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public String getChipName() {
        return "VCO";
    }

    @Override
    public void setupPins() {
        sizeX = 2;
        sizeY = 4;
        pins = new Pin[6];
        pins[0] = new Pin(0, SIDE_W, "Vi");
        pins[1] = new Pin(3, SIDE_W, "Vo");
        pins[1].output = true;
        pins[2] = new Pin(0, SIDE_E, "C");
        pins[3] = new Pin(1, SIDE_E, "C");
        pins[4] = new Pin(2, SIDE_E, "R1");
        pins[4].output = true;
        pins[5] = new Pin(3, SIDE_E, "R2");
        pins[5].output = true;
    }

    @Override
    public boolean nonLinear() {
        return true;
    }

    @Override
    public void stamp() {
        // output pin
        engine.stampVoltageSource(0, nodes[1], pins[1].voltSource);
        // attach Vi to R1 pin so its current is proportional to Vi
        engine.stampVoltageSource(nodes[0], nodes[4], pins[4].voltSource, 0);
        // attach 5V to R2 pin so we get a current going
        engine.stampVoltageSource(0, nodes[5], pins[5].voltSource, 5);
        // put resistor across cap pins to give current somewhere to go
        // in case cap is not connected
        engine.stampResistor(nodes[2], nodes[3], cResistance);
        engine.stampNonLinear(nodes[2]);
        engine.stampNonLinear(nodes[3]);
    }

    @Override
    public void doStep() {
        double vc = volts[3] - volts[2];
        double vo = volts[1];
        int dir = (vo < 2.5) ? 1 : -1;
        // switch direction of current through cap as we oscillate
        if (vo < 2.5 && vc > 4.5) {
            vo = 5;
            dir = -1;
        }
        if (vo > 2.5 && vc < .5) {
            vo = 0;
            dir = 1;
        }

        // generate output voltage
        engine.updateVoltageSource(pins[1].voltSource, vo);
        // now we set the current through the cap to be equal to the
        // current through R1 and R2, so we can measure the voltage
        // across the cap
        int cur1 = engine.nodeList.size() + pins[4].voltSource;
        int cur2 = engine.nodeList.size() + pins[5].voltSource;
        engine.stampMatrix(nodes[2], cur1, dir);
        engine.stampMatrix(nodes[2], cur2, dir);
        engine.stampMatrix(nodes[3], cur1, -dir);
        engine.stampMatrix(nodes[3], cur2, -dir);
        cDir = dir;
    }

    // can't do this in calculateCurrent() because it's called before
    // we get pins[4].current and pins[5].current, which we need
    private void computeCurrent() {
        if (cResistance == 0)
            return;
        double c = cDir * (pins[4].current + pins[5].current) +
                (volts[3] - volts[2]) / cResistance;
        pins[2].current = -c;
        pins[3].current = c;
        pins[0].current = -pins[4].current;
    }

    @Override
    public void draw(DrawContext ctx) {
        computeCurrent();
        drawChip(ctx);
    }

    @Override
    public int getPostCount() {
        return 6;
    }

    @Override
    public int getVoltageSourceCount() {
        return 3;
    }

    @Override
    public int getDumpType() {
        return 158;
    }
}