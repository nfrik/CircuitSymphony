package org.circuitsymphony.evolution;

import org.circuitsymphony.engine.CircuitLoadResult;
import org.circuitsymphony.manager.CircuitManager;

import java.util.concurrent.FutureTask;

public interface CircuitLoadingMethod {
    FutureTask<CircuitLoadResult> load(CircuitManager manager);
}
