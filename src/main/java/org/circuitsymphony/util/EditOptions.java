package org.circuitsymphony.util;

import org.circuitsymphony.Editable;
import org.circuitsymphony.ui.CirSim;
import org.circuitsymphony.ui.EditInfo;

public class EditOptions implements Editable {
    private final CirSim sim;

    public EditOptions(CirSim s) {
        sim = s;
    }

    @Override
    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Time step size (s)", sim.getCircuitEngine().timeStep, 0, 0);
        if (n == 1)
            return new EditInfo("Range for voltage color (V)", sim.getCircuitEngine().voltageRange, 0, 0);

        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        if (n == 0 && ei.getValue() > 0)
            sim.getCircuitEngine().timeStep = ei.getValue();
        if (n == 1 && ei.getValue() > 0)
            sim.getCircuitEngine().voltageRange = ei.getValue();
    }
}
