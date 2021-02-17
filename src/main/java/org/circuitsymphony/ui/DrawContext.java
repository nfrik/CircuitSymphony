package org.circuitsymphony.ui;

import org.circuitsymphony.util.NumberFormatUtil;

import java.awt.*;
import java.text.NumberFormat;


public class DrawContext {
    public static final int colorScaleCount = 32;
    public double currentMult, powerMult;
    public Color whiteColor, selectColor, lightGrayColor;
    public Font unitsFont;
    public Color[] colorScale;
    private CirSim sim;
    private CircuitCanvas canvas;
    private Graphics graphics;
    private NumberFormat shortFormat;
    private NumberFormat showFormat;
    private Stroke thickStroke = new BasicStroke(5,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);

    public DrawContext(CirSim sim) {
        this.sim = sim;
        this.canvas = sim.getCircuitCanvas();

        unitsFont = new Font("SansSerif", 0, 10);

        colorScale = new Color[colorScaleCount];
        for (int i = 0; i != colorScaleCount; i++) {
            double v = i * 2. / colorScaleCount - 1;
            if (v < 0) {
                int n1 = (int) (128 * -v) + 127;
                int n2 = (int) (127 * (1 + v));
                colorScale[i] = new Color(n1, n2, n2);
            } else {
                int n1 = (int) (128 * v) + 127;
                int n2 = (int) (127 * (1 - v));
                colorScale[i] = new Color(n2, n1, n2);
            }
        }

        shortFormat = NumberFormat.getInstance();
        shortFormat.setMaximumFractionDigits(1);
        showFormat = NumberFormat.getInstance();
        showFormat.setMaximumFractionDigits(2);
    }

    public void begin(Graphics graphics) {
        this.graphics = graphics;
    }

    public void end() {
        this.graphics = null;
    }

    public void setColor(Color color) {
        graphics.setColor(color);
    }

    public void drawRect(int x, int y, int width, int height) {
        Point screenPos = canvas.project(x, y);
        x = screenPos.x;
        y = screenPos.y;
        graphics.drawRect(x, y, (int) (width / getCameraZoom()), (int) (height / getCameraZoom()));
    }

    public void fillRect(int x, int y, int width, int height) {
        Point screenPos = canvas.project(x, y);
        x = screenPos.x;
        y = screenPos.y;
        graphics.fillRect(x, y, (int) (width / getCameraZoom()), (int) (height / getCameraZoom()));
    }

    public void fillOval(int x, int y, int width, int height) {
        Point screenPos = canvas.project(x, y);
        x = screenPos.x;
        y = screenPos.y;
        graphics.fillOval(x, y, (int) (width / getCameraZoom()), (int) (height / getCameraZoom()));
    }

    public void drawLine(int x, int y, int x2, int y2) {
        Point screenPos = project(x, y);
        x = screenPos.x;
        y = screenPos.y;
        screenPos = project(x2, y2);
        x2 = screenPos.x;
        y2 = screenPos.y;
        graphics.drawLine(x, y, x2, y2);
    }

    public void drawThickLine(int x, int y, int x2, int y2) {
        Point screenPos = project(x, y);
        x = screenPos.x;
        y = screenPos.y;
        screenPos = project(x2, y2);
        x2 = screenPos.x;
        y2 = screenPos.y;

        graphics.drawLine(x, y, x2, y2);
        graphics.drawLine(x + 1, y, x2 + 1, y2);
        graphics.drawLine(x, y + 1, x2, y2 + 1);
        graphics.drawLine(x + 1, y + 1, x2 + 1, y2 + 1);
    }

    public void drawDoubleThickLine(int x, int y, int x2, int y2) {
        Point screenPos = project(x, y);
        x = screenPos.x;
        y = screenPos.y;
        screenPos = project(x2, y2);
        x2 = screenPos.x;
        y2 = screenPos.y;

        Graphics2D g2 = (Graphics2D) graphics;
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(thickStroke);
        g2.drawLine(x, y, x2, y2);
        g2.setStroke(oldStroke);
    }

    public void drawThickLine(Point pa, Point pb) {
        drawThickLine(pa.x, pa.y, pb.x, pb.y);
    }

    public void drawThickPolygon(int xs[], int ys[], int c) {
        int i;
        for (i = 0; i != c - 1; i++) {
            drawThickLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }
        drawThickLine(xs[i], ys[i], xs[0], ys[0]);
    }

    public void drawThickPolygon(Polygon p) {
        drawThickPolygon(p.xpoints, p.ypoints, p.npoints);
    }

    public void drawThickCircle(int cx, int cy, int ri) {
        int a;
        double m = CirSim.PI / 180;
        double r = ri * .98;
        for (a = 0; a != 360; a += 20) {
            double ax = Math.cos(a * m) * r + cx;
            double ay = Math.sin(a * m) * r + cy;
            double bx = Math.cos((a + 20) * m) * r + cx;
            double by = Math.sin((a + 20) * m) * r + cy;
            drawThickLine((int) ax, (int) ay, (int) bx, (int) by);
        }
    }

    public void drawPolyline(int xPoints[], int yPoints[], int nPoints) {
        if (nPoints < 2) return;
        int x = xPoints[0];
        int y = yPoints[0];
        for (int i = 1; i < nPoints; i++) {
            drawLine(x, y, xPoints[i], yPoints[i]);
            x = xPoints[i];
            y = yPoints[i];
        }
    }

    public void fillPolygon(Polygon polygon) {
        graphics.fillPolygon(projectPolygon(polygon));
    }

    public void drawString(String text, int x, int y) {
        Point screenPos = project(x, y);
        graphics.drawString(text, screenPos.x, screenPos.y);
    }

    private double getCameraZoom() {
        return canvas.getCameraZoom();
    }

    private Point project(int worldX, int worldY) {
        return canvas.project(worldX, worldY);
    }

    private Polygon projectPolygon(Polygon polygon) {
        return canvas.projectPolygon(polygon);
    }

    public void setFont(Font font) {
        graphics.setFont(font);
    }

    public FontMetrics getFontMetrics() {
        return graphics.getFontMetrics();
    }

    public CirSim getSim() {
        return sim;
    }

    public String getShortUnitText(double v, String u) {
        double va = Math.abs(v);
        if (va < 1e-13)
            return null;
        if (va < 1e-9)
            return shortFormat.format(v * 1e12) + "p" + u;
        if (va < 1e-6)
            return shortFormat.format(v * 1e9) + "n" + u;
        if (va < 1e-3)
            return shortFormat.format(v * 1e6) + CirSim.muString + u;
        if (va < 1)
            return shortFormat.format(v * 1e3) + "m" + u;
        if (va < 1e3)
            return shortFormat.format(v) + u;
        if (va < 1e6)
            return shortFormat.format(v * 1e-3) + "k" + u;
        if (va < 1e9)
            return shortFormat.format(v * 1e-6) + "M" + u;
        return shortFormat.format(v * 1e-9) + "G" + u;
    }

    public String getVoltageDText(double v) {
        return getUnitText(Math.abs(v), "V");
    }

    public String getVoltageText(double v) {
        return getUnitText(v, "V");
    }

    public String getUnitText(double v, String u) {
        return NumberFormatUtil.getUnitText(showFormat, v, u);
    }

    public String getCurrentText(double i) {
        return getUnitText(i, "A");
    }

    public String getCurrentDText(double i) {
        return getUnitText(Math.abs(i), "A");
    }

    public NumberFormat getShowFormat() {
        return showFormat;
    }
}
