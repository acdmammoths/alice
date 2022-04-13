/*
 * Copyright (C) 2022 Alexander Lee and Matteo Riondato
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
import caterpillars.structures.NaiveBJDMMatrix;
import caterpillars.helpers.SwappableAndNewEdges;
import caterpillars.structures.SparseMatrix;
import caterpillars.helpers.LogNumEquivMatricesTracker;
import caterpillars.structures.Vector;
import caterpillars.structures.Edge;
import diffusr.samplers.Sampler;
import caterpillars.utils.Timer;
import java.util.List;
import java.util.Random;

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

        final NaiveBJDMMatrix matrix = new NaiveBJDMMatrix(inMatrix);

        final Random rnd = new Random(seed);

        double logNumEquivMatrices = matrix.getLogNumEquivMatrices();

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);

        int i = 0;
        while (i < numSwaps) {
            timer.start();

            List<SwappableAndNewEdges> snes = matrix.getSwappablesNewEdges(rnd);
            
            double logNumEquivAdjMatrices = logNumEquivMatrices;
            
             for (SwappableAndNewEdges edge : snes) {

                Edge swappableEdge1 = edge.swappableEdge1;
                Edge swappableEdge2 = edge.swappableEdge2;
                Edge newEdge1 = edge.newEdge1;
                Edge newEdge2 = edge.newEdge2;

                final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
                final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
                final Vector[] newRows = matrix.getNewRows(swappableEdge1, swappableEdge2, newEdge1, newEdge2);

                logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(logNumEquivAdjMatrices, 
                        swappableRow1, swappableRow2, newRows[0], newRows[1]);
             }

            double frac = Math.exp(logNumEquivMatrices - logNumEquivAdjMatrices);
//            frac *= something;

            final double acceptanceProb = Math.min(1, frac);

            if (rnd.nextDouble() <= acceptanceProb) {

                for (SwappableAndNewEdges edge : snes) {

                    Edge swappableEdge1 = edge.swappableEdge1;
                    Edge swappableEdge2 = edge.swappableEdge2;
                    Edge newEdge1 = edge.newEdge1;
                    Edge newEdge2 = edge.newEdge2;

                    final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
                    final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
                    final Vector swappableCol1 = matrix.getColInstance(swappableEdge1.col);
                    final Vector swappableCol2 = matrix.getColInstance(swappableEdge2.col);
                    final Vector[] newRows = matrix.getNewRows(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
                    final Vector newRow1 = newRows[0];
                    final Vector newRow2 = newRows[1];
                    final Vector[] newCols = matrix.getNewCols(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
                    final Vector newCol1 = newCols[0];
                    final Vector newCol2 = newCols[1];

                    logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(logNumEquivMatrices,
                            swappableRow1,
                            swappableRow2,
                            newRow1,
                            newRow2);

                    matrix.transition(
                            swappableEdge1,
                            swappableEdge2,
                            newEdge1,
                            newEdge2,
                            swappableRow1,
                            swappableRow2,
                            swappableCol1,
                            swappableCol2,
                            newRow1,
                            newRow2,
                            newCol1,
                            newCol2);

                    logNumEquivMatrices = logNumEquivAdjMatrices;
                }
                i += snes.size();
            }
            timer.stop();
        }
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

        final NaiveBJDMMatrix matrix = new NaiveBJDMMatrix(inMatrix);

        final Random rnd = new Random(seed);

        double logNumEquivMatrices = matrix.getLogNumEquivMatrices();

        int i = 0;
        while (i < numSwaps) {
            logNumEquivMatricesTracker.save(matrix, logNumEquivMatrices);

            List<SwappableAndNewEdges> snes = matrix.getSwappablesNewEdges(rnd);

            for (SwappableAndNewEdges edge : snes) {

                Edge swappableEdge1 = edge.swappableEdge1;
                Edge swappableEdge2 = edge.swappableEdge2;
                Edge newEdge1 = edge.newEdge1;
                Edge newEdge2 = edge.newEdge2;

                final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
                final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
                final Vector swappableCol1 = matrix.getColInstance(swappableEdge1.col);
                final Vector swappableCol2 = matrix.getColInstance(swappableEdge2.col);
                final Vector[] newRows = matrix.getNewRows(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
                final Vector newRow1 = newRows[0];
                final Vector newRow2 = newRows[1];
                final Vector[] newCols = matrix.getNewCols(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
                final Vector newCol1 = newCols[0];
                final Vector newCol2 = newCols[1];

                final double logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(logNumEquivMatrices,
                        swappableRow1,
                        swappableRow2,
                        newRow1,
                        newRow2);

                matrix.transition(
                        swappableEdge1,
                        swappableEdge2,
                        newEdge1,
                        newEdge2,
                        swappableRow1,
                        swappableRow2,
                        swappableCol1,
                        swappableCol2,
                        newRow1,
                        newRow2,
                        newCol1,
                        newCol2);
                
                logNumEquivMatrices = logNumEquivAdjMatrices;
            }
            i += snes.size();
        }
    }

}
