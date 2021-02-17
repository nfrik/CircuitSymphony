package org.circuitsymphony.rest.services;

import com.google.gson.Gson;
import org.circuitsymphony.manager.CircuitManager;
import org.circuitsymphony.manager.Measurement;
import org.circuitsymphony.manager.MeasurementWriter;
import org.circuitsymphony.rest.exceptions.SymphonyRestServiceException;
import org.circuitsymphony.rest.models.dto.SimulationState;
import org.circuitsymphony.ui.CircuitSymphony;
import org.circuitsymphony.util.Gzip;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class SimulationServiceImpl implements SimulationService {
    private Map<String, CircuitManager> simulations = new ConcurrentHashMap<>();

    @Override
    public CircuitManager getSimIfExist(String key) {
        CircuitManager simulation = simulations.get(key);
        if (simulation == null) {
            throw new SymphonyRestServiceException("Simulation with key '" + key + "' not found");
        }
        return simulation;
    }

    @Override
    public List<SimulationState> getSimulationsState() {
        List<SimulationState> states = new ArrayList<>();
        for (Map.Entry<String, CircuitManager> entry : simulations.entrySet()) {
            CircuitManager manager = entry.getValue();
            states.add(new SimulationState(entry.getKey(), manager.getDateCreated(), manager.isRunning()));
        }
        return states;
    }

    @Override
    public String createNewSimulation() {
        UUID uuid = UUID.randomUUID();
        String key = uuid.toString();
        simulations.put(key, new CircuitManager());
        return key;
    }

    @Override
    public void loadCircuit(String key, File file) {
        CircuitManager simulation = getSimIfExist(key);
        try {
            simulation.loadCircuit(file).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new SymphonyRestServiceException("Failed to load circuit", e);
        }
    }

    @Override
    public void loadGraphCircuit(String key, File file) {
        CircuitManager simulation = getSimIfExist(key);
        try {
            simulation.loadGraphCircuit(file).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new SymphonyRestServiceException("Failed to load circuit from graph", e);
        }
    }

    @Override
    public void addGraphElements(String key, String json) {
        CircuitManager simulation = getSimIfExist(key);
        try {
            simulation.addGraphElements(json).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new SymphonyRestServiceException("Failed to load circuit from graph", e);
        }
    }

    @Override
    public void deleteGraphElement(String key, int elementId) {
        CircuitManager simulation = getSimIfExist(key);
        try {
            simulation.deleteGraphElement(elementId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new SymphonyRestServiceException("Failed to load circuit from graph", e);
        }
    }

    @Override
    public String getGraphAsCmf(String key) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.getGraphAsCmf();
    }

    @Override
    public HashMap<Integer, List<Object>> getGraph(String key) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.getGraph();
    }

    @Override
    public HashMap<Integer, Object> getGraphElements(String key) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.getGraphElements();
    }

    @Override
    public void loadInternalCircuit(String key, String filename) {
        System.out.println(filename);
        CircuitManager simulation = getSimIfExist(key);
        try {
            //TODO can we upload file externally?
            simulation.loadInternalCircuit(filename).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new SymphonyRestServiceException("Failed to load internal circuit", e);
        }
    }

    @Override
    public void start(String key) {
        CircuitManager simulation = getSimIfExist(key);
        try {
            simulation.start().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new SymphonyRestServiceException("Failed to start the simulation", e);
        }
    }

    @Override
    public void startFor(String key, double circuitsSeconds) {
        CircuitManager simulation = getSimIfExist(key);
        try {
            simulation.startFor(circuitsSeconds).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new SymphonyRestServiceException("Failed to start the simulation", e);
        }
    }

    @Override
    public boolean startForAndWait(String key, double circuitsSeconds) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.startForAndWait(circuitsSeconds);
    }

    @Override
    public void stop(String key) {
        CircuitManager simulation = getSimIfExist(key);
        try {
            simulation.stop().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new SymphonyRestServiceException("Failed to stop the simulation", e);
        }
    }

    @Override
    public void kill(String key) {
        CircuitManager simulation = getSimIfExist(key);
        simulation.kill();
        simulations.remove(key);
    }

    @Override
    public void setPeekInterval(String key, double peekInterval) {
        CircuitManager simulation = getSimIfExist(key);
        try {
            simulation.setPeekInterval(peekInterval).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new SymphonyRestServiceException("Failed to set peek interval", e);
        }
    }

    @Override
    public void setPokeInterval(String key, double pokeInterval){
        CircuitManager simulation = getSimIfExist(key);
        try {
            simulation.setPokeInterval(pokeInterval).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new SymphonyRestServiceException("Failed to set peek interval", e);
        }
    }

    @Override
    public List<Object> getElements(String key) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.getElements();
    }

    @Override
    public List<String> getElementProperties(String key, int elementId) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.getElementPropertyList(elementId);
    }

    @Override
    public Object getElementProperty(String key, int elementId, String propertyKey) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.getElementProperty(elementId, propertyKey);
    }

    @Override
    public void setElementProperty(String key, int elementId, String propertyKey, Object newValue) {
        CircuitManager simulation = getSimIfExist(key);
        simulation.setElementProperty(elementId, propertyKey, newValue);
    }

    @Override
    public File retrieveMeasurements(String key) {
        CircuitManager simulation = getSimIfExist(key);
        ArrayList<Measurement> measurements = simulation.retrieveMeasurements();
        MeasurementWriter writer = new MeasurementWriter();
        File resultFile;
        try {
            resultFile = File.createTempFile("m_results", ".txt");
        } catch (IOException e) {
            throw new SymphonyRestServiceException("Failed to create file for storing measurements", e);
        }
        writer.writeAllToFile(resultFile, measurements);
        System.out.println(resultFile.getAbsoluteFile());
        return resultFile;
    }

    @Override
    public ArrayList<Measurement> retrieveMeasurementsJson(String key){
        CircuitManager simulation = getSimIfExist(key);
        ArrayList<Measurement> measurements = simulation.retrieveMeasurements();
        return measurements;
    }

    @Override
    public ArrayList<String> getStatistics(String key){
        CircuitManager simulation = getSimIfExist(key);
        ArrayList<String> logs = simulation.getStatistics();
        return logs;
    };

    @Override
    public String retrieveMeasurementsGzip(String key){
        CircuitManager simulation = getSimIfExist(key);
        String measurements = simulation.retrieveMeasurementsGzip();

        try {
            byte [] data = Gzip.compress(measurements);
            measurements = Base64.getEncoder().encodeToString((data));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return measurements;
    }

    @Override
    public double getTime(String key) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.getTime();
    }

    @Override
    public double getVoltageDiff(String key, int elementId) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.getVoltageDiff(elementId);
    }

    @Override
    public double getCurrent(String key, int elementId) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.getCurrent(elementId);
    }

    @Override
    public double peekTime(String key) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.peekTime();
    }

    @Override
    public double peekVoltageDiff(String key, int elementId) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.peekVoltageDiff(elementId);
    }

    @Override
    public double peekCurrent(String key, int elementId) {
        CircuitManager simulation = getSimIfExist(key);
        return simulation.peekCurrent(elementId);
    }

    @Override
    public void setMeasurableElements(String key, String json){
        CircuitManager simulation = getSimIfExist(key);
        Pattern p = Pattern.compile("\\d+");

        Matcher m = p.matcher(json);

        List<Integer> list = new ArrayList<>();
        while(m.find()){
            list.add(new Integer(m.group()));
        }

//        String [] jsonTokens = json.split(",");
//
//
//        int [] intArr = Stream.of(jsonTokens)
//                .mapToInt(strToken -> Integer.parseInt(strToken))
//                .toArray();
//
//        List<Integer> list = Arrays.stream(intArr)		// IntStream
//                .boxed()  		// Stream<Integer>
//                .collect(Collectors.toList());

        simulation.setMeasurableElements(list);

    }

    @Override
    public void setArbitraryWaveData(String key, String json){
        CircuitManager simulation = getSimIfExist(key);
        Gson gson = new Gson();

        HashMap<String,ArrayList<Double>> map = new HashMap<>();

        map = (HashMap<String, ArrayList<Double>>) gson.fromJson(json,map.getClass());

        simulation.setArbitraryWaveData(map);
    }
}
