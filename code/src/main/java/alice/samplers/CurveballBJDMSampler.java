package alice.samplers;

import alice.structures.BJDMMatrix;
import alice.helpers.SwappableAndNewEdges;
import alice.structures.SparseMatrix;
import alice.helpers.LogNumEquivMatricesTracker;
import alice.helpers.SwappableLists;
import alice.structures.Edge;
import alice.structures.Vector;
import alice.utils.Timer;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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
 * ALICE-B Sampler.
 * 
 */
public class CurveballBJDMSampler implements Sampler {

    /**
     * @param inMatrix a {@link SparseMatrix} representation of the dataset
     * @param numSwaps the number of swaps to make such that the chain
     * sufficiently mixes
     * @param seed the random seed
     * @param timer a timer
     * @return the matrix representation of the sampled dataset
     */
    @Override
    public SparseMatrix sample(SparseMatrix inMatrix, int numSwaps, long seed, Timer timer) {
        final long setupTimeStart = System.currentTimeMillis();

        final BJDMMatrix matrix = new BJDMMatrix(inMatrix);

        final Random rnd = new Random(seed);

        double logNumEquivMatrices = matrix.getLogNumEquivMatrices();

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);
        for (int i = 0; i < numSwaps; i++) {
            timer.start();
            final SwappableLists snes = matrix.getSwappablesNewEdges(rnd);
            // no feasible swap at this round
            if (snes == null) {
                continue;
            }
            final List<SwappableAndNewEdges> swappables = matrix.fromListToSwappables(snes);
            // self-loop swap
            if (swappables.isEmpty()) {
                continue;
            }
            double logNumEquivAdjMatrices = logNumEquivMatrices;
            // temporary structure for computing num equivalent matrices
            Map<Vector, Integer> rowToEqRows = Maps.newHashMap();
            Vector new1 = new Vector(snes.new1);
            Vector new2 = new Vector(snes.new2);
            if (snes.rowBased) {
                logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(
                        logNumEquivAdjMatrices,
                        matrix.getRowInstance(snes.swappable1),
                        matrix.getRowInstance(snes.swappable2),
                        new1,
                        new2);
            } else {
                // incremental update
                for (SwappableAndNewEdges swappable : swappables) {
                    final Vector[] swappableRows = new Vector[]{
                        matrix.getRowInstance(swappable.swappableEdge1.row), 
                        matrix.getRowInstance(swappable.swappableEdge2.row)};
                    final Edge newEdge1 = new Edge(swappable.swappableEdge1.row, swappable.swappableEdge2.col);
                    final Edge newEdge2 = new Edge(swappable.swappableEdge2.row, swappable.swappableEdge1.col);
                    final Vector[] newRows = matrix.getNewRows(
                            swappableRows[0], 
                            swappableRows[1],
                            newEdge1, 
                            newEdge2);
                    logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(
                            logNumEquivAdjMatrices,
                            swappableRows[0],
                            swappableRows[1],
                            newRows[0],
                            newRows[1],
                            rowToEqRows);
                }
            }
//            final double firstProb = matrix.curveballSamplingProb(snes, matrix.getRows(), matrix.getCols());
//            final double secondProb = matrix.curveballSamplingProb(snes, rows, cols);
            final double frac = Math.exp(logNumEquivMatrices - logNumEquivAdjMatrices);
            final double acceptanceProb = Math.min(1, frac);

            if (rnd.nextDouble() <= acceptanceProb) {
                if (snes.rowBased) {
                    // update num equal rows
                    matrix.decNumEqRows(matrix.getRowInstance(snes.swappable1));
                    matrix.decNumEqRows(matrix.getRowInstance(snes.swappable2));
                    if (!matrix.incNumEqRows(new1)) {
                        new1 = new Vector(snes.new1);
                    }
                    if (!matrix.incNumEqRows(new2)) {
                        new2 = new Vector(snes.new2);
                    }
                    // update rows
                    matrix.setRow(snes.swappable1, new1);
                    matrix.setRow(snes.swappable2, new2);
                    
                    for (SwappableAndNewEdges swappable : swappables) {
                        // update cols
                        matrix.setCol(swappable.swappableEdge1.row, swappable.swappableEdge1.col, 0);
                        matrix.setCol(swappable.swappableEdge2.row, swappable.swappableEdge2.col, 0);
                        matrix.setCol(swappable.swappableEdge1.row, swappable.swappableEdge2.col, 1);
                        matrix.setCol(swappable.swappableEdge2.row, swappable.swappableEdge1.col, 1);
                        // update edges
                        matrix.edges.remove(swappable.swappableEdge1);
                        matrix.edges.remove(swappable.swappableEdge2);
                        matrix.edges.add(new Edge(swappable.swappableEdge1.row, swappable.swappableEdge2.col));
                        matrix.edges.add(new Edge(swappable.swappableEdge2.row, swappable.swappableEdge1.col));
                    }
                } else {
                    // update num equal rows
                    matrix.replaceNumEqRows(rowToEqRows);
                    
                    // update cols
                    matrix.setCol(snes.swappable1, new1);
                    matrix.setCol(snes.swappable2, new2);
                    for (SwappableAndNewEdges swappable : swappables) {
                        // update rows
                        matrix.setRow(swappable.swappableEdge1.row, swappable.swappableEdge1.col, 0);
                        matrix.setRow(swappable.swappableEdge2.row, swappable.swappableEdge2.col, 0);
                        matrix.setRow(swappable.swappableEdge1.row, swappable.swappableEdge2.col, 1);
                        matrix.setRow(swappable.swappableEdge2.row, swappable.swappableEdge1.col, 1);
                        // update edges
                        matrix.edges.remove(swappable.swappableEdge1);
                        matrix.edges.remove(swappable.swappableEdge2);
                        matrix.edges.add(new Edge(swappable.swappableEdge1.row, swappable.swappableEdge2.col));
                        matrix.edges.add(new Edge(swappable.swappableEdge2.row, swappable.swappableEdge1.col));
                    }
                }
                logNumEquivMatrices = logNumEquivAdjMatrices;
            }
            timer.stop();
        }
        return matrix.getMatrix();
    }
    
    public SparseMatrix sample(SparseMatrix inMatrix, long degree, int numSwaps, long seed, Timer timer) {
        final long setupTimeStart = System.currentTimeMillis();

        final BJDMMatrix matrix = new BJDMMatrix(inMatrix);

        final Random rnd = new Random(seed);

        double logNumEquivMatrices = matrix.getLogNumEquivMatrices();

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);
        for (int i = 0; i < numSwaps; i++) {
            timer.start();
            final SwappableLists snes = matrix.getSwappablesNewEdges(rnd);
            // no feasible swap at this round
            if (snes == null) {
                continue;
            }
            final List<SwappableAndNewEdges> swappables = matrix.fromListToSwappables(snes);
            // self-loop swap
            if (swappables.isEmpty()) {
                continue;
            }
            double logNumEquivAdjMatrices = logNumEquivMatrices;
            // temporary structure for computing num equivalent matrices
            Map<Vector, Integer> rowToEqRows = Maps.newHashMap();
            Vector new1 = new Vector(snes.new1);
            Vector new2 = new Vector(snes.new2);
            if (snes.rowBased) {
                logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(
                        logNumEquivAdjMatrices,
                        matrix.getRowInstance(snes.swappable1),
                        matrix.getRowInstance(snes.swappable2),
                        new1,
                        new2);
            } else {
                // incremental update
                for (SwappableAndNewEdges swappable : swappables) {
                    final Vector[] swappableRows = new Vector[]{
                        matrix.getRowInstance(swappable.swappableEdge1.row), 
                        matrix.getRowInstance(swappable.swappableEdge2.row)};
                    final Edge newEdge1 = new Edge(swappable.swappableEdge1.row, swappable.swappableEdge2.col);
                    final Edge newEdge2 = new Edge(swappable.swappableEdge2.row, swappable.swappableEdge1.col);
                    final Vector[] newRows = matrix.getNewRows(
                            swappableRows[0], 
                            swappableRows[1],
                            newEdge1, 
                            newEdge2);
                    logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(
                            logNumEquivAdjMatrices,
                            swappableRows[0],
                            swappableRows[1],
                            newRows[0],
                            newRows[1],
                            rowToEqRows);
                }
            }
//            final double firstProb = matrix.curveballSamplingProb(snes, matrix.getRows(), matrix.getCols());
//            final double secondProb = matrix.curveballSamplingProb(snes, rows, cols);
            final double frac = Math.exp(logNumEquivMatrices - logNumEquivAdjMatrices);
            final double acceptanceProb = Math.min(1, frac);

            if (rnd.nextDouble() <= acceptanceProb) {
                if (snes.rowBased) {
                    // update num equal rows
                    matrix.decNumEqRows(matrix.getRowInstance(snes.swappable1));
                    matrix.decNumEqRows(matrix.getRowInstance(snes.swappable2));
                    if (!matrix.incNumEqRows(new1)) {
                        new1 = new Vector(snes.new1);
                    }
                    if (!matrix.incNumEqRows(new2)) {
                        new2 = new Vector(snes.new2);
                    }
                    // update rows
                    matrix.setRow(snes.swappable1, new1);
                    matrix.setRow(snes.swappable2, new2);
                    
                    for (SwappableAndNewEdges swappable : swappables) {
                        // update cols
                        matrix.setCol(swappable.swappableEdge1.row, swappable.swappableEdge1.col, 0);
                        matrix.setCol(swappable.swappableEdge2.row, swappable.swappableEdge2.col, 0);
                        matrix.setCol(swappable.swappableEdge1.row, swappable.swappableEdge2.col, 1);
                        matrix.setCol(swappable.swappableEdge2.row, swappable.swappableEdge1.col, 1);
                        // update edges
                        matrix.edges.remove(swappable.swappableEdge1);
                        matrix.edges.remove(swappable.swappableEdge2);
                        matrix.edges.add(new Edge(swappable.swappableEdge1.row, swappable.swappableEdge2.col));
                        matrix.edges.add(new Edge(swappable.swappableEdge2.row, swappable.swappableEdge1.col));
                    }
                } else {
                    // update num equal rows
                    matrix.replaceNumEqRows(rowToEqRows);
                    
                    // update cols
                    matrix.setCol(snes.swappable1, new1);
                    matrix.setCol(snes.swappable2, new2);
                    for (SwappableAndNewEdges swappable : swappables) {
                        // update rows
                        matrix.setRow(swappable.swappableEdge1.row, swappable.swappableEdge1.col, 0);
                        matrix.setRow(swappable.swappableEdge2.row, swappable.swappableEdge2.col, 0);
                        matrix.setRow(swappable.swappableEdge1.row, swappable.swappableEdge2.col, 1);
                        matrix.setRow(swappable.swappableEdge2.row, swappable.swappableEdge1.col, 1);
                        // update edges
                        matrix.edges.remove(swappable.swappableEdge1);
                        matrix.edges.remove(swappable.swappableEdge2);
                        matrix.edges.add(new Edge(swappable.swappableEdge1.row, swappable.swappableEdge2.col));
                        matrix.edges.add(new Edge(swappable.swappableEdge2.row, swappable.swappableEdge1.col));
                    }
                }
                logNumEquivMatrices = logNumEquivAdjMatrices;
            }
            timer.stop();
        }
//        System.out.println("Actual Swaps: " + actualSwaps);
        return matrix.getMatrix();
    }

    /**
     * Saves the natural logarithm of the number of equivalent matrices for each
     * matrix the sampler reaches and always transitions to the adjacent matrix.
     * We always transition to the adjacent matrix so that the number of data
     * points we save is equal to the number of swaps (assuming there are no
     * hash collisions when saving the data points). This method is used
     * specifically for {@link DistortionExperiment}.
     *
     * @param inMatrix a {@link SparseMatrix} representation of the dataset
     * @param numSwaps the number of swaps to make such that the chain
     * sufficiently mixes
     * @param seed the random seed
     * @param logNumEquivMatricesTracker a tracker for the log of the number of
     * equivalent matrices
     */
    static void sample(
            SparseMatrix inMatrix,
            int numSwaps,
            long seed,
            LogNumEquivMatricesTracker logNumEquivMatricesTracker) {

        final BJDMMatrix matrix = new BJDMMatrix(inMatrix);

        final Random rnd = new Random(seed);

        double logNumEquivMatrices = matrix.getLogNumEquivMatrices();

        for (int i = 0; i < numSwaps; i++) {
            logNumEquivMatricesTracker.save(matrix.getMatrix(), logNumEquivMatrices);

            final SwappableLists snes = matrix.getSwappablesNewEdges(rnd);

            if (snes == null) {
                continue;
            }

            final List<SwappableAndNewEdges> swappables = matrix.fromListToSwappables(snes);
            
            if (swappables.isEmpty()) {
                continue;
            }
            
            double logNumEquivAdjMatrices = logNumEquivMatrices;

            for (SwappableAndNewEdges swappable : swappables) {

                final Vector swappableRow1 = matrix.getRowInstance(swappable.swappableEdge1.row);
                final Vector swappableRow2 = matrix.getRowInstance(swappable.swappableEdge2.row);
                final Edge newEdge1 = new Edge(swappable.swappableEdge1.row, swappable.swappableEdge2.col);
                final Edge newEdge2 = new Edge(swappable.swappableEdge2.row, swappable.swappableEdge1.col);
                final Vector[] newRows = matrix.getNewRows(newEdge1, newEdge2);

                logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(
                        logNumEquivAdjMatrices, swappableRow1, swappableRow2, newRows[0], newRows[1]);

                matrix.transition(swappable,
                                swappableRow1,
                                swappableRow2,
                                newRows[0],
                                newRows[1]);
            }
            logNumEquivMatrices = logNumEquivAdjMatrices;
        }
    }

    /**
     * @param inMatrix a {@link SparseMatrix} representation of the dataset
     * @param numSwaps the number of swaps to make such that the chain
     * sufficiently mixes
     * @param seed the random seed
     * @param timer a timer
     * @param stats stores the distances between consecutive BJDMs
     * @param numCater stores the number of caterpillars
     * @return the matrix representation of the sampled dataset
     */
    @Override
    public SparseMatrix sample(SparseMatrix inMatrix, 
            int numSwaps, 
            long seed, 
            Timer timer, 
            DescriptiveStatistics stats,
            DescriptiveStatistics numCater) {
        
        final long setupTimeStart = System.currentTimeMillis();

        final BJDMMatrix matrix = new BJDMMatrix(inMatrix);

        final Random rnd = new Random(seed);

        double logNumEquivMatrices = matrix.getLogNumEquivMatrices();

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);
        
        // starting BJDM vector
        double[] start = matrix.getBJDMVector(true);

        for (int i = 0; i < numSwaps; i++) {
            timer.start();
            final SwappableLists snes = matrix.getSwappablesNewEdges(rnd);
            // no feasible swap at this round
            if (snes == null) {
                continue;
            }
            final List<SwappableAndNewEdges> swappables = matrix.fromListToSwappables(snes);
            // self-loop swap
            if (swappables.isEmpty()) {
                continue;
            }
            double logNumEquivAdjMatrices = logNumEquivMatrices;
            // temporary structure for computing num equivalent matrices
            Map<Vector, Integer> rowToEqRows = Maps.newHashMap();
            Vector new1 = new Vector(snes.new1);
            Vector new2 = new Vector(snes.new2);
            if (snes.rowBased) {
                logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(
                        logNumEquivAdjMatrices,
                        matrix.getRowInstance(snes.swappable1),
                        matrix.getRowInstance(snes.swappable2),
                        new1,
                        new2);
            } else {
                // incremental update
                for (SwappableAndNewEdges swappable : swappables) {
                    final Vector[] swappableRows = new Vector[]{
                        matrix.getRowInstance(swappable.swappableEdge1.row), 
                        matrix.getRowInstance(swappable.swappableEdge2.row)};
                    final Edge newEdge1 = new Edge(swappable.swappableEdge1.row, swappable.swappableEdge2.col);
                    final Edge newEdge2 = new Edge(swappable.swappableEdge2.row, swappable.swappableEdge1.col);
                    final Vector[] newRows = matrix.getNewRows(
                            swappableRows[0], 
                            swappableRows[1],
                            newEdge1, 
                            newEdge2);
                    logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(
                            logNumEquivAdjMatrices,
                            swappableRows[0],
                            swappableRows[1],
                            newRows[0],
                            newRows[1],
                            rowToEqRows);
                }
            }
            final double frac = Math.exp(logNumEquivMatrices - logNumEquivAdjMatrices);
            final double acceptanceProb = Math.min(1, frac);

            if (rnd.nextDouble() <= acceptanceProb) {
                
                if (snes.rowBased) {
                    // update num equal rows
                    matrix.decNumEqRows(matrix.getRowInstance(snes.swappable1));
                    matrix.decNumEqRows(matrix.getRowInstance(snes.swappable2));
                    if (!matrix.incNumEqRows(new1)) {
                        new1 = new Vector(snes.new1);
                    }
                    if (!matrix.incNumEqRows(new2)) {
                        new2 = new Vector(snes.new2);
                    }
                    // update rows
                    matrix.setRow(snes.swappable1, new1);
                    matrix.setRow(snes.swappable2, new2);
                    
                    for (SwappableAndNewEdges swappable : swappables) {
                        // update cols
                        matrix.setCol(swappable.swappableEdge1.row, swappable.swappableEdge1.col, 0);
                        matrix.setCol(swappable.swappableEdge2.row, swappable.swappableEdge2.col, 0);
                        matrix.setCol(swappable.swappableEdge1.row, swappable.swappableEdge2.col, 1);
                        matrix.setCol(swappable.swappableEdge2.row, swappable.swappableEdge1.col, 1);
                        // update edges
                        matrix.edges.remove(swappable.swappableEdge1);
                        matrix.edges.remove(swappable.swappableEdge2);
                        matrix.edges.add(new Edge(swappable.swappableEdge1.row, swappable.swappableEdge2.col));
                        matrix.edges.add(new Edge(swappable.swappableEdge2.row, swappable.swappableEdge1.col));
                    }
                } else {
                    // update num equal rows
                    matrix.replaceNumEqRows(rowToEqRows);
                    
                    // update cols
                    matrix.setCol(snes.swappable1, new1);
                    matrix.setCol(snes.swappable2, new2);
                    for (SwappableAndNewEdges swappable : swappables) {
                        // update rows
                        matrix.setRow(swappable.swappableEdge1.row, swappable.swappableEdge1.col, 0);
                        matrix.setRow(swappable.swappableEdge2.row, swappable.swappableEdge2.col, 0);
                        matrix.setRow(swappable.swappableEdge1.row, swappable.swappableEdge2.col, 1);
                        matrix.setRow(swappable.swappableEdge2.row, swappable.swappableEdge1.col, 1);
                        // update edges
                        matrix.edges.remove(swappable.swappableEdge1);
                        matrix.edges.remove(swappable.swappableEdge2);
                        matrix.edges.add(new Edge(swappable.swappableEdge1.row, swappable.swappableEdge2.col));
                        matrix.edges.add(new Edge(swappable.swappableEdge2.row, swappable.swappableEdge1.col));
                    }
                }
                logNumEquivMatrices = logNumEquivAdjMatrices;
            }
            
            timer.stop();
            
            if (i % 100 == 0) {
                double distance = matrix.getDistanceFrom(start, true);
                stats.addValue(distance);
            } 
        }
        numCater.addValue(matrix.getNumCaterpillars());
        return matrix.getMatrix();
        
    }

}
