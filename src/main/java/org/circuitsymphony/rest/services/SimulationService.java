package org.circuitsymphony.rest.services;

import org.circuitsymphony.manager.CircuitManager;
import org.circuitsymphony.manager.Measurement;
import org.circuitsymphony.rest.models.dto.SimulationState;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface SimulationService {
    CircuitManager getSimIfExist(String key);

    List<SimulationState> getSimulationsState();

    String createNewSimulation();

    void loadCircuit(String key, File file);

    void loadGraphCircuit(String key, File file);

    void addGraphElements(String key, String json);

    void deleteGraphElement(String key, int elementId);

    String getGraphAsCmf(String key);

    HashMap<Integer, List<Object>> getGraph(String key);

    HashMap<Integer, Object> getGraphElements(String key);

    void loadInternalCircuit(String key, String filename);

    void start(String key);

    void startFor(String key, double circuitsSeconds);

    boolean startForAndWait(String key, double circuitsSeconds);

    void stop(String key);

    void kill(String key);

    void setPeekInterval(String key, double peekInterval);

    void setPokeInterval(String key, double pokeInterval);

    List<Object> getElements(String key);

    List<String> getElementProperties(String key, int elementId);

    Object getElementProperty(String key, int elementId, String propertyKey);

    void setElementProperty(String key, int elementId, String propertyKey, Object newValue);

    File retrieveMeasurements(String key);

    ArrayList<Measurement> retrieveMeasurementsJson(String key);

    ArrayList<String> getStatistics(String key);

    String retrieveMeasurementsGzip(String key);

    double getTime(String key);

    double getVoltageDiff(String key, int elementId);

    double getCurrent(String key, int elementId);

    double peekTime(String key);

    double peekVoltageDiff(String key, int elementId);

    double peekCurrent(String key, int elementId);

    void setMeasurableElements(String key, String json);

    void setArbitraryWaveData(String key, String json);

}
