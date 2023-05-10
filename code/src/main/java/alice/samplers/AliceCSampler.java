package alice.samplers;

import alice.config.JsonKeys;
import alice.helpers.SwappableAndNewEdges;
import alice.structures.Edge;
import alice.structures.MultiGraph;
import alice.structures.RawFastIntCollectionFixedSizeWithOrder;
import alice.utils.CMDLineParser;
import alice.utils.Config;
import alice.utils.Timer;
import alice.utils.Transformer;
import java.io.IOException;
import java.util.Random;

/*
 * Copyright (C) 2022 Giulia Preti
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
/**
 * ALICE-C Sampler.
 */
public class AliceCSampler implements SeqSampler {

    /**
     * @param inGraph
     * @param numSwaps the number of swaps to make such that the chain
     * sufficiently mixes
     * @param seed the random seed
     * @param timer a timer
     * @return the matrix representation of the sampled dataset
     */
    @Override
    public MultiGraph sample(MultiGraph inGraph, int numSwaps, long seed, Timer timer) {
        
        final long setupTimeStart = System.currentTimeMillis();

        final Random rnd = new Random(seed);
        
        final MultiGraph graph = new MultiGraph(inGraph);

        double logNumEquivMatrices = graph.getLogNumEquivMatrices();

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);

        int actualSwaps = 0;
        for (int i = 0; i < numSwaps; i++) {
            timer.start();

            final SwappableAndNewEdges sne = graph.getSwappableAndNewEdges(rnd);
            
            if (sne == null) {
                continue;
            }
            
            final Edge swappableEdge1 = sne.swappableEdge1;
            final Edge swappableEdge2 = sne.swappableEdge2;
            final RawFastIntCollectionFixedSizeWithOrder swappableRow1 = graph.getRowInstance(swappableEdge1.row);
            final RawFastIntCollectionFixedSizeWithOrder swappableRow2 = graph.getRowInstance(swappableEdge2.row);
            final RawFastIntCollectionFixedSizeWithOrder[] newRows = graph.getNewRows(sne);
            final RawFastIntCollectionFixedSizeWithOrder newRow1 = newRows[0];
            final RawFastIntCollectionFixedSizeWithOrder newRow2 = newRows[1];

            final double logNumEquivAdjMatrices
                    = graph.getLogNumEquivAdjMatrices(
                            logNumEquivMatrices, swappableRow1, swappableRow2, newRow1, newRow2);

            final double frac = Math.exp(logNumEquivMatrices - logNumEquivAdjMatrices);
            final double acceptanceProb = Math.min(1, frac);
            
            if (rnd.nextDouble() <= acceptanceProb) {
                actualSwaps ++;
                graph.transition(sne, swappableRow1, swappableRow2, newRow1, newRow2);
                logNumEquivMatrices = logNumEquivAdjMatrices;
            }
            timer.stop();
        }
        System.out.println("Actual Swaps: " + actualSwaps);
        return graph;
    }
    
    public static void main(String[] args) throws IOException {
        CMDLineParser.parse(args);

        System.out.println("Executing runtime experiment for dataset at " + Config.datasetPath);

        final Transformer transformer = new Transformer();
        final MultiGraph graph = transformer.createMultiGraph(Config.datasetPath);
        final Random rnd = new Random(Config.seed);

        final Timer timer = new Timer(true);
        AliceCSampler sampler = new AliceCSampler();
        sampler.sample(graph, Config.numSwaps, rnd.nextLong(), timer);

        final long setupTime = timer.getSavedTime();
        final double minStepTime = timer.getMin();
        final double c10StepTime = timer.getPercentile(10);
        final double q1StepTime = timer.getPercentile(25);
        final double medianStepTime = timer.getPercentile(50);
        final double q3StepTime = timer.getPercentile(75);
        final double c90StepTime = timer.getPercentile(90);
        final double maxStepTime = timer.getMax();

        System.out.println("\t" + JsonKeys.runtimeStats + ":");
        System.out.println("\t\t" + JsonKeys.setupTime + ": " + setupTime);
        System.out.println("\t\t" + JsonKeys.minStepTime + ": " + minStepTime);
        System.out.println("\t\t" + JsonKeys.c10StepTime + ": " + c10StepTime);
        System.out.println("\t\t" + JsonKeys.q1StepTime + ": " + q1StepTime);
        System.out.println("\t\t" + JsonKeys.medianStepTime + ": " + medianStepTime);
        System.out.println("\t\t" + JsonKeys.q3StepTime + ": " + q3StepTime);
        System.out.println("\t\t" + JsonKeys.c90StepTime + ": " + c90StepTime);
        System.out.println("\t\t" + JsonKeys.maxStepTime + ": " + maxStepTime);
    }
    
}