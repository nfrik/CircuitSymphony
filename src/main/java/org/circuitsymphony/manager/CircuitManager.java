package org.circuitsymphony.manager;

import com.google.gson.Gson;
import org.circuitsymphony.Constants;
import org.circuitsymphony.element.CircuitElm;
import org.circuitsymphony.element.WireElm;
import org.circuitsymphony.element.active.ResistorElm;
import org.circuitsymphony.element.io.VoltageElm;
import org.circuitsymphony.element.passive.SwitchElm;
import org.circuitsymphony.engine.*;
import org.circuitsymphony.util.JarUtils;
import org.circuitsymphony.util.ResettableCountDownLatch;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Asynchronous interface for controlling and measuring running circuit simulation.
 * <p>
 * Most methods in this class allowing to control simulation flow returns an object of type {@link FutureTask} representing
 * a command sent to simulation thread. All those method will return immediately after sending command, the time when command
 * is actually processed by simulation engine is unspecified. You may call {@link FutureTask#get()} to block until command
 * is executed by simulation thread.
 */
public class CircuitManager {
    private final LocalDateTime dateCreated = LocalDateTime.now();
    private final ArrayList<Runnable> runnables = new ArrayList<>();
    private final ArrayList<Runnable> runnablesToRun = new ArrayList<>();
    private final ResettableCountDownLatch idleLatch = new ResettableCountDownLatch(1);
    private final HashMap<Integer, CircuitElm> trackedElements = new HashMap<>();
    private final HashMap<Integer, CircuitElm> measurableElements = new HashMap<>();
    private HashMap<String, ArrayList<Double>> arbitraryWaveData = new HashMap<>();
    private final BlockingQueue<Measurement> measurements = new LinkedBlockingQueue<>();
    private final AtomicReference<Measurement> lastMeasurement = new AtomicReference<>(null);
    private AtomicBoolean running = new AtomicBoolean(false);
    private AtomicBoolean alive = new AtomicBoolean(true);
    private AtomicBoolean needAnalyze = new AtomicBoolean(false);
    private volatile CircuitEngine engine;
    private CircuitLoader loader;
    private PropertiesController propsController;
    private double lastPeek = -1;
    private double lastPoke = -1;
    private double peekInterval = -1;
    private double pokeInterval = -1;
    private double runTimerEndTime = -1;
    private long peekAccumulator = 1;
    private long pokeAccumulator = 1;

    private AbortableCountDownLatch runTimerLatch;
    private PropertyManager propertyManager;

    public CircuitManager() {
        Thread thread = new Thread(() -> {
            try {
                runSimulation();
            } catch (Exception e) {
                e.printStackTrace();
                runTimerLatch.abort();
                stop();
                kill();
//                throw new Exception("Possible singular matrix",e);
                throw new IllegalStateException(Constants.SINGULAR_MSG,e);
            }
        }, "CircuitManager");
        thread.start();
    }

    private void runSimulation() throws Exception {
        engine = new CircuitEngine(new CircuitEngineListener() {
            @Override
            public void stop(String cause, CircuitElm ce) throws Exception {
                throw new IllegalStateException("Simulation stopped! Reason: " + cause);
//                throw new Exception("Simulation stopped! Reason: " + cause);
            }

            @Override
            public void setAnalyzeFlag() {
                needAnalyze.set(true);
            }
        });

        loader = new CircuitLoader(engine, new CircuitLoaderListener() {
            @Override
            public void configureOptions(CircuitOptions options) {
                engine.timeStep = options.timeStep;
                engine.voltageRange = options.volateRange;
                engine.updateGrid(options.smallGridCheck);
                engine.updateIterationCount(options.speedBarValue);
            }

            @Override
            public void afterLoading(EnumSet<CircuitLoader.RetentionPolicy> retain) {
                engine.setAnalyzeFlag();
            }
        });

        propertyManager = new PropertyManager(needAnalyze);

        engine.setFixedIterations(1);
        propsController = new PropertiesController();

        while (alive.get()) {
            if (running.get() == false) {
                idleLatch.await();
            }
            if (alive.get() == false){
                break;
            }
            if (running.get()) {
                if (needAnalyze.get()) {
                    engine.analyzeCircuit();
                    needAnalyze.set(false);
                }
                engine.runCircuit();

                if (peekInterval != -1) {
//                    if (engine.t >= lastPeek + peekInterval) {
                    if (engine.t >= peekInterval*peekAccumulator) {
                        lastPeek = engine.t;
                        performMeasurement();
                        peekAccumulator+=1;
                    }
                }

                if (pokeInterval != -1) {
//                    if (engine.t >= lastPoke + pokeInterval) {
                    if (engine.t >= pokeInterval*pokeAccumulator) {
                        lastPoke = engine.t;
                        assignValuesFromArbitraryWave();
                        pokeAccumulator+=1;
                    }
                }

                if (runTimerEndTime != -1) {
                    if (engine.t > runTimerEndTime) {
                        running.set(false);
                        runTimerLatch.countDown();
                        runTimerEndTime = -1;
                    }
                }
            }

            executeRunnables();
        }
        engine.kill_nicslu();
    }

    private void executeRunnables() {
        synchronized (runnables) {
            runnablesToRun.addAll(runnables);
            runnables.clear();
            idleLatch.reset();
        }
        if (runnablesToRun.size() != 0) {
            runnablesToRun.forEach(Runnable::run);
            runnablesToRun.clear();
        }
    }

    /**
     * Retrieves all stored simulation measurements and returns it in {@link ArrayList}. Note that calling this method
     * will remove all measurement data from internal storage.
     *
     * @return list containing measurement data
     */
    public ArrayList<Measurement> retrieveMeasurements() {
        return retrieveMeasurements(new ArrayList<>());
    }

    /**
     * Retrieves log data from a circular buffer for particular simulation
     *
     * @return list containing log data
     */
    public ArrayList<String> getStatistics() {
        ArrayList<String> logs = new ArrayList<>();

        for(Iterator i = engine.log.iterator(); i.hasNext();){
            logs.add((String)i.next());
        }

        return logs;
    }

    /**
     * Retrieves all stored simulation measurements and returns it in {@link ArrayList}. Note that calling this method
     * will remove all measurement data from internal storage.
     *
     * @return list containing measurement data
     */
    public String retrieveMeasurementsGzip() {
        Gson gson = new Gson();

        String json = gson.toJson(retrieveMeasurements(new ArrayList<>()));



        return json;
    }

    /**
     * Retrieves arbitrary wave data which is used to set DC Voltage rate every peek interval
     *
     * @return Map containing arbitrary wave data
     */
    public HashMap<String,ArrayList<Double>> retrieveArbitraryWaveData(){
        return arbitraryWaveData;
    }

    /**
     * Retrieves arbitrary wave data which is used to set DC Voltage rate every peek interval
     *
     * @return Map containing arbitrary wave data
     */
    public void setArbitraryWaveData(HashMap<String,ArrayList<Double>> data){
        arbitraryWaveData = data;
    }

    /**
     * Retrieves all stored simulation measurements and stores it into provided array list. Note that calling this method
     * will remove all measurement data from internal storage.
     *
     * @return list containing measurement data that was passed as argument to this method
     */
    public ArrayList<Measurement> retrieveMeasurements(ArrayList<Measurement> fillList) {
        measurements.drainTo(fillList);
        return fillList;
    }

    /**
     * @return string representation of this {@link CircuitEngine} elements. Dumped elements always use new format.
     */
    public String dumpElements() {
        return executeForResult(new FutureTask<>(() -> engine.dumpElements(new StringBuilder(), true).toString()));
    }

    public List<Object> getElements() {
        return executeForResult(new FutureTask<>(() -> {
            List<Object> elements = new ArrayList<>();
            for (CircuitElm elm : engine.getElmList()) {
                elements.add(new ElementProperties(elm.flags2, elm.getClass().getSimpleName(),
                        elm.getCurrent(), elm.getVoltageDiff()));
            }
            return elements;
        }));
    }

    public List<String> getElementPropertyList(int id) {
        return executeForResult(new FutureTask<>(() -> propertyManager.getPropertyList(getTrackedElement(id))));
    }

    public <V> V getElementProperty(int id, String propertyKey) {
        return executeForResult(new FutureTask<>(() -> propertyManager.getProperty(getTrackedElement(id), propertyKey)));
    }

    public <V> void setElementProperty(int id, String propertyKey, V newValue) {
        execute(() -> propertyManager.setProperty(getTrackedElement(id), propertyKey, newValue));
    }

    /**
     * @return time of simulation
     */
    public double getTime() {
        return executeForResult(new FutureTask<>(() -> engine.t));
    }

    /**
     * @return voltage diff on specified tracked element id
     */
    public double getVoltageDiff(int id) {
        return executeForResult(new FutureTask<>(() -> getTrackedElement(id).getVoltageDiff()));
    }

    /**
     * @return current on specified tracked element id
     */
    public double getCurrent(int id) {
        return executeForResult(new FutureTask<>(() -> getTrackedElement(id).getCurrent()));
    }

    /**
     * @return time of last measurement. Or -1 if there are no measurements.
     */
    public double peekTime() {
        Measurement measurement = lastMeasurement.get();
        if (measurement == null) return -1;
        return measurement.getTime();
    }

    /**
     * @return time of last poke. Or -1 if there are no pokes.
     */
    public double pokeTime() {
        Measurement measurement = lastMeasurement.get();
        if (measurement == null) return -1;
        return measurement.getTime();
    }
    /**
     * @return voltage diff of specified element from last measurement. If there is no measurement data, this method will block
     * until current simulation update is finished.
     */
    public double peekVoltageDiff(int id) {
        return getLastElmRecord(id).getVoltageDiff();
    }

    /**
     * @return current of specified element from last measurement. If there is no measurement data, this method will block
     * until current simulation update is finished.
     */
    public double peekCurrent(int id) {
        return getLastElmRecord(id).getCurrent();
    }

    private ElementRecord getLastElmRecord(int id) {
        Measurement measurement = lastMeasurement.get();
        if (measurement != null) return measurement.getRecords().get(id);

        FutureTask<ElementRecord> task = new FutureTask<>(() -> {
            CircuitElm elm = getTrackedElement(id);
            return new ElementRecord(elm.getCurrent(), elm.getVoltageDiff());
        });
        executeOnSim(task);
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @return true is simulation engine is running, false if simulation engine is permanently stopped ({@link #kill()}
     * was called) or engine is in idle state.
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * @return true if simulation is alive (simulation thread is running).
     */
    public boolean isAlive() {
        return alive.get();
    }

    /**
     * @return date when this instance was created
     */
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    /**
     * Sends command to load circuit file from internal circuit collection (circuits/ folder)
     */
    public FutureTask<CircuitLoadResult> loadInternalCircuit(String fileName) {
        return loadCircuit(new File(JarUtils.getJarPath(CircuitManager.class), "circuits/" + fileName));
    }

    /**
     * Sends command to load circuit from specified file.
     */
    public FutureTask<CircuitLoadResult> loadCircuit(File file) {
        FutureTask<CircuitLoadResult> task = new FutureTask<>(() -> {
            CircuitLoadResult result = loader.loadCircuit(file);
            if (result == CircuitLoadResult.OK) {
                rebuildTrackedElements();
            } else {
                throw new IllegalStateException("Failed to load circuit file.");
            }
            return result;
        });
        executeOnSim(task);
        return task;
    }

    /**
     * Sends command to load circuit represented as graph from specified file.
     */
    public FutureTask<CircuitLoadResult> loadGraphCircuit(File file) {
        FutureTask<CircuitLoadResult> task = new FutureTask<>(() -> {
            CircuitLoadResult result = loader.loadGraphCircuit(file);
            if (result == CircuitLoadResult.OK) {
                rebuildTrackedElements();
            } else {
                throw new IllegalStateException("Failed to load graph represented circuit file.");
            }
            return result;
        });
        executeOnSim(task);
        return task;
    }

    /**
     * Sends command to append circuit circuit elements to current graph
     */
    public FutureTask<CircuitLoadResult> addGraphElements(String json) {
        FutureTask<CircuitLoadResult> task = new FutureTask<>(() -> {
            CircuitLoadResult result = loader.addGraphElements(json);
            if (result == CircuitLoadResult.OK) {
                rebuildTrackedElements();
            } else {
                throw new IllegalStateException("Failed to append to graph represented circuit.");
            }
            return result;
        });
        executeOnSim(task);
        return task;
    }

    public FutureTask<Void> deleteGraphElement(int elementId) {
        FutureTask<Void> task = new FutureTask<>(() -> {
            loader.deleteGraphElement(elementId);
            return null;
        });
        executeOnSim(task);
        return task;
    }

    /**
     * @return currently loaded graph as CMF
     */
    public String getGraphAsCmf() {
        return executeForResult(new FutureTask<>(() -> loader.getGraphAsCmf()));
    }

    /**
     * @return currently loaded graph as JSON string
     */
    public HashMap<Integer, List<Object>> getGraph() {
        return executeForResult(new FutureTask<>(() -> loader.getGraph()));
    }

    /**
     * @return currently loaded element map
     */
    public HashMap<Integer, Object> getGraphElements() {
        return executeForResult(new FutureTask<>(() -> {
            HashMap<Integer, List<Object>> graph = loader.getGraph();
            HashMap<Integer, Object> elements = new HashMap<>();
            for (List<Object> elm : graph.values()) {
                if (elm.get(0).equals("$")) continue;
                if (elm.get(1) instanceof Map) {
                    int elementId = (Integer) elm.get(3);
                    if (elementId == 0) continue;
                    elements.put(elementId, elm.get(1));
                } else {
                    int elementId = (Integer) elm.get(4);
                    if (elementId == 0) continue;
                    elements.put(elementId, new Point((Integer) elm.get(1), (Integer) elm.get(2)));
                }
            }
            return elements;
        }));
    }

    /**
     * Sends command to load circuit from specified string.
     *
     * @param newFormat set to true if string uses new save format (*.cmf, elementId is included)
     */
    public FutureTask<CircuitLoadResult> loadCircuit(String text, boolean newFormat) {
        FutureTask<CircuitLoadResult> task = new FutureTask<>(() -> {
            CircuitLoadResult result = loader.loadCircuit(text, false, newFormat);
            if (result == CircuitLoadResult.OK) {
                rebuildTrackedElements();
            } else {
                throw new IllegalStateException("Failed to load circuit file.");
            }
            return result;
        });
        executeOnSim(task);
        return task;
    }

    private void rebuildTrackedElements() {
        trackedElements.clear();
        for (CircuitElm elm : engine.getElmList()) {
            if (elm.flags2 == 0) continue;
            trackedElements.put(elm.flags2, elm);
        }
    }

    public void setMeasurableElements(List<Integer> measurableElementsIdList){
        measurableElements.clear();
        for (CircuitElm elm : engine.getElmList()) {
            if (elm.flags2 == 0) continue;
            if (measurableElementsIdList.contains(elm.flags2)) {
                measurableElements.put(elm.flags2, elm);
            }
        }
    }

    private CircuitElm getTrackedElement(int id) {
        CircuitElm elm = trackedElements.get(id);
        if (elm == null) throw new IllegalArgumentException("No such element with id: " + id);
        return elm;
    }

    /**
     * Sends command to start simulation.
     */
    public FutureTask<Void> start() {
        FutureTask<Void> task = new FutureTask<>(() -> {
            running.set(true);
            return null;
        });
        executeOnSim(task);
        return task;
    }

    /**
     * Sends command to start simulation for a specified amount of simulated circuit seconds. Note that
     * this method returns {@link FutureTask} however calling {@link FutureTask#get()} on it will only block
     * until command is executed and not until specified time actually passed. If you to block until specified time elapsed
     * use {@link #startForAndWait(double)} or poll on {@link #isRunning()} method until it returns false.
     * <p>
     * This should be only called when simulation is stopped. ({@link #isRunning()} returns false)
     */
    public FutureTask<Void> startFor(double circuitSeconds) {
        runTimerLatch = new AbortableCountDownLatch(1);
        FutureTask<Void> task = new FutureTask<>(() -> {
            runTimerEndTime = engine.t + circuitSeconds;
            running.set(true);
            return null;
        });
        executeOnSim(task);
        return task;
    }

    /**
     * Sends command to start simulation for a specified amount of simulated circuit seconds then blocks until
     * this time has elapsed.
     * <p>
     * This should be only called when simulation is stopped. ({@link #isRunning()} returns false)
     */
    public boolean startForAndWait(double circuitSeconds) {
        runTimerLatch = new AbortableCountDownLatch(1);
        executeOnSim(() -> {
            runTimerEndTime = engine.t + circuitSeconds;
            running.set(true);
        });
        try {
            runTimerLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return runTimerLatch.aborted;
    }

    /**
     * Sends command to stop simulation.
     */
    public FutureTask<Void> stop() {
        FutureTask<Void> task = new FutureTask<>(() -> {
            running.set(false);
            return null;
        });
        executeOnSim(task);
        return task;
    }

    /**
     * Sends command to permanently stop simulation. Simulation thread will exit right away if simulation engine is in
     * idle state, when it's running simulation thread will exit after currently run circuit update. After this command
     * finishes, this object should be only used to retrieve measurements using {@link #retrieveMeasurements()} and
     * {@link #dumpElements()} methods
     */
    public void kill() {
        executeOnSim(() -> alive.set(false));
    }

    /**
     * Sends command to that will specify peek interval. Peek interval specifies how often measured data will be saved.
     * Set to -1 to disable peeking.
     */
    public FutureTask<Void> setPeekInterval(double circuitSeconds) {
        FutureTask<Void> task = new FutureTask<>(() -> {
            this.lastPeek = engine.t;
            this.peekInterval = circuitSeconds;
            return null;
        });
        executeOnSim(task);
        return task;
    }

    public FutureTask<Void> setPokeInterval(double circuitSeconds) {
        FutureTask<Void> task = new FutureTask<>(() -> {
            this.lastPoke = engine.t;
            this.pokeInterval = circuitSeconds;
            return null;
        });
        executeOnSim(task);
        return task;
    }

    /**
     * Allows to synchronously modify state of simulation. Like others method it will return right away however listener
     * passed in parameter will be executed after simulation update in simulation thread.
     */
    public FutureTask<Void> modifyState(SimStateModifier modifier) {
        FutureTask<Void> task = new FutureTask<>(() -> {
            modifier.modifyState(propsController);
            return null;
        });
        executeOnSim(task);
        return task;
    }

    private void performMeasurement() {
        HashMap<Integer, ElementRecord> records = new HashMap<>();
        for (CircuitElm elm : measurableElements.values()) {
            records.put(elm.flags2, new ElementRecord(elm.getCurrent(), elm.getVoltageDiff()));
        }
        Measurement measurement = new Measurement(engine.t, records);
        measurements.add(measurement);
        lastMeasurement.set(measurement);
    }

    private void assignValuesFromArbitraryWave() {
        for(Map.Entry<String,ArrayList<Double>> entry : arbitraryWaveData.entrySet()){
            Integer key = Integer.parseInt(entry.getKey());
            ArrayList<Double> doubleArrayList = entry.getValue();
            if(doubleArrayList.size()>0) {
                Double voltage = doubleArrayList.remove(0);
                setElementProperty(key, "maxVoltage", voltage);
            }
        }
    }

    private void execute(Runnable task) {
        if (alive.get()) {
            executeOnSim(task);
        } else {
            task.run();
        }
    }

    private <T> T executeForResult(FutureTask<T> task) {
        if (alive.get()) {
            executeOnSim(task);
        } else {
            task.run();
        }
        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    private void executeOnSim(Runnable runnable) {
        synchronized (runnables) {
            runnables.add(runnable);
            idleLatch.countDown();
        }
    }

    /**
     * Allows to manipulate simulation properties after calling {@link #modifyState(SimStateModifier)} method.
     */
    public class PropertiesController {
        public void setControlVoltage(int id, double value) {
            VoltageElm elm = (VoltageElm) getTrackedElement(id);
            elm.setMaxVoltage(value);
            engine.setAnalyzeFlag();
        }

        public void setWireVoltage(int id, double value) {
            WireElm elm = (WireElm) getTrackedElement(id);
            elm.setNodeVoltage(0, value);
            engine.setAnalyzeFlag();
        }

        public void setSwitchState(int id, boolean on) {
            SwitchElm elm = (SwitchElm) getTrackedElement(id);
            if (on) {
                elm.setPosition(0);
            } else {
                elm.setPosition(1);
            }
            engine.setAnalyzeFlag();
        }

        public void setResistanceValue(int id, double value) {
            ResistorElm elm = (ResistorElm) getTrackedElement(id);
            elm.setResistance(value);
            engine.setAnalyzeFlag();
        }

        public CircuitElm getElementById(int id) {
            return getTrackedElement(id);
        }

        @SuppressWarnings("unchecked")
        public <V extends CircuitElm> V getElementById(int id, Class<V> clazz) {
            return (V) getTrackedElement(id);
        }

        public CircuitEngine getEngine() {
            return engine;
        }
    }
}
