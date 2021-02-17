package org.circuitsymphony.ui;

import org.circuitsymphony.util.Camera;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;


public class CircuitCanvas extends Canvas {
    private static final double minZoom = 0.05;
    private static final double mouseDraggedThreshold = 4;

    private final CirSim sim;

    private Camera camera;
    private double startX = 0;
    private double startY = 0;
    private double lastX = 0;
    private double lastY = 0;
    private boolean mouseDragged = false;
    private boolean orthoInitialized = false;

    private Point tmpPoint = new Point();
    private Camera.Vector3 tmpVec = new Camera.Vector3();

    private PolygonProjector polygonProjector = new PolygonProjector();

    public CircuitCanvas(CirSim sim) {
        this.sim = sim;
        camera = new Camera();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
                startX = e.getX();
                startY = e.getY();
                mouseDragged = false;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) == false) return;
                camera.position.x -= (e.getX() - lastX) * camera.zoom;
                camera.position.y -= (e.getY() - lastY) * camera.zoom;
                lastX = e.getX();
                lastY = e.getY();
                if (Math.abs(e.getX() - startX) >= mouseDraggedThreshold || Math.abs(e.getY() - startY) >= mouseDraggedThreshold) {
                    mouseDragged = true;
                }
                camera.update();
                if (sim.isSimulationStopped()) repaint();
            }
        });

        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoomAroundPoint(e.getX(), e.getY(), 0.05 * e.getPreciseWheelRotation());
                if (sim.isSimulationStopped()) repaint();
            }
        });
    }

    /**
     * Zooms camera around given point.
     *
     * @param x screen x
     * @param y screen y
     */
    private void zoomAroundPoint(double x, double y, double zoomDelta) {
        double oldZoom = camera.zoom;
        double newZoom = Math.max(camera.zoom + zoomDelta, minZoom);
        if (oldZoom == newZoom) return;

        camera.unproject(tmpVec.set(x, y, 0));
        double cursorX = tmpVec.x;
        double cursorY = tmpVec.y;

        camera.position.x = cursorX + newZoom / camera.zoom * (camera.position.x - cursorX);
        camera.position.y = cursorY + newZoom / camera.zoom * (camera.position.y - cursorY);
        camera.zoom = newZoom;

        camera.update();
    }

    /**
     * Called by {@link CirSim} when circuit area has changed.
     */
    public void circuitAreaChanged() {
        if (sim.getCircuitAreaWidth() == 0 && sim.getCircuitAreaHeight() == 0) return;
        initOrthographicProjection();
        camera.updateViewport(sim.getCircuitAreaWidth(), sim.getCircuitAreaHeight());
    }

    private void initOrthographicProjection() {
        if (orthoInitialized) return;
        orthoInitialized = true;
        camera.setToOrtho(sim.getCircuitAreaWidth(), sim.getCircuitAreaHeight());
        camera.position.x = sim.getCircuitAreaWidth() / 2;
        camera.position.y = sim.getCircuitAreaHeight() / 2;
        camera.update();
    }

    public double getCameraX() {
        return camera.position.x;
    }

    public double getCameraY() {
        return camera.position.y;
    }

    public double getCameraZoom() {
        return camera.zoom;
    }

    /**
     * @return true if mouse was dragged on last button press
     */
    public boolean wasMouseDragged() {
        return mouseDragged;
    }

    /**
     * Converts screen coordinates to circuit world coordinates
     *
     * @return point holding converted coordinates. Warning: this is a shared object which should values should be used
     * immediately or copied into variable if they are needed later.
     */
    public Point unproject(Point screenPoint) {
        return unproject(screenPoint.x, screenPoint.y);
    }

    /**
     * Converts screen coordinates to circuit world coordinates
     *
     * @return point holding converted coordinates. Warning: this is a shared object which should values should be used
     * immediately or copied into variable if they are needed later.
     */
    public Point unproject(int screenX, int screenY) {
        camera.unproject(tmpVec.set(screenX, screenY, 0));
        tmpPoint.setLocation((int) tmpVec.x, (int) tmpVec.y);
        return tmpPoint;
    }

    /**
     * Converts screen coordinates to circuit world coordinates.
     * This method avoids converting result to integer thus giving more precision.
     */
    public void unprojectPrecise(int screenX, int screenY, Point2D.Double result) {
        camera.unproject(tmpVec.set(screenX, screenY, 0));
        result.setLocation(tmpVec.x, tmpVec.y);
    }

    /**
     * Converts circuit world coordinates to screen coordinates
     *
     * @return point holding converted coordinates. Warning: this is a shared object which should values should be used
     * immediately or copied into variable if they are needed later.
     */
    public Point project(Point worldPoint) {
        return project(worldPoint.x, worldPoint.y);
    }

    /**
     * Converts circuit world coordinates to screen coordinates
     *
     * @return point holding converted coordinates. Warning: this is a shared object which should values should be used
     * immediately or copied into variable if they are needed later.
     */
    public Point project(int worldX, int worldY) {
        camera.project(tmpVec.set(worldX, worldY, 0));
        tmpPoint.setLocation((int) tmpVec.x, (int) tmpVec.y);
        return tmpPoint;
    }

    /**
     * Converts circuit world coordinates of a polygon to screen coordinates
     *
     * @return polygon holding converted coordinates. Warning: This is a shared object which
     * should values should be used immediately or copied into variable if they are needed later.
     */
    public Polygon projectPolygon(Polygon polygon) {
        return polygonProjector.projectPolygon(polygon);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, 400);
    }

    @Override
    public void update(Graphics g) {
        try {
            sim.updateCircuit(g);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        try {
            sim.updateCircuit(g);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paintGird(Graphics g, Color gridColor) {
        g.setColor(gridColor);
        Point topLeft = new Point(unproject(0, 0));
        Point bottomRight = new Point(unproject(sim.getCircuitAreaWidth(), sim.getCircuitAreaHeight()));
        double worldWidth = bottomRight.x - topLeft.x;
        double worldHeight = bottomRight.y - topLeft.y;

        int actualGridSize = sim.getGridSize();
        if (camera.zoom < 0.5) {
            actualGridSize /= 2;
        } else if (camera.zoom > 3.5) {
            actualGridSize *= 4;
        }

        double verticalLinesToDraw = worldHeight / actualGridSize;
        int verticalLinesOffset = topLeft.y / actualGridSize;
        double circlesToDraw = worldWidth / actualGridSize;
        int circlesOffset = topLeft.x / actualGridSize;

        for (int i = 0; i < verticalLinesToDraw; i++) {
            for (int ii = 0; ii < circlesToDraw; ii++) {
                Point screenPos = project((ii + circlesOffset) * actualGridSize, (i + verticalLinesOffset) * actualGridSize);
                g.fillRect(screenPos.x, screenPos.y, 2, 2);
            }
        }
    }

    private class PolygonProjector {
        int pos;
        int xPoints[] = new int[8];
        int yPoints[] = new int[8];

        Polygon tmpRes = new Polygon(xPoints, yPoints, 8);

        public Polygon projectPolygon(Polygon polygon) {
            pos = 0;

            for (int i = 0; i < polygon.npoints; i++) {
                Point screenPos = project(polygon.xpoints[i], polygon.ypoints[i]);
                pushPoint(screenPos.x, screenPos.y);
            }

            tmpRes.xpoints = xPoints;
            tmpRes.ypoints = yPoints;
            tmpRes.npoints = pos;
            return tmpRes;
        }

        private void pushPoint(int x, int y) {
            if (pos == size() - 1) resize();
            xPoints[pos] = x;
            yPoints[pos] = y;
            pos++;
        }

        private void resize() {
            int newX[] = new int[(int) (size() * 1.75)];
            int newY[] = new int[(int) (size() * 1.75)];
            System.arraycopy(xPoints, 0, newX, 0, size());
            System.arraycopy(yPoints, 0, newY, 0, size());
            xPoints = newX;
            yPoints = newY;
        }

        private int size() {
            return xPoints.length;
        }
    }
}
