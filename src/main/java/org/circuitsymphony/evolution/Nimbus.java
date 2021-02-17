package org.circuitsymphony.evolution;

import org.circuitsymphony.manager.CircuitManager;
import org.jenetics.*;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionStatistics;
import org.jenetics.util.DoubleRange;
import org.jenetics.util.Factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import static org.jenetics.engine.EvolutionResult.toBestPhenotype;
import static org.jenetics.engine.limit.bySteadyFitness;


/**
 * Created by nfrik on 10/25/17.
 */
public class Nimbus {

    private CircuitLoadingMethod loadingMethod;

    private List<EvolElement> manifest;

    private Vector<Vector<Double>> testData;

    public Nimbus(String internalPath, List<EvolElement> manifest, Vector<Vector<Double>> testData) {
        this(manager -> manager.loadInternalCircuit(internalPath), manifest, testData);
    }

    public Nimbus(CircuitLoadingMethod loadingMethod, List<EvolElement> manifest, Vector<Vector<Double>> testData) {
        this.loadingMethod = loadingMethod;
        setManifest(manifest);
        setTestData(testData);
    }

    public Vector<Vector<Double>> getTestData() {
        return testData;
    }

    public void setTestData(Vector<Vector<Double>> testData) {
        this.testData = testData;
    }

    public List<EvolElement> getManifest() {
        return manifest;
    }

    public void setManifest(List<EvolElement> manifest) {
        this.manifest = manifest;
    }

    private Double eval(final Genotype<DoubleGene> gt) {
//        final double x = gt.getGene().doubleValue();
//        final double y = gt.getGene().doubleValue();
        final double x = gt.get(0,0).doubleValue();
        final double y = gt.get(1,0).doubleValue();
//        System.out.println(gt.getNumberOfGenes());
        return Math.cos(0.5 + Math.sin(x))*Math.cos(x)*Math.exp(Math.abs(y));
    }

    // Definition of the fitness function.
    private Double loss(final Genotype<DoubleGene> gt){
        // load diodeOR circuit and test data
        CircuitManager cm = null;
        Vector<Vector<Double>> testData = getTestData();
        Double resultVal=0.0;

        try {


            for(Vector<Double> v : testData){
                cm = new CircuitManager();
                loadingMethod.load(cm).get();

                double input1 = v.get(0);
                double input2 = v.get(1);
                double output = v.get(2);

                cm.setPeekInterval(0.001).get();
                // Run simulation for 100 ms
                cm.startForAndWait(.0100);

                //Set input values
                cm.modifyState(controller -> controller.setControlVoltage(1,input1)).get();
                cm.modifyState(controller -> controller.setControlVoltage(2,input2)).get();

//                System.out.println("Input1: "+input1+" Input2: "+input2);

                updateCircuit4Genotype(gt, cm);

                cm.startForAndWait(.0100);
                /*// Change circuit switch state, wait for command to finish
                simulation.modifyState(controller -> controller.setSwitchState(300, true)).get();*/
                // Change circuit switch state, wait for command to finish

                double outcalc = cm.peekVoltageDiff(5);

                resultVal+=Math.abs(output-outcalc);

                // Run simulation for 100 ms
                cm.startForAndWait(0.0100);

                cm.setPeekInterval(-1).get();
                //Terminate simulation
                cm.stop().get();
                cm.kill();

            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

//        final double x = gt.get(0,0).doubleValue();
//        final double y = gt.get(1,0).doubleValue();

//        return Math.cos(0.5 + Math.sin(x))*Math.cos(x)*Math.exp(Math.abs(y));
        System.out.println("Retval: "+resultVal);
        return resultVal;
    }

    private void updateCircuit4Genotype(Genotype<DoubleGene> gt, CircuitManager cm) throws InterruptedException, ExecutionException {
        int i = 0;
        List<EvolElement> ee = getManifest();
        for(EvolElement e: ee){

            //We want to skip all non internal elements
            if(e.getDefinition() == EvolElement.ELEM.INTERNAL) {
                double newRValue = gt.get(i, 0).doubleValue();
//                        System.out.println("Gt: "+gt.get(i,0));
                cm.modifyState(controller -> controller.setResistanceValue(e.getElemId(), newRValue)).get();
                i++;
            }
        }
    }


    public CircuitManager runSimulation(Integer generations, Integer population, Integer fitnessCutoff, Double mutator, Double alterer){//, Vector<Vector<Double>> evalData, Integer generations, Integer population, Double mutator, Double alterer){


        List<DoubleChromosome> doubleChromosomes = new ArrayList<>();
        for(EvolElement evolElement : getManifest()){
            if(evolElement.getDefinition() == EvolElement.ELEM.INTERNAL){
                doubleChromosomes.add(DoubleChromosome.of(evolElement.getRange().getMin(),evolElement.getRange().getMax()));
            }
        }

//        dc.add(DoubleChromosome.of(0.0, 2.0*Math.PI));
//        dc.add(DoubleChromosome.of(-10.0, 2.0*Math.PI));


        Factory<Genotype<DoubleGene>> gtf = Genotype.of(doubleChromosomes);


        final Engine<DoubleGene, Double> engine = Engine
                .builder(
                        this::loss,gtf)
                .populationSize(population)//500
                .optimize(Optimize.MINIMUM)
                .alterers(
                        new Mutator<>(mutator), //0.03
                        new MeanAlterer<>(alterer)) //0.6
                .build();


        final EvolutionStatistics<Double, ?>
                statistics = EvolutionStatistics.ofNumber();

        // Execute the GA (engine).
        final Phenotype<DoubleGene, Double> result = engine.stream()
                // Truncate the evolution stream if no better individual could
                // be found after 5 consecutive generations.
                .limit(bySteadyFitness(fitnessCutoff))
                // Terminate the evolution after maximal 100 generations.
                .limit(generations)//100
                .peek(statistics)
                .peek(r -> System.out.println(statistics))
                .collect(toBestPhenotype());

        System.out.println(result);


        try {
            CircuitManager finSim = new CircuitManager();
            loadingMethod.load(finSim).get();
            updateCircuit4Genotype(result.getGenotype(), finSim);
            return finSim;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new IllegalStateException("Failed to obtain simulation result");
    }


    public static void main(String[] args){
        List<EvolElement> manifest = new ArrayList<>();

        //Create manifest for circuit
        //Range values specify range of input voltages
        manifest.add(new EvolElement(1,EvolElement.ELEM.INPUT, DoubleRange.of(0,1)));
        manifest.add(new EvolElement(2,EvolElement.ELEM.INPUT, DoubleRange.of(0,1)));

        //These elements will be modified by the algorithm
        //Sequence is important
        manifest.add(new EvolElement(3,EvolElement.ELEM.INTERNAL, DoubleRange.of(2,3000)));
        manifest.add(new EvolElement(4,EvolElement.ELEM.INTERNAL, DoubleRange.of(10,20000)));

        //Output element which will value will be compared with target function
        manifest.add(new EvolElement(5,EvolElement.ELEM.OUTPUT, DoubleRange.of(-100,100)));


        // load target data for OR logic test - total 100 randomized pieces - should be changeable
        TTGen ga = new TTGen();
        Vector<Vector<Double>> testDataOfLen = ga.getTestDataOfLen(100, TTGen.DATA.OR);

        String rootCircuitPath = "../research/diodeOR.cmf";
        //Evolve circuit and obtain best configuration
        Nimbus nimbus = new Nimbus(rootCircuitPath, manifest, testDataOfLen);

        Integer generations = 400;
        Integer population = 50;
        Integer fitnessCutoff = 50;
        Double mutator = 0.03;
        Double alterer = 0.6;
        nimbus.runSimulation(generations,population,fitnessCutoff,mutator,alterer);

    }

}

