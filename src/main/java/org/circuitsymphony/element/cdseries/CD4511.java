package org.circuitsymphony.element.cdseries;

import org.circuitsymphony.element.ChipCDElm;
import org.circuitsymphony.engine.CircuitEngine;

import java.util.StringTokenizer;

public class CD4511 extends ChipCDElm {

    private final boolean[] latch = new boolean[7];
    private boolean le;

    public CD4511(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public CD4511(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public CD4511(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2, StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public String getChipName() {
        return "BCD to 7-Segm Decoder (4511)";
    }

    public void setupPins() {
        sizeX = 2;
        sizeY = 7;
        pins = new Pin[getPostCount()];
        int i;

        for (i = 0; i != 4; i++)
            pins[i] = new Pin(3 - i, SIDE_W, "I" + i);

        String[] alpha = {"a", "b", "c", "d", "e", "f", "g"};

        for (i = 0; i != 7; i++) {
            pins[i + 4] = new Pin(i, SIDE_E, alpha[i]);
            pins[i + 4].output = true;
        }

        pins[11] = new Pin(4, SIDE_W, "LE");
        pins[12] = new Pin(5, SIDE_W, "BI");
        pins[13] = new Pin(6, SIDE_W, "LT");

        allocNodes();
    }

    public void execute() {

        int a;

        //LT Lamp Test
        if (!pins[13].value) {
            pins[4].value = true;
            pins[5].value = true;
            pins[6].value = true;
            pins[7].value = true;
            pins[8].value = true;
            pins[9].value = true;
            pins[10].value = true;
            return;
        }


        // BI Blanking
        if (!pins[12].value) {
            pins[4].value = false;
            pins[5].value = false;
            pins[6].value = false;
            pins[7].value = false;
            pins[8].value = false;
            pins[9].value = false;
            pins[10].value = false;
            return;
        }


        int[] bit = new int[4];

        for (a = 0; a < 4; a++)
            if (pins[a].value) bit[a] = 1;

        int decimal;
        decimal = bit[0] + bit[1] * 2 + bit[2] * 4 + bit[3] * 8;
        //System.out.println(decimal);

        if (decimal == 0) {
            pins[4].value = true;
            pins[5].value = true;
            pins[6].value = true;
            pins[7].value = true;
            pins[8].value = true;
            pins[9].value = true;
            pins[10].value = false;
        }

        if (decimal == 1) {
            pins[4].value = false;
            pins[5].value = true;
            pins[6].value = true;
            pins[7].value = false;
            pins[8].value = false;
            pins[9].value = false;
            pins[10].value = false;
        }

        if (decimal == 2) {
            pins[4].value = true;
            pins[5].value = true;
            pins[6].value = false;
            pins[7].value = true;
            pins[8].value = true;
            pins[9].value = false;
            pins[10].value = true;
        }

        if (decimal == 3) {
            pins[4].value = true;
            pins[5].value = true;
            pins[6].value = true;
            pins[7].value = true;
            pins[8].value = false;
            pins[9].value = false;
            pins[10].value = true;
        }

        if (decimal == 4) {
            pins[4].value = false;
            pins[5].value = true;
            pins[6].value = true;
            pins[7].value = false;
            pins[8].value = false;
            pins[9].value = true;
            pins[10].value = true;
        }

        if (decimal == 5) {
            pins[4].value = true;
            pins[5].value = false;
            pins[6].value = true;
            pins[7].value = true;
            pins[8].value = false;
            pins[9].value = true;
            pins[10].value = true;
        }

        if (decimal == 6) {
            pins[4].value = false;
            pins[5].value = false;
            pins[6].value = true;
            pins[7].value = true;
            pins[8].value = true;
            pins[9].value = true;
            pins[10].value = true;
        }

        if (decimal == 7) {
            pins[4].value = true;
            pins[5].value = true;
            pins[6].value = true;
            pins[7].value = false;
            pins[8].value = false;
            pins[9].value = false;
            pins[10].value = false;
        }

        if (decimal == 8) {
            pins[4].value = true;
            pins[5].value = true;
            pins[6].value = true;
            pins[7].value = true;
            pins[8].value = true;
            pins[9].value = true;
            pins[10].value = true;
        }

        if (decimal == 9) {
            pins[4].value = true;
            pins[5].value = true;
            pins[6].value = true;
            pins[7].value = false;
            pins[8].value = false;
            pins[9].value = true;
            pins[10].value = true;
        }

        if (decimal > 9) {
            pins[4].value = false;
            pins[5].value = false;
            pins[6].value = false;
            pins[7].value = false;
            pins[8].value = false;
            pins[9].value = false;
            pins[10].value = false;
        }


        // LE latch enable
        if (!pins[11].value) {
            le = false;
        }

        if (le) {
            for (a = 0; a < 7; a++) {
                pins[a + 4].value = latch[a];
            }
            return;
        }

        if (pins[11].value) {
            for (a = 0; a < 7; a++) {
                latch[a] = pins[a + 4].value;
            }
            le = true;
        }
    }

    public int getVoltageSourceCount() {
        return 7;
    }

    public int getPostCount() {
        return 7 + 7;
    }

    public int getDumpType() {
        return 184;
    }
}
