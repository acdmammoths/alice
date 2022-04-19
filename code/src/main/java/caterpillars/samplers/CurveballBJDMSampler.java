package caterpillars.samplers;

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
import caterpillars.helpers.SwappableLists;
import caterpillars.structures.Edge;
import caterpillars.structures.Vector;
import diffusr.samplers.Sampler;
import caterpillars.utils.Timer;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
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

        for (int i = 0; i < numSwaps; i++) {
            timer.start();

            SwappableLists snes = matrix.getSwappablesNewEdges(rnd);

            if (snes == null) {
                continue;
            }

            List<SwappableAndNewEdges> swappables = matrix.fromListToSwappables(snes);

            double logNumEquivAdjMatrices = logNumEquivMatrices;
            
            // copy vectors for adj matrix
            List<Vector> rows = Lists.newArrayList();
            for (Vector row : matrix.getRows()) {
                rows.add(row.copy());
            }
            List<Vector> cols = Lists.newArrayList();
            for (Vector col : matrix.getCols())  {
                cols.add(col.copy());
            }
            if (snes.rowBased) {
                Vector[] swappableRows = new Vector[]{
                    matrix.getRowInstance(snes.swappable1),
                    matrix.getRowInstance(snes.swappable2)};
                for (SwappableAndNewEdges swappable : swappables) {
                    Vector[] newRows = matrix.getNewRows(swappableRows[0], swappableRows[1],
                            swappable.newEdge1, swappable.newEdge2);
                    logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(
                            logNumEquivAdjMatrices,
                            swappableRows[0],
                            swappableRows[1],
                            newRows[0],
                            newRows[1]);
                    swappableRows = newRows;
                    // update cols
                    Vector[] newCols = matrix.getNewCols(swappable.swappableEdge1, swappable.swappableEdge2,
                            swappable.newEdge1, swappable.newEdge2);
                    cols.add(swappable.newEdge1.col, newCols[0]);
                    cols.add(swappable.newEdge2.col, newCols[1]);
                }
                // update rows
                Vector newRow1 = new Vector(snes.new1);
                Vector newRow2 = new Vector(snes.new2);
                rows.add(snes.swappable1, newRow1);
                rows.add(snes.swappable2, newRow2);
            } else {
                Map<Vector, Integer> rowToEqRows = matrix.getRowToNumEqRowsMap();
                for (SwappableAndNewEdges swappable : swappables) {
                    Vector swappableRow1 = rows.get(swappable.swappableEdge1.row);
                    Vector swappableRow2 = rows.get(swappable.swappableEdge2.row);
                    Vector[] newRows = matrix.getNewRows(swappableRow1, swappableRow2,
                            swappable.newEdge1, swappable.newEdge2);
                    logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(
                            logNumEquivAdjMatrices,
                            swappableRow1,
                            swappableRow2,
                            newRows[0],
                            newRows[1],
                            rowToEqRows);
                    rows.add(swappable.swappableEdge1.row, newRows[0]);
                    rows.add(swappable.swappableEdge2.row, newRows[1]);
                    rowToEqRows.put(newRows[0], rowToEqRows.getOrDefault(newRows[0], 0) + 1);
                    rowToEqRows.put(newRows[1], rowToEqRows.getOrDefault(newRows[1], 0) + 1);
                    int newCount1 = rowToEqRows.getOrDefault(swappableRow1, 0) - 1;
                    if (newCount1 <= 0) {
                        rowToEqRows.remove(swappableRow1);
                    } else {
                        rowToEqRows.put(swappableRow1, newCount1);
                    }
                    int newCount2 = rowToEqRows.getOrDefault(swappableRow2, 0) - 1;
                    if (newCount2 <= 0) {
                        rowToEqRows.remove(swappableRow2);
                    } else {
                        rowToEqRows.put(swappableRow2, newCount2);
                    }
                }
                // update cols
                Vector newCol1 = new Vector(snes.new1);
                Vector newCol2 = new Vector(snes.new2);
                cols.add(snes.swappable1, newCol1);
                cols.add(snes.swappable2, newCol2);
            }

            double frac = Math.exp(logNumEquivMatrices - logNumEquivAdjMatrices)
                    * matrix.curveballSamplingProb(snes, matrix.getRows(), matrix.getCols()) / 
                    matrix.curveballSamplingProb(snes, rows, cols);

            final double acceptanceProb = Math.min(1, frac);

            if (rnd.nextDouble() <= acceptanceProb) {
                for (SwappableAndNewEdges swappable : swappables) {
                    matrix.transition(
                            swappable.swappableEdge1,
                            swappable.swappableEdge2,
                            swappable.newEdge1,
                            swappable.newEdge2);
                }
                logNumEquivMatrices = logNumEquivAdjMatrices;
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

        for (int i = 0; i < numSwaps; i++) {
            logNumEquivMatricesTracker.save(matrix, logNumEquivMatrices);

            SwappableLists snes = matrix.getSwappablesNewEdges(rnd);

            if (snes == null) {
                continue;
            }

            List<SwappableAndNewEdges> swappables = matrix.fromListToSwappables(snes);
            double logNumEquivAdjMatrices = logNumEquivMatrices;

            for (SwappableAndNewEdges swappable : swappables) {

                Vector swappableRow1 = matrix.getRowInstance(swappable.swappableEdge1.row);
                Vector swappableRow2 = matrix.getRowInstance(swappable.swappableEdge2.row);
                Vector[] newRows = matrix.getNewRows(swappable.swappableEdge1,
                        swappable.swappableEdge2,
                        swappable.newEdge1,
                        swappable.newEdge2);

                logNumEquivAdjMatrices = matrix.getLogNumEquivAdjMatrices(
                        logNumEquivAdjMatrices, swappableRow1, swappableRow2, newRows[0], newRows[1]);

                matrix.transition(
                        swappable.swappableEdge1,
                        swappable.swappableEdge2,
                        swappable.newEdge1,
                        swappable.newEdge2);
            }
            logNumEquivMatrices = logNumEquivAdjMatrices;
        }
    }

}
