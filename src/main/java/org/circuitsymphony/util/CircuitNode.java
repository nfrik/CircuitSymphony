package org.circuitsymphony.util;

import java.util.Vector;

public class CircuitNode {

    private final Vector<CircuitNodeLink> links;
    private int x, y;
    private boolean internal;

    public CircuitNode() {
        links = new Vector<>();
    }

    public Vector<CircuitNodeLink> getLinks() {
        return links;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }
}
