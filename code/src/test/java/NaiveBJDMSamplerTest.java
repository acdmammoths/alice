import alice.samplers.BJDMSampler;
import alice.structures.BJDMMatrix;
import alice.helpers.SwappableAndNewEdges;
import alice.structures.SparseMatrix;
import alice.config.Paths;
import alice.structures.Vector;
import alice.config.DatasetNames;
import alice.structures.Edge;
import alice.structures.Matrix;
import alice.structures.SetMatrix;
import alice.utils.Config;
import alice.utils.Transformer;
import alice.utils.Timer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

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
public class NaiveBJDMSamplerTest {

    private final Transformer transformer = new Transformer();
    private final BJDMSampler sampler = new BJDMSampler();
    private final Random rnd = new Random();
    
    @Test
    public void noTransitions() {
        List<SingleSideSwappableMatrix<SetMatrix>> testCases = Lists.newArrayList();
        List<Set<Integer>> swappables = Lists.newArrayList();
        swappables.add(Sets.newHashSet(0, 1));
        swappables.add(Sets.newHashSet(2, 3));
        testCases.add(new SingleSideSwappableMatrix(new BJDMMatrix(new SparseMatrix(
                new int[][]{
                    {1, 1, 0},
                    {0, 1, 1},
                    {0, 1, 0},
                    {0, 0, 1}
                })),
                true,
                swappables));
        for (SingleSideSwappableMatrix<SetMatrix> test : testCases) {
            BJDMMatrix matrix = (BJDMMatrix) test.matrix;
            for (int t = 0; t < 100; t++) {
                boolean rowSwap = rnd.nextBoolean();
                SwappableAndNewEdges swap = matrix.getSwappableAndNewEdges(rnd, rowSwap);
                
                if (swap == null) {
                    continue;
                }
                
                if (test.rowSwappable != rowSwap) {
                    Assert.assertNull(swap);
                } else {
                    int id1 = -1;
                    int id2 = -1;
                    int el1 = (test.rowSwappable) ? swap.swappableEdge1.row : swap.swappableEdge1.col;
                    int el2 = (test.rowSwappable) ? swap.swappableEdge2.row : swap.swappableEdge2.col;
                    
                    for (int i = 0; i < test.swappableIndices.size(); i++) {
                        if (test.swappableIndices.get(i).contains(el1)) {
                            id1 = i;
                        }
                        if (test.swappableIndices.get(i).contains(el2)) {
                            id2 = i;
                        }
                    }
                    Assert.assertTrue(id1 == id2 && id1 != -1 & id2 != -1);
                }
            }
        }
    } 
    
    @Test
    public void getNumEquivAdjMatrices() {
        final List<AdjMatrixTestCase<SetMatrix>> testCases = new ArrayList<>();
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
        testCases.add(new AdjMatrixTestCase(
                        new BJDMMatrix(
                                new SparseMatrix(
                                        new int[][]{
                                            {1, 1, 0, 0},
                                            {1, 1, 0, 0},
                                            {0, 1, 1, 0},
                                            {0, 0, 1, 1}
                                        })),
                        new BJDMMatrix(
                                new SparseMatrix(
                                        new int[][]{
                                            {1, 0, 0, 1},
                                            {1, 1, 0, 0},
                                            {0, 1, 1, 0},
                                            {0, 1, 1, 0}
                                        })),
                        new Edge(0, 1),
                        new Edge(3, 3)));
        testCases.add(new AdjMatrixTestCase(
                        new BJDMMatrix(
                                new SparseMatrix(
                                        new int[][]{
                                            {1, 1, 0, 0},
                                            {1, 1, 0, 0},
                                            {0, 0, 1, 1}
                                        })),
                        new BJDMMatrix(
                                new SparseMatrix(
                                        new int[][]{
                                            {1, 1, 0, 0},
                                            {1, 0, 0, 1},
                                            {0, 1, 1, 0}
                                        })),
                        new Edge(1, 1),
                        new Edge(2, 3)));
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
        for (AdjMatrixTestCase<SetMatrix> testCase : testCases) {
            final BJDMMatrix matrix = (BJDMMatrix) testCase.matrix;
            final BJDMMatrix adjMatrix = (BJDMMatrix) testCase.adjMatrix;
            final Edge swappableEdge1 = testCase.swappableEdge1;
            final Edge swappableEdge2 = testCase.swappableEdge2;
            final Edge newEdge1 = new Edge(swappableEdge1.row, swappableEdge2.col);
            final Edge newEdge2 = new Edge(swappableEdge2.row, swappableEdge1.col);
            final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
            final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
            final Vector[] newRows = matrix.getNewRows(newEdge1, newEdge2);
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
    public void transition() {
        final String datasetPath = Paths.concat(Config.datasetsDir, DatasetNames.foodmart);
        final SparseMatrix inMatrix = this.transformer.createMatrix(datasetPath);
        final BJDMMatrix matrix = new BJDMMatrix(inMatrix);

        for (int t = 0; t < 1000; t++) {
            BJDMMatrix adjMatrix = new BJDMMatrix(matrix.getMatrix());
            final SwappableAndNewEdges sne = adjMatrix.getSwappableAndNewEdges(this.rnd);
            
            if (sne == null) {
                continue;
            }
            
            final Edge swappableEdge1 = sne.swappableEdge1;
            final Edge swappableEdge2 = sne.swappableEdge2;
            final Edge newEdge1 = new Edge(swappableEdge1.row, swappableEdge2.col);
            final Edge newEdge2 = new Edge(swappableEdge2.row, swappableEdge1.col);
            double samplingProb = adjMatrix.samplingProb(swappableEdge1, swappableEdge2);
            adjMatrix.transition(sne);
            adjMatrix = new BJDMMatrix(adjMatrix.getMatrix());

            final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
            final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
            final Vector[] newRows = matrix.getNewRows(newEdge1, newEdge2);
            final Vector newRow1 = newRows[0];
            final Vector newRow2 = newRows[1];
            matrix.transition(sne,
                    swappableRow1,
                    swappableRow2,
                    newRow1,
                    newRow2);
            double adjSamplingProb = matrix.samplingProb(newEdge1, newEdge2);
            Assert.assertEquals(adjMatrix, matrix);
            Assert.assertEquals(adjMatrix.getEdgesSet(), matrix.getEdgesSet());
            Assert.assertEquals(adjMatrix.getRowToNumEqRowsMap(), matrix.getRowToNumEqRowsMap());
            Assert.assertEquals(adjMatrix.getBJDM(), matrix.getBJDM());
            Assert.assertEquals(samplingProb, adjSamplingProb, 0.);
        }
    }

    @Test
    public void equalMarginsAndBJDM() {
        final String datasetPath = Paths.concat(Config.datasetsDir, DatasetNames.foodmart);
        final SparseMatrix matrix = this.transformer.createMatrix(datasetPath);
        final BJDMMatrix M = new BJDMMatrix(matrix);
        final int numSwaps = 100;

        final SparseMatrix sample
                = this.sampler.sample(matrix, numSwaps, this.rnd.nextLong(), new Timer(false));
        final BJDMMatrix SampleM = new BJDMMatrix(sample);

        Assert.assertArrayEquals(M.getRowSums(), SampleM.getRowSums());
        Assert.assertArrayEquals(M.getColSums(), SampleM.getColSums());
        Assert.assertEquals(M.getBJDM(), SampleM.getBJDM());
    }

    @Test
    public void uniformity() {
        final Map<SetMatrix, Integer> matrixNumStatesMap = Maps.newHashMap();
        matrixNumStatesMap.put(
                new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {0, 1, 1},
                                    {1, 0, 0},
                                    {1, 0, 0}})),
                1);
        matrixNumStatesMap.put(
                new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {1, 1, 0},
                                    {1, 1, 0},
                                    {0, 0, 1}})),
                1);
        matrixNumStatesMap.put(
                new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {0, 1, 0, 0, 0, 0},
                                    {1, 1, 0, 0, 0, 0},
                                    {0, 1, 1, 1, 0, 0},
                                    {0, 0, 0, 1, 0, 0}})),
                2);
        matrixNumStatesMap.put(
                new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {1, 0, 1, 0},
                                    {0, 1, 0, 1}})),
                3);
        matrixNumStatesMap.put(
                new BJDMMatrix(
                        new SparseMatrix(
                                new int[][]{
                                    {1, 0, 0, 0},
                                    {1, 1, 0, 0},
                                    {0, 1, 0, 0},
                                    {0, 1, 0, 0},
                                    {1, 0, 0, 0},
                                    {1, 1, 1, 0},
                                    {1, 1, 0, 1},
                                    {1, 0, 0, 0},
                                    {0, 1, 0, 0}})),
                1);

        matrixNumStatesMap.entrySet().stream().forEach(entry -> {
            final SetMatrix matrix = entry.getKey();
            final int numStates = entry.getValue();
            final int numSamples = numStates * 10_000;
            final int numSwaps = 2 * matrix.getNumEdges(); // as per our experiments
            Map<Dataset, Integer> datasetCounts = Maps.newHashMap();
            for (int i = 0; i < numSamples; i++) {
                final SparseMatrix sample = this.sampler.sample(
                        matrix.getMatrix(), 
                        numSwaps, 
                        this.rnd.nextLong(), 
                        new Timer(false));
                final Dataset dataset = new Dataset(sample);
                final int count = datasetCounts.getOrDefault(dataset, 0) + 1;
                datasetCounts.put(dataset, count);
            }
            Assert.assertEquals(numStates, datasetCounts.size());

            for (int count : datasetCounts.values()) {
                final double expectedProb = (double) 1 / numStates;
                final double actualProb = (double) count / numSamples;
                final double delta = 0.1 * expectedProb;
                Assert.assertEquals(expectedProb, actualProb, delta);
            }
        });
    }

}
