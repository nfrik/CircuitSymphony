package test;

import org.circuitsymphony.evolution.EvolElement;
import org.circuitsymphony.evolution.TTGen;
import org.circuitsymphony.manager.CircuitManager;
import org.jenetics.util.DoubleRange;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

/**
 * Created by nfrik on 10/9/17.
 */
public class GAEvolverTest {



    public static void main(String[] args) throws InterruptedException, ExecutionException{
//           Input 1 61
//           Input 2 62
//           Input 3 63
//        Resistor 1 171
//        Resistor 2 172
//        Resistor 3 173
//        Resistor 4 174
//        Resistor 5 175
//        Resistor 6 176
//        Resistor 7 177
//        Resistor 8 178
//        Resistor 9 179
//        Resistor 10 180
//        Resistor 11 181
//        Resistor 12 182
//        Resistor 13 183
//        Map<Integer,List<DoubleRange>> manifestmap = new HashMap<>();
//        manifestmap.put(171, DoubleRange.of(10.,3000.));
//        manifestmap.put(172, DoubleRange.of(10.,3000.));
//        manifestmap.put(173, DoubleRange.of(10.,3000.));
//        manifestmap.put(174, DoubleRange.of(10.,3000.));
//        manifestmap.put(175, DoubleRange.of(10.,3000.));
//        manifestmap.put(176, DoubleRange.of(10.,3000.));
//        manifestmap.put(177, DoubleRange.of(10.,3000.));
//        manifestmap.put(178, DoubleRange.of(10.,3000.));
//        manifestmap.put(179, DoubleRange.of(10.,3000.));
//        manifestmap.put(180, DoubleRange.of(10.,3000.));
//        manifestmap.put(181, DoubleRange.of(10.,3000.));
//        manifestmap.put(182, DoubleRange.of(10.,3000.));
//        manifestmap.put(183, DoubleRange.of(10.,3000.));

//        Map<Integer, EvolElement> manifest = new HashMap<>();
//        manifest.put(61, new EvolElement(EvolElement.ELEM.INPUT,null));
//        manifest.put(62, new EvolElement(EvolElement.ELEM.INPUT,null));
//        manifest.put(63, new EvolElement(EvolElement.ELEM.INPUT,null));
//        manifest.put(171, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(172, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(173, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(174, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(175, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(176, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(177, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(178, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(179, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(180, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(181, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(182, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(183, new EvolElement(EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
//        manifest.put(201, new EvolElement(EvolElement.ELEM.OUTPUT,null));

        Map<Integer, EvolElement> manifest = new HashMap<>();
        manifest.put(1, new EvolElement(1,EvolElement.ELEM.INPUT,null));
        manifest.put(2, new EvolElement(2,EvolElement.ELEM.INTERNAL,DoubleRange.of(10.,300.)));
        manifest.put(3, new EvolElement(3,EvolElement.ELEM.OUTPUT,null));
        manifest.put(4, new EvolElement(4,EvolElement.ELEM.INPUT,null));


        CircuitManager simulation = new CircuitManager();

        //load pre-annotated cmf file
//        simulation.loadInternalCircuit("../research/memnet.cmf");
        simulation.loadInternalCircuit("../research/simple.txt");

        TTGen ttgen = new TTGen();

        //Prepare truth table
        Vector<Vector<Double>> resultsAND = ttgen.getTestDataOfLen(10,TTGen.DATA.AND);

        for(Vector<Double> ttable: resultsAND){
            double input1 = ttable.get(0);
            double input2 = ttable.get(1);
            double output = ttable.get(2);

            //let simulator run for
            simulation.startForAndWait(0.1);

            simulation.modifyState(controller -> controller.setWireVoltage(1, 13)).get();
            simulation.modifyState(controller -> controller.setResistanceValue(171, 13)).get();
        }

        simulation.setPeekInterval(0.001).get();

        simulation.startForAndWait(0.1);//Run for 100 ms and wait
    }
}
