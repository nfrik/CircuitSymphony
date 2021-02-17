package org.circuitsymphony.util;

// info about each row/column of the matrix for simplification purposes
public class RowInfo {

    public static final int ROW_NORMAL = 0;  // ordinary value
    public static final int ROW_CONST = 1;  // value is constant
    public static final int ROW_EQUAL = 2;  // value is equal to another value
    private int nodeEq, type, mapCol, mapRow;
    private double value;
    private boolean rsChanges; // row's right side changes
    private boolean lsChanges; // row's left side changes
    private boolean dropRow;   // row is not needed in matrix

    public RowInfo() {
        type = ROW_NORMAL;
    }

    public int getNodeEq() {
        return nodeEq;
    }

    public void setNodeEq(int nodeEq) {
        this.nodeEq = nodeEq;
    }

    public boolean isDropRow() {
        return dropRow;
    }

    public void setDropRow(boolean dropRow) {
        this.dropRow = dropRow;
    }

    public boolean isLsChanges() {
        return lsChanges;
    }

    public void setLsChanges(boolean lsChanges) {
        this.lsChanges = lsChanges;
    }

    public boolean isRsChanges() {
        return rsChanges;
    }

    public void setRsChanges(boolean rsChanges) {
        this.rsChanges = rsChanges;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getMapRow() {
        return mapRow;
    }

    public void setMapRow(int mapRow) {
        this.mapRow = mapRow;
    }

    public int getMapCol() {
        return mapCol;
    }

    public void setMapCol(int mapCol) {
        this.mapCol = mapCol;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
