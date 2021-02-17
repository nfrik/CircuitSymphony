package org.circuitsymphony.engine.graph;

/**
 * Represents single gird point.
 */
public class GridPoint {
    final int x;
    final int y;

    public GridPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GridPoint gridPoint = (GridPoint) o;

        if (x != gridPoint.x) return false;
        return y == gridPoint.y;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}
