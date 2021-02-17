package org.circuitsymphony.rest.controllers;

import org.circuitsymphony.Constants;
import org.circuitsymphony.rest.exceptions.SymphonyRestServiceException;
import org.circuitsymphony.rest.models.dto.*;
import org.circuitsymphony.rest.services.SimulationService;
import org.circuitsymphony.rest.services.StorageProperties;
import org.circuitsymphony.rest.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@RestController
public class SymphonyController {
    private StorageService storageService;
    private SimulationService simulationService;

    @Autowired
    public SymphonyController(StorageService storageService, SimulationService simulationService) {
        this.storageService = storageService;
        this.simulationService = simulationService;
    }

    /**
     * Returns list of all created simulations
     */
    @GetMapping("/symphony/simulations")
    public List<SimulationState> getSimulationsState() {
        return simulationService.getSimulationsState();
    }

    /**
     * Create new simulation and retrieve unique simulation key
     */
    @PostMapping("/symphony/simulations")
    public SimulationDTO createNewSimulation() {
        String key = simulationService.createNewSimulation();
        return new SimulationDTO(key);
    }

    /**
     * Load circuit file from internal circuit store specified by {@link StorageProperties#getLocation()}
     */
    @PostMapping("/symphony/simulations/{key}/loadInternalCircuit")
    public ResponseModel loadInternalCircuit(@PathVariable String key, @RequestParam String filename) {
        simulationService.loadInternalCircuit(key, filename);
        return new ResponseModel("loadInternalCircuit: " + filename);
    }

    /**
     * Load circuit from uploaded CMF file
     */
    @PostMapping("/symphony/simulations/{key}/loadCircuit")
    public ResponseModel loadCircuit(@PathVariable String key, @RequestParam MultipartFile file) {
        File f = storageService.storeAsTemp(file);
        simulationService.loadCircuit(key, f);
        return new ResponseModel("loadCircuit: " + f.getAbsolutePath());
    }

    /**
     * Load circuit represented by graph from uploaded JSON file
     */
    @PostMapping("/symphony/simulations/{key}/loadCircuitFromGraph")
    public ResponseModel loadCircuitFromGraph(@PathVariable String key, @RequestParam MultipartFile file) {
        File f = storageService.storeAsTemp(file);
        simulationService.loadGraphCircuit(key, f);
        return new ResponseModel("loadCircuitFromGraph: " + f.getAbsolutePath());
    }

    /**
     * Append graph elements to current circuit
     */
    @PostMapping("/symphony/simulations/{key}/addGraphElementsFromFile")
    public ResponseModel addGraphElementsFromFile(@PathVariable String key, @RequestParam MultipartFile file) throws IOException {
        simulationService.addGraphElements(key, new String(file.getBytes(), "UTF-8"));
        return new ResponseModel("addGraphElementsFromFile");
    }

    /**
     * Append graph elements to current circuit
     */
    @PostMapping("/symphony/simulations/{key}/addGraphElements")
    public ResponseModel addGraphElements(@PathVariable String key, @RequestBody String json) {
        simulationService.addGraphElements(key, json);
        return new ResponseModel("addGraphElements");
    }

    /**
     * Delete graph elements to current circuit
     */
    @PostMapping("/symphony/simulations/{key}/deleteGraphElement")
    public ResponseModel deleteGraphElement(@PathVariable String key, @RequestParam int elementId) {
        simulationService.deleteGraphElement(key, elementId);
        return new ResponseModel("deleteGraphElement: " + elementId);
    }

    /**
     * Starts simulation and returns immediately
     */
    @PostMapping("/symphony/simulations/{key}/start")
    public ResponseModel start(@PathVariable String key) {
        simulationService.start(key);
        return new ResponseModel("start");
    }

    /**
     * Starts simulation for specified period of simulated circuit seconds and returns immediately
     */
    @PostMapping("/symphony/simulations/{key}/startFor")
    public ResponseModel startFor(@PathVariable String key, @RequestParam double seconds) {
        simulationService.startFor(key, seconds);
        return new ResponseModel("startFor: " + seconds);
    }

    /**
     * Starts simulation for specified period of simulated circuit seconds and waits for it to finish
     * before completing the request.
     */
    @PostMapping("/symphony/simulations/{key}/startForAndWait")
    public ResponseModel startForAndWait(@PathVariable String key, @RequestParam double seconds) {
        boolean aborted = simulationService.startForAndWait(key, seconds);
        if(! aborted){
            return new ResponseModel("startForAndWait: " + seconds);
        }else{
            return new ResponseModel(Constants.SINGULAR_MSG);
        }
    }

    /**
     * Stops simulation, after this it can be still resumed
     */
    @PostMapping("/symphony/simulations/{key}/stop")
    public ResponseModel stop(@PathVariable("key") String key) {
        simulationService.stop(key);
        return new ResponseModel("stop");
    }

    /**
     * Kills simulation, after this it will become inaccessible
     */
    @PostMapping("/symphony/simulations/{key}/kill")
    public ResponseModel kill(@PathVariable("key") String key) {
        simulationService.kill(key);
        return new ResponseModel("kill");
    }

    /**
     * Get solver statistics
     * @return
     */
    @GetMapping("/symphony/simulations/{key}/statistics")
    public LogsResult statistics(@PathVariable("key") String key) {
        return new LogsResult(key,simulationService.getStatistics(key));
    }


    /**
     * Retrieve measurements from specified simulation as response file
     */
    @GetMapping("/symphony/simulations/{key}/measurements")
    public ResponseEntity<InputStreamResource> retrieveMeasurements(@PathVariable String key) {
        File measurementsFile = simulationService.retrieveMeasurements(key);
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.TEXT_PLAIN);
        respHeaders.setContentLength(measurementsFile.length());
        respHeaders.setContentDispositionFormData("attachment", "measurements.txt", Charset.forName("UTF-8"));

        InputStreamResource isr;
        try {
            isr = new InputStreamResource(new FileInputStream(measurementsFile));
        } catch (FileNotFoundException e) {
            throw new SymphonyRestServiceException("Can't download measurements file", e);
        }
        return new ResponseEntity<>(isr, respHeaders, HttpStatus.OK);
    }

    /**
     * Retrieve measurements from specified simulation as json
     */
    @GetMapping("/symphony/simulations/{key}/measurements_json")
    public MeasurementsResult retrieveMeasurementsJson(@PathVariable String key) {
        return new MeasurementsResult(key,simulationService.retrieveMeasurementsJson(key));
    }

    /**
     * Retrieve measurements from specified simulation as json
     */
    @GetMapping("/symphony/simulations/{key}/measurements_gzip")
    public ResponseModel retrieveMeasurementsGzip(@PathVariable String key) {
        return new ResponseModel(simulationService.retrieveMeasurementsGzip(key));
    }

    /**
     * Set arbitrary wave data from json
     */
    @PostMapping("/symphony/simulations/{key}/setArbwaveData")
    public ResponseModel retrieveMeasurementsJson(@PathVariable("key") String key, @RequestBody String json) {
//        return new MeasurementsResult(key,simulationService.retrieveMeasurementsJson(key));
        simulationService.setArbitraryWaveData(key,json);
        return new ResponseModel("setArbwaveData");
    }

    /**
     * By default all elements are tracked. This method allows one to track particular elements. Need to set peekInterval in order to take effect.
     */
    @PostMapping("/symphony/simulations/{key}/setMeasurableElements")
    public ResponseModel setMeasurableElements(@PathVariable("key") String key, @RequestBody String json){
        simulationService.setMeasurableElements(key,json);
        return new ResponseModel("setMeasurableElements");
    }


    /**
     * Changes peek and poke intervals of measurements and arb wave setting correspondingly
     */
    @PatchMapping("/symphony/simulations/{key}/settings")
    public ResponseModel setPeekInterval(@PathVariable("key") String key, @RequestParam double peekInterval, @RequestParam double pokeInterval) {
        simulationService.setPeekInterval(key, peekInterval);
        simulationService.setPokeInterval(key, pokeInterval);
        return new ResponseModel("setPeekInterval: " + peekInterval + " setPokeInterval: " + pokeInterval);
    }


    /**
     * Returns currently loaded graph circuit as CMF
     */
    @GetMapping("/symphony/simulations/{key}/graphAsCmf")
    public CmfResult getGraphAsCmf(@PathVariable String key) {
        return new CmfResult(key, simulationService.getGraphAsCmf(key));
    }

    /**
     * Returns currently loaded graph circuit as JSON string
     */
    @GetMapping("/symphony/simulations/{key}/currentCircuitAsJSONGraph")
    public GraphResult getGraph(@PathVariable String key) {
        return new GraphResult(key, simulationService.getGraph(key));
    }

    /**
     * Returns currently loaded element map
     */
    @GetMapping("/symphony/simulations/{key}/graphElements")
    public GraphElementsResult getGraphElements(@PathVariable String key) {
        return new GraphElementsResult(key, simulationService.getGraphElements(key));
    }

    /**
     * Returns simulation time
     */
    @PostMapping("/symphony/simulations/{key}/time")
    public TimeResult getTime(@PathVariable String key) {
        double time = simulationService.getTime(key);
        return new TimeResult(key, time);
    }

    /**
     * Returns simulation time when last measurement was performed
     */
    @PostMapping("/symphony/simulations/{key}/peekTime")
    public TimeResult peekTime(@PathVariable("key") String key) {
        double time = simulationService.peekTime(key);
        return new TimeResult(key, time);
    }

    /**
     * List of all circuit elements
     */
    @GetMapping("/symphony/simulations/{key}/elements")
    public ElementsResult getElements(@PathVariable String key) {
        return new ElementsResult(key, simulationService.getElements(key));
    }

    /**
     * List of all available element properties
     */
    @PostMapping("/symphony/simulations/{key}/element/{elementId}")
    public ElementPropertiesResult getElementProperties(@PathVariable String key, @PathVariable int elementId) {
        return new ElementPropertiesResult(elementId, simulationService.getElementProperties(key, elementId));
    }

    /**
     * Changes specified element property to given value
     */
    @PatchMapping("/symphony/simulations/{key}/element/{elementId}/property")
    public ElementPropertyResult setElementProperty(@PathVariable String key, @PathVariable int elementId,
                                                    @RequestParam String propertyKey, @RequestParam double newValue) {
        simulationService.setElementProperty(key, elementId, propertyKey, newValue);
        return new ElementPropertyResult(elementId, propertyKey, newValue);
    }

    /**
     * Returns value of specified element property
     */
    @PostMapping("/symphony/simulations/{key}/element/{elementId}/property")
    public ElementPropertyResult getElementProperty(@PathVariable String key, @PathVariable int elementId,
                                                    @RequestParam String propertyKey) {
        Object propValue = simulationService.getElementProperty(key, elementId, propertyKey);
        return new ElementPropertyResult(elementId, propertyKey, propValue);
    }

    /**
     * Returns voltage diff on specified element (real time)
     */
    @PostMapping("/symphony/simulations/{key}/element/{elementId}/voltageDiff")
    public ElementResult getVoltageDiff(@PathVariable String key, @PathVariable int elementId) {
        double voltageDiff = simulationService.getVoltageDiff(key, elementId);
        return new ElementResult(key, elementId, voltageDiff);
    }

    /**
     * Returns current on specified element (real time)
     */
    @PostMapping("/symphony/simulations/{key}/element/{elementId}/current")
    public ElementResult getCurrent(@PathVariable String key, @PathVariable int elementId) {
        double current = simulationService.getCurrent(key, elementId);
        return new ElementResult(key, elementId, current);
    }

    /**
     * Returns voltage diff on specified element when last measurement was performed
     */
    @PostMapping("/symphony/simulations/{key}/element/{elementId}/peekVoltageDiff")
    public ElementResult peekVoltageDiff(@PathVariable("key") String key, @PathVariable int elementId) {
        double voltageDiff = simulationService.peekVoltageDiff(key, elementId);
        return new ElementResult(key, elementId, voltageDiff);
    }

    /**
     * Returns current on specified element when last measurement was performed
     */
    @PostMapping("/symphony/simulations/{key}/element/{elementId}/peekCurrent")
    public ElementResult peekCurrent(@PathVariable("key") String key, @PathVariable int elementId) {
        double current = simulationService.peekCurrent(key, elementId);
        return new ElementResult(key, elementId, current);
    }
}
