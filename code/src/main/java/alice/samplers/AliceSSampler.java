package alice.samplers;

import alice.helpers.Swappables;
import alice.structures.Edge;
import alice.structures.MultiGraph;
import alice.structures.RawFastIntCollectionFixedSizeWithOrder;
import alice.utils.Timer;
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
 * ALICE-S Sampler.
 */
public class AliceSSampler implements SeqSampler {

    /**
     * @param inGraph observed graph
     * @param numSwaps the number of swaps to make such that the chain
     * sufficiently mixes
     * @param seed the random seed
     * @param timer a timer
     * @return the graph representation of the sampled dataset
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

            final Swappables sne = graph.getSwappables(rnd);
            
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
    
}