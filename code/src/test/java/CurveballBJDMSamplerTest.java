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
import caterpillars.structures.BJDMMatrix;
import caterpillars.helpers.SwappableAndNewEdges;
import caterpillars.structures.SparseMatrix;
import caterpillars.config.Paths;
import caterpillars.structures.Vector;
import caterpillars.config.DatasetNames;
import caterpillars.samplers.CurveballBJDMSampler;
import caterpillars.structures.Edge;
import caterpillars.utils.Config;
import caterpillars.utils.Transformer;
import caterpillars.utils.Timer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class CurveballBJDMSamplerTest {

    private final Transformer transformer = new Transformer();
    private final CurveballBJDMSampler sampler = new CurveballBJDMSampler();
    private final Random rnd = new Random();

    @Test
    @Ignore
    public void getNumEquivMatrices() {
        final Map<BJDMMatrix, Integer> matrixNumEquivMatricesMap = new HashMap<>();
        matrixNumEquivMatricesMap.put(new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {0, 1},
                                    {1, 0}
                                })),
                2);
        matrixNumEquivMatricesMap.put(new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {1, 1},
                                    {1, 0}
                                })),
                1);
        matrixNumEquivMatricesMap.put(new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {1, 1},
                                    {0, 0}
                                })),
                1);
        matrixNumEquivMatricesMap.put(new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {1, 1},
                                    {1, 1}
                                })),
                1);
        matrixNumEquivMatricesMap.put(new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {1, 0},
                                    {1, 1},
                                    {1, 1},
                                    {0, 1}
                                })),
                2);
        matrixNumEquivMatricesMap.put(new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {0, 1, 1},
                                    {1, 0, 0},
                                    {1, 0, 0}
                                })),
                1);
        matrixNumEquivMatricesMap.put(new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {1, 0, 1},
                                    {0, 1, 0},
                                    {1, 0, 0}
                                })),
                2);
        matrixNumEquivMatricesMap.put(new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {1, 0, 0},
                                    {0, 1, 0},
                                    {0, 0, 1},})),
                6);
        matrixNumEquivMatricesMap.put(new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {1, 0, 0},
                                    {0, 1, 0},
                                    {0, 0, 1},
                                    {1, 0, 1},
                                    {1, 1, 0},
                                    {0, 1, 1}
                                })),
                36);
        matrixNumEquivMatricesMap.put(new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {0, 0, 0, 1},
                                    {1, 1, 0, 0},
                                    {1, 0, 1, 1},})),
                1);
        for (Entry<BJDMMatrix, Integer> entry : matrixNumEquivMatricesMap.entrySet()) {
            final BJDMMatrix matrix = entry.getKey();
            final int expectedNumEquivMatrices = entry.getValue();

            final long actualNumEquivMatrices = Math.round(Math.exp(matrix.getLogNumEquivMatrices()));

            Assert.assertEquals(expectedNumEquivMatrices, actualNumEquivMatrices);
        }
    }

    @Test
    @Ignore
    public void getNumEquivAdjMatrices() {
        final List<AdjMatrixTestCase> testCases = new ArrayList<>();
        // different row sums
        testCases.add(new AdjMatrixTestCase(
                        new BJDMMatrix(
                                new SparseMatrix(
                                        new int[][]{
                                            {1, 0, 0, 0},
                                            {1, 1, 0, 1},
                                            {0, 0, 1, 0},
                                            {0, 0, 1, 0}
                                        })),
                        new BJDMMatrix(
                                new SparseMatrix(
                                        new int[][]{
                                            {0, 0, 1, 0},
                                            {1, 1, 0, 1},
                                            {1, 0, 0, 0},
                                            {0, 0, 1, 0}
                                        })),
                        new Edge(0, 0),
                        new Edge(2, 2)));
        // different row sums
        testCases.add(new AdjMatrixTestCase(
                        new BJDMMatrix(
                                new SparseMatrix(
                                        new int[][]{
                                            {1, 0, 0, 0},
                                            {1, 1, 0, 0},
                                            {0, 0, 1, 0},
                                            {0, 0, 1, 1}
                                        })),
                        new BJDMMatrix(
                                new SparseMatrix(
                                        new int[][]{
                                            {1, 0, 0, 0},
                                            {1, 0, 1, 0},
                                            {0, 1, 0, 0},
                                            {0, 0, 1, 1}
                                        })),
                        new Edge(3, 2),
                        new Edge(1, 1)));
        // same dataset
        testCases.add(new AdjMatrixTestCase(
                        new BJDMMatrix(
                                new SparseMatrix(
                                        new int[][]{
                                            {1, 1, 0},
                                            {0, 0, 1},
                                            {1, 0, 0}
                                        })),
                        new BJDMMatrix(
                                new SparseMatrix(
                                        new int[][]{
                                            {1, 1, 0},
                                            {1, 0, 0},
                                            {0, 0, 1}
                                        })),
                        new Edge(1, 2),
                        new Edge(2, 0)));
        // same dataset
        testCases.add(new AdjMatrixTestCase(
                        new BJDMMatrix(
                                new SparseMatrix(
                                        new int[][]{
                                            {1, 1, 0},
                                            {1, 0, 1},
                                            {0, 1, 0}
                                        })),
                        new BJDMMatrix(
                                new SparseMatrix(
                                        new int[][]{
                                            {1, 0, 1},
                                            {1, 1, 0},
                                            {0, 1, 0}
                                        })),
                        new Edge(0, 1),
                        new Edge(1, 2)));
        for (AdjMatrixTestCase testCase : testCases) {
            final BJDMMatrix matrix = (BJDMMatrix) testCase.matrix;
            final BJDMMatrix adjMatrix = (BJDMMatrix) testCase.adjMatrix;
            final Edge swappableEdge1 = testCase.swappableEdge1;
            final Edge swappableEdge2 = testCase.swappableEdge2;
            final Edge newEdge1 = new Edge(swappableEdge1.row, swappableEdge2.col);
            final Edge newEdge2 = new Edge(swappableEdge2.row, swappableEdge1.col);
            final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
            final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
            final Vector[] newRows
                    = matrix.getNewRows(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
            final Vector newRow1 = newRows[0];
            final Vector newRow2 = newRows[1];

            final long expectedNumEquivAdjMatrices
                    = Math.round(Math.exp(adjMatrix.getLogNumEquivMatrices()));

            final double matrixLogNumEquivMatrices = matrix.getLogNumEquivMatrices();
            final long actualNumEquivAdjMatrices
                    = Math.round(
                            Math.exp(
                                    matrix.getLogNumEquivAdjMatrices(
                                            matrixLogNumEquivMatrices, swappableRow1, swappableRow2, newRow1, newRow2)));

            Assert.assertEquals(expectedNumEquivAdjMatrices, actualNumEquivAdjMatrices);
        }
    }

    @Test
    @Ignore
    public void transition() {
        final String datasetPath = Paths.concat(Config.datasetsDir, DatasetNames.foodmart);
        final SparseMatrix inMatrix = this.transformer.createMatrix(datasetPath);
        final BJDMMatrix matrix = new BJDMMatrix(inMatrix);

        for (int t = 0; t < 1000; t++) {
            BJDMMatrix adjMatrix = new BJDMMatrix(matrix.getMatrix());
            final SwappableAndNewEdges sne = adjMatrix.getSwappableAndNewEdges(this.rnd);
            final Edge swappableEdge1 = sne.swappableEdge1;
            final Edge swappableEdge2 = sne.swappableEdge2;
            final Edge newEdge1 = sne.newEdge1;
            final Edge newEdge2 = sne.newEdge2;
            adjMatrix.transition(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
            adjMatrix = new BJDMMatrix(adjMatrix.getMatrix());

            final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
            final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
            final Vector[] newRows
                    = adjMatrix.getNewRows(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
            final Vector newRow1 = newRows[0];
            final Vector newRow2 = newRows[1];
            matrix.transition(
                    swappableEdge1,
                    swappableEdge2,
                    newEdge1,
                    newEdge2,
                    swappableRow1,
                    swappableRow2,
                    newRow1,
                    newRow2);

            Assert.assertEquals(adjMatrix, matrix);
            Assert.assertEquals(adjMatrix.getEdgesSet(), matrix.getEdgesSet());
            Assert.assertEquals(adjMatrix.getRowToNumEqRowsMap(), matrix.getRowToNumEqRowsMap());
            Assert.assertEquals(adjMatrix.getBJDM(), matrix.getBJDM());
        }
    }

    @Test
    @Ignore
    public void equalMargins() {
        final String datasetPath = Paths.concat(Config.datasetsDir, DatasetNames.foodmart);
        final SparseMatrix matrix = this.transformer.createMatrix(datasetPath);
        final BJDMMatrix diffusrMatrix = new BJDMMatrix(matrix);
        final int numSwaps = 100;

        final SparseMatrix sample
                = this.sampler.sample(matrix, numSwaps, this.rnd.nextLong(), new Timer(false));
        final BJDMMatrix diffusrSampleMatrix = new BJDMMatrix(sample);

        Assert.assertArrayEquals(diffusrMatrix.getRowSums(), diffusrSampleMatrix.getRowSums());
        Assert.assertArrayEquals(diffusrMatrix.getColSums(), diffusrSampleMatrix.getColSums());
        Assert.assertEquals(diffusrMatrix.getBJDM(), diffusrSampleMatrix.getBJDM());
    }

//    @Test
//    public void uniformity() {
//        final Map<Matrix, Integer> matrixNumStatesMap = Maps.newHashMap();
//        matrixNumStatesMap.put(
//                new NaiveMatrix(
//                        new SparseMatrix(
//                                new int[][]{
//                                    {0, 1, 1},
//                                    {1, 0, 0},
//                                    {1, 0, 0},})),
//                3);
//        matrixNumStatesMap.put(
//                new NaiveMatrix(
//                        new SparseMatrix(
//                                new int[][]{
//                                    {1, 1, 0},
//                                    {1, 1, 0},
//                                    {0, 0, 1},})),
//                3);
//        matrixNumStatesMap.put(
//                new NaiveMatrix(
//                        new SparseMatrix(
//                                new int[][]{
//                                    {1, 1, 1, 0},
//                                    {0, 0, 1, 1},
//                                    {0, 0, 0, 1},
//                                    {0, 0, 0, 1}
//                                })),
//                8);
//        matrixNumStatesMap.put(
//                new NaiveMatrix(
//                        new SparseMatrix(
//                                new int[][]{
//                                    {1, 0, 1, 0},
//                                    {0, 1, 0, 1},})),
//                3);
//        matrixNumStatesMap.put(
//                new NaiveMatrix(
//                        new SparseMatrix(
//                                new int[][]{
//                                    {1, 0, 1, 0},
//                                    {1, 0, 1, 0},
//                                    {0, 1, 0, 1},
//                                    {0, 1, 0, 1},})),
//                6);
//        for (Entry<Matrix, Integer> entry : matrixNumStatesMap.entrySet()) {
//            final Matrix matrix = entry.getKey();
//            final int numStates = entry.getValue();
//            final int numSamples = numStates * 10_000;
//            final int numSwaps = 2 * matrix.getSum(); // as per our experiments
//            final Map<Dataset, Integer> datasetCounts = new HashMap<>();
//
//            for (int i = 0; i < numSamples; i++) {
//                final SparseMatrix sample
//                        = this.sampler.sample(
//                                matrix.getMatrix(), numSwaps, this.rnd.nextLong(), new Timer(false));
//                final Dataset dataset = new Dataset(sample);
//                final int count = datasetCounts.getOrDefault(dataset, 0) + 1;
//                datasetCounts.put(dataset, count);
//            }
//
//            final int numDatasets = datasetCounts.size();
//            Assert.assertEquals(numStates, numDatasets);
//
//            for (int count : datasetCounts.values()) {
//                final double expectedProb = (double) 1 / numStates;
//                final double actualProb = (double) count / numSamples;
//                final double delta = 0.1 * expectedProb;
//                Assert.assertEquals(expectedProb, actualProb, delta);
//            }
//        }
//    }
}
