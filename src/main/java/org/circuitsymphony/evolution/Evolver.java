package org.circuitsymphony.evolution;

import org.jenetics.*;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionStatistics;
import org.jenetics.engine.codecs;
import org.jenetics.util.DoubleRange;

import java.util.ArrayList;

import static org.jenetics.engine.EvolutionResult.toBestPhenotype;
import static org.jenetics.engine.limit.bySteadyFitness;

/**
 * Created by nfrik on 9/15/17.
 *
  Input 1 61
  Input 2 62
  Input 3 63
  Resistor 1 171
  Resistor 2 172
  Resistor 3 173
  Resistor 4 174
  Resistor 5 175
  Resistor 6 176
  Resistor 7 177
  Resistor 8 178
  Resistor 9 179
  Resistor 10 180
  Resistor 11 181
  Resistor 12 182
  Resistor 13 183
  Output    1 201
 */
public class Evolver {
    private static final double A = 10;
    private static final double S = 100;
    private static final double R = 5.120;
    private static final int N = 10;
    private static int call = 0;

    private static double fitness(final double[] x) {
//        Rastrigin function
        double value = A * N;
        for (int i = 0; i < N; ++i) {
            value += x[i] * x[i] - A * Math.cos(2.0 * Math.PI * x[i]);
        }
        call++;
        return value;
    }

    private static double fitness2(final double[] x) {
        double value = 1;
        for (int i = 0; i < N; ++i) {
            value *= Math.sin(x[i]) * Math.sin(x[i]) * Math.exp(x[i]/S);
        }

        return value;
    }

    private static double eggholder(final double[] x){
//        (-(y+47)*np.sin(np.sqrt(np.abs(x/2+(y+47))))-x*np.sin(np.sqrt(np.abs(x-(y+47)))))
        return -1*(-(x[1]+47.)*Math.sin(Math.sqrt(Math.abs(x[0]/2.+(x[1]+47.))))-x[0]*Math.sin(Math.sqrt(Math.abs(x[0]-(x[1]+47)))));
    }

    public static void main(final String[] args) {

//        ArrayList<DoubleRange> domains = new ArrayList<>();
//        domains.add(DoubleRange.of(-R,R));
//        domains.add(DoubleRange.of(-R,R));
//        domains.add(DoubleRange.of(-R,R));
        DoubleRange[] domains = new DoubleRange[2];
        domains[0]=DoubleRange.of(-R,R);

        final Engine<DoubleGene, Double> engine = Engine
                .builder(
                Evolver::fitness,
                codecs.ofVector(DoubleRange.of(-R, R), N))
                .populationSize(300)
                .optimize(Optimize.MINIMUM)
                .alterers(
                        new Mutator<>(0.03),
                        new MeanAlterer<>(0.6))
                .build();

        final EvolutionStatistics<Double, ?>
                statistics = EvolutionStatistics.ofNumber();

        final Phenotype<DoubleGene, Double> best = engine.stream()
                .limit(bySteadyFitness(300))
                .peek(statistics)
                .collect(toBestPhenotype());

        System.out.println(statistics);
        System.out.println(best);
        System.out.println("Total calls: "+call);
    }
}
