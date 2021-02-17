package org.circuitsymphony.util;

import org.circuitsymphony.element.CircuitElm;

public class CircuitNodeLink {

    private int num;
    private CircuitElm elm;

    public CircuitNodeLink() {
    }

    public CircuitNodeLink(int num, CircuitElm elm) {
        this.num = num;
        this.elm = elm;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public CircuitElm getElm() {
        return elm;
    }

    public void setElm(CircuitElm elm) {
        this.elm = elm;
    }
}
