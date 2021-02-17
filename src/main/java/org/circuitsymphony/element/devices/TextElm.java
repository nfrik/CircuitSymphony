package org.circuitsymphony.element.devices;

import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.engine.CircuitEngine;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.DrawContext;
import org.circuitsymphony.ui.EditInfo;

import java.awt.*;
import java.util.StringTokenizer;
import java.util.Vector;

public class TextElm extends CircuitElm {
    private final int FLAG_CENTER = 1;
    private final int FLAG_BAR = 2;
    private String text;
    private Vector lines;
    private int size;

    @SuppressWarnings("unchecked")
    public TextElm(CircuitEngine engine, int xx, int yy) {
        super(engine, xx, yy);
        text = "hello";
        lines = new Vector();
        lines.add(text);
        size = 24;
    }

    public TextElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f,
                   StringTokenizer st) {
        this(engine, xa, ya, xb, yb, f, 0, st);
    }

    public TextElm(CircuitEngine engine, int xa, int ya, int xb, int yb, int f, int f2,
                   StringTokenizer st) {
        super(engine, xa, ya, xb, yb, f, f2);
        size = new Integer(st.nextToken());
        text = st.nextToken();
        while (st.hasMoreTokens())
            text += ' ' + st.nextToken();
        split();
    }

    @SuppressWarnings("unchecked")
    private void split() {
        int i;
        lines = new Vector();
        StringBuilder sb = new StringBuilder(text);
        for (i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c == '\\') {
                sb.deleteCharAt(i);
                c = sb.charAt(i);
                if (c == 'n') {
                    lines.add(sb.substring(0, i));
                    sb.delete(0, i + 1);
                    i = -1;
                }
            }
        }
        lines.add(sb.toString());
    }

    @Override
    public String dump(boolean newFormat) {
        return super.dump(newFormat) + " " + size + " " + text;
    }

    @Override
    public int getDumpType() {
        return 'x';
    }

    @Override
    public void drag(CirSim sim, int xx, int yy) {
        x = xx;
        y = yy;
        x2 = xx + 16;
        y2 = yy;
    }

    @Override
    public void draw(DrawContext ctx) {
        ctx.setColor(needsHighlight(ctx) ? ctx.selectColor : ctx.lightGrayColor);
        Font f = new Font("SansSerif", 0, size);
        ctx.setFont(f);
        FontMetrics fm = ctx.getFontMetrics();
        int i;
        int maxw = -1;
        for (i = 0; i != lines.size(); i++) {
            int w = fm.stringWidth((String) (lines.elementAt(i)));
            if (w > maxw)
                maxw = w;
        }
        int cury = y;
        setBbox(x, y, x, y);
        for (i = 0; i != lines.size(); i++) {
            String s = (String) (lines.elementAt(i));
            if ((flags & FLAG_CENTER) != 0)
                x = (100 - fm.stringWidth(s)) / 2;
            ctx.drawString(s, x, cury);
            if ((flags & FLAG_BAR) != 0) {
                int by = cury - fm.getAscent();
                ctx.drawLine(x, by, x + fm.stringWidth(s) - 1, by);
            }
            adjustBbox(x, cury - fm.getAscent(),
                    x + fm.stringWidth(s), cury + fm.getDescent());
            cury += fm.getHeight();
        }
        x2 = boundingBox.x + boundingBox.width;
        y2 = boundingBox.y + boundingBox.height;
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0) {
            EditInfo ei = new EditInfo("Text", 0, -1, -1);
            ei.setText(text);
            return ei;
        }
        if (n == 1)
            return new EditInfo("Size", size, 5, 100);
        if (n == 2) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Center", (flags & FLAG_CENTER) != 0));
            return ei;
        }
        if (n == 3) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.setCheckbox(new Checkbox("Draw Bar On Top", (flags & FLAG_BAR) != 0));
            return ei;
        }
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0) {
            text = ei.getTextf().getText();
            split();
        }
        if (n == 1)
            size = (int) ei.getValue();
        if (n == 3) {
            if (ei.getCheckbox().getState())
                flags |= FLAG_BAR;
            else
                flags &= ~FLAG_BAR;
        }
        if (n == 2) {
            if (ei.getCheckbox().getState())
                flags |= FLAG_CENTER;
            else
                flags &= ~FLAG_CENTER;
        }
    }

    @Override
    public boolean isCenteredText() {
        return (flags & FLAG_CENTER) != 0;
    }

    @Override
    public void getInfo(DrawContext ctx, String arr[]) {
        arr[0] = text;
    }

    @Override
    public int getPostCount() {
        return 0;
    }
}

