package org.circuitsymphony.element.cdseries;

import org.circuitsymphony.element.ChipCDElm;
import org.circuitsymphony.engine.CircuitEngine;

import java.util.StringTokenizer;

public class CD4028 extends ChipCDElm {

    public CD4028(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public CD4028(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public CD4028(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2, StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    public boolean needsBits() {
        return true;
    }

    public String getChipName() {
        return "BCD-to-Decimal Decoder (4028)";
    }

    public void setupPins() {
        sizeX = 2;
        sizeY = bits + 6;
        pins = new Pin[getPostCount()];
        int i;

        String[] alpha = {"A", "B", "C", "D"};

        for (i = 0; i != bits; i++)
            pins[i] = new Pin(bits - 1 - i, SIDE_W, alpha[i]);

        for (i = 0; i != bits + 6; i++) {
            pins[i + bits] = new Pin(i, SIDE_E, "Q" + i);
            pins[i + bits].output = true;
        }

        allocNodes();
    }

    public void execute() {
        int[] bit = new int[4];

        for (int a = 0; a < 4; a++)
            if (pins[a].value) bit[a] = 1;

        int decimal;
        decimal = bit[0] + bit[1] * 2 + bit[2] * 4 + bit[3] * 8;
        //System.out.println(decimal);

        pins[4].value = false;
        pins[5].value = false;
        pins[6].value = false;
        pins[7].value = false;
        pins[8].value = false;
        pins[9].value = false;
        pins[10].value = false;
        pins[11].value = false;
        pins[12].value = false;
        pins[13].value = false;

        if (decimal == 0) pins[4].value = true;
        if (decimal == 1) pins[5].value = true;
        if (decimal == 2) pins[6].value = true;
        if (decimal == 3) pins[7].value = true;
        if (decimal == 4) pins[8].value = true;
        if (decimal == 5) pins[9].value = true;
        if (decimal == 6) pins[10].value = true;
        if (decimal == 7) pins[11].value = true;
        if (decimal == 8) pins[12].value = true;
        if (decimal == 9) pins[13].value = true;
    }

    public int getVoltageSourceCount() {
        return bits + 6;
    }

    public int getPostCount() {
        return bits * 2 + 6;
    }

    public int getDumpType() {
        return 185;
    }
}
