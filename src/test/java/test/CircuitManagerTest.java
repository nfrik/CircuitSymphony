package test;

import org.circuitsymphony.manager.CircuitManager;
import org.circuitsymphony.manager.Measurement;
import org.circuitsymphony.manager.MeasurementWriter;
import org.circuitsymphony.util.JarUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;


public class CircuitManagerTest {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Create circuit manager for managing single simulation. You can have as many CircuitManager as needed, each
        // represents single simulation
        CircuitManager simulation = new CircuitManager();
        // Load circuit, wait for load to finish
        simulation.loadInternalCircuit("../research/memnet.cmf").get();
        // Specify elements which need to be measured
        simulation.setMeasurableElements(new ArrayList<>((Arrays.asList(171,172,173))));
        // Specify to measure parameters each 1 ms, wait for command to finish
        simulation.setPeekInterval(0.001).get();
        // Run simulation for 100 ms
        simulation.startForAndWait(.100);
        /*// Change circuit switch state, wait for command to finish
        simulation.modifyState(controller -> controller.setSwitchState(300, true)).get();*/
        // Change circuit switch state, wait for command to finish
        simulation.modifyState(controller -> controller.setResistanceValue(171, 13)).get();
        // Run simulation for 100 ms
        simulation.startForAndWait(0.100);
        // Change circuit switch state, wait for command to finish
//        simulation.modifyState(controller -> controller.setSwitchState(300, false)).get();
        // Run simulation for 100 ms
        simulation.startForAndWait(0.100);
        // Disable measuring, wait for command to finish
        simulation.setPeekInterval(-1).get();

        // Peek some measurements, simulation may be running at this point.
        System.out.println(simulation.peekTime());
        System.out.println(simulation.peekCurrent(171));
        System.out.println(simulation.peekVoltageDiff(171));
//        System.out.println(simulation.peekVoltageDiff(1000));

        // More complex state modification, change value of resistor
//        simulation.modifyState(controller -> {
//            controller.getElementById(1000, ResistorElm.class).setResistance(1000);
//        }).get();


        // Send start command, don't wait for command to finish
        simulation.start();
        // Do some other operation on this thread, here we are just sleeping
        Thread.sleep(1000);
        // Send stop command and wait for it to finish
        simulation.stop().get();
        // Permanently stop simulation, simulation thread will exit
        simulation.kill();

        // Retrieve all measured data, internal data storage of simulation will be cleared
        ArrayList<Measurement> measurements = simulation.retrieveMeasurements();

        // Note that if you need to repeatedly retrieve data when simulation is running you should provide your own list
        // to save memory e.g:
        //existingAndReusedArrayList.clear(); // clear old data
        //ArrayList<Measurement> measurements = simulation.retrieveMeasurements(existingAndReusedArrayList); // fetch new data

        // Writing measurements to file. Prepare data writer:
        MeasurementWriter writer = new MeasurementWriter();
//        writer.setReplaceDotWithComma(false);
        // Output to repository root directory/target/TTGen-classes
        File outRoot = new File(JarUtils.getJarPath(CircuitManagerTest.class));
        // Write data of all elements to file
        writer.writeAllToFile(new File(outRoot, "testResults.txt"), measurements);
        // Write data of single element with id 300 to file
        writer.writeSingleElementToFile(new File(outRoot, "testResultsForElm300.txt"), 300, measurements);
        writer.writeSingleElementToFile(new File(outRoot, "testResultsForElm1000.txt"), 1000, measurements);
    }
}
