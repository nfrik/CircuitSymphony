package test;

import org.circuitsymphony.manager.CircuitManager;
import org.circuitsymphony.util.JarUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

public class OOMTest {
    private void isolatedTest(int taskN) throws InterruptedException, ExecutionException {
        System.out.println("Running task: "+taskN);
        CircuitManager simulation = new CircuitManager();
        // Load circuit, wait for load to finish
        File file = new File(JarUtils.getJarPath(CircuitManager.class), "/research/large_circ12k.json");
        simulation.loadGraphCircuit(file).get();
        // Specify elements which need to be measured
        simulation.setMeasurableElements(new ArrayList<>((Arrays.asList(12002,12004,12006))));
        // Specify to measure parameters each 1 ms, wait for command to finish
        simulation.setPeekInterval(0.001).get();
        // Run simulation for 100 ms
        simulation.startForAndWait(.0000500);
        // Disable measuring, wait for command to finish
        simulation.setPeekInterval(-1).get();

        // Peek some measurements, simulation may be running at this point.
        System.out.println(simulation.peekTime());
        System.out.println(simulation.peekCurrent(8002));
        System.out.println(simulation.peekVoltageDiff(8004));
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
    }

    private final Executor executor = Executors.newFixedThreadPool(16);

    public void executeTask(Runnable task){
        executor.execute(task);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ArrayList<Integer> procList = new ArrayList<>();

        OOMTest oomTest = new OOMTest();
        //oomTest.isolatedTest(0);
        Thread.sleep(2000);
        //Runnable task = null;
        for(int i =0;i<500;i++) {
            int finalI = i;
            Runnable task = (new Runnable() {
                @Override
                public void run() {
                    System.out.println();
                    OOMTest oomTest = new OOMTest();
                    try {
                        Thread.sleep(new Random().nextInt(3000));
                        oomTest.isolatedTest(finalI);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            });

            oomTest.executeTask(task);
        }

        System.out.print("All done");

    }
}
