package org.circuitsymphony.element.devices;

import org.circuitsymphony.element.ChipElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.DrawContext;

import java.awt.*;
import java.util.StringTokenizer;

public class Matrix5x7Elm extends ChipElm {
    private Color darkred;

    public Matrix5x7Elm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
    }

    public Matrix5x7Elm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                        StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public Matrix5x7Elm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                        StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2, st);
    }

    @Override
    public String getChipName() {
        return "5x7 matrix driver/display";
    }

    public boolean needsBits() {
        return true;
    }

    @Override
    public void setupPins() {
        darkred = new Color(30, 0, 0);
        sizeX = 6;
        sizeY = 8;
        pins = new Pin[getPostCount()]; // 12 Pines
        pins[0] = new Pin(0, SIDE_W, "i1");
        pins[1] = new Pin(1, SIDE_W, "i2");
        pins[2] = new Pin(2, SIDE_W, "i3");
        pins[3] = new Pin(3, SIDE_W, "i4");
        pins[4] = new Pin(4, SIDE_W, "i5");
        pins[5] = new Pin(5, SIDE_W, "i6");
        pins[6] = new Pin(6, SIDE_W, "i7");

        pins[7] = new Pin(1, SIDE_S, "o1");
        pins[8] = new Pin(2, SIDE_S, "o2");
        pins[9] = new Pin(3, SIDE_S, "o3");
        pins[10] = new Pin(4, SIDE_S, "o4");
        pins[11] = new Pin(5, SIDE_S, "o5");

        allocNodes();
    }

    @Override
    public void draw(DrawContext ctx) {
        drawChip(ctx);

        int xl = x + (csize * 24);
        int yl = y;
        int xs = csize * 12;
        int ys = csize * 12;
        int p = csize * 5;


        for (int b = 0; b < 5; b++) {
            for (int a = 0; a < 7; a++) {
                ctx.setColor(darkred);
                ctx.fillOval(xl + (xs * (b + 1)), yl + (ys * (a + 1)), p, p);
            }
        }


        for (int b = 0; b < 7; b++) {
            if (pins[b].value) {
                for (int a = 0; a < 5; a++) {
                    ctx.setColor(pins[7 + a].value ? Color.red : darkred);
                    ctx.fillOval(xl + (xs * (a + 1)), yl + (ys * (b + 1)), p, p);
                }
            }
        }

	    /*
        if(pins[0].value == true) {
			g.setColor(pins[7].value ? Color.red : darkred);
			g.fillOval(xl+(xs*(1)), yl+(ys*(1)), p, p);
			
			g.setColor(pins[8].value ? Color.red : darkred);
			g.fillOval(xl+(xs*(2)), yl+(ys*(1)), p, p);
			
		}
	    */
        //System.out.println("+");
    }

    public void setColor(Graphics g, int p) {
        g.setColor(pins[p].value ? Color.red : darkred);
    }

    @Override
    public int getVoltageSourceCount() {
        return 0;
    }

    @Override
    public int getPostCount() {
        return 7 + 5;
    }

    @Override
    public int getDumpType() {
        return 180;
    }
}
