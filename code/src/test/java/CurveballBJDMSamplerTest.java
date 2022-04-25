import caterpillars.structures.BJDMMatrix;
import caterpillars.helpers.SwappableAndNewEdges;
import caterpillars.structures.SparseMatrix;
import caterpillars.config.Paths;
import caterpillars.structures.Vector;
import caterpillars.config.DatasetNames;
import caterpillars.helpers.SwappableLists;
import caterpillars.samplers.CurveballBJDMSampler;
import caterpillars.structures.Edge;
import caterpillars.structures.Matrix;
import caterpillars.utils.Config;
import caterpillars.utils.Transformer;
import caterpillars.utils.Timer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class CurveballBJDMSamplerTest {

    private final Transformer transformer = new Transformer();
    private final CurveballBJDMSampler sampler = new CurveballBJDMSampler();
    private final Random rnd = new Random();

    @Test
    public void noTransitions() {
        List<SingleSideSwappableMatrix> testCases = Lists.newArrayList();
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
        for (SingleSideSwappableMatrix test : testCases) {
            BJDMMatrix matrix = (BJDMMatrix) test.matrix;
            for (int t = 0; t < 100; t++) {
                boolean rowSwap = rnd.nextBoolean();
                SwappableLists swap = matrix.getSwappablesNewEdges(rnd, rowSwap);
                if (test.rowSwappable != rowSwap) {
                    Assert.assertNull(swap);
                } else {
                    int id1 = -1;
                    int id2 = -1;
                    int el1 = swap.swappable1;
                    int el2 = swap.swappable2;
                    
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
    @Ignore
    public void transition() {
        final String datasetPath = Paths.concat(Config.datasetsDir, DatasetNames.foodmart);
        final SparseMatrix inMatrix = this.transformer.createMatrix(datasetPath);
        final BJDMMatrix matrix = new BJDMMatrix(inMatrix);

        for (int t = 0; t < 1000; t++) {
            BJDMMatrix adjMatrix = new BJDMMatrix(matrix.getMatrix());
            
            SwappableLists snes = adjMatrix.getSwappablesNewEdges(rnd);
            if (snes == null) {
                continue;
            }
            
            double samplingProb = adjMatrix.curveballSamplingProb(snes, adjMatrix.getRows(), adjMatrix.getCols());
            
            List<SwappableAndNewEdges> swappables = matrix.fromListToSwappables(snes);
            for (SwappableAndNewEdges swappable : swappables) {
                adjMatrix.transition(
                        swappable.swappableEdge1,
                        swappable.swappableEdge2,
                        swappable.newEdge1,
                        swappable.newEdge2);
            }
            adjMatrix = new BJDMMatrix(adjMatrix.getMatrix());
            for (SwappableAndNewEdges swappable : swappables) {
                matrix.transition(
                            swappable.swappableEdge1,
                            swappable.swappableEdge2,
                            swappable.newEdge1,
                            swappable.newEdge2);
            }
            double adjSamplingProb = matrix.curveballSamplingProb(snes, adjMatrix.getRows(), adjMatrix.getCols());
            
            Assert.assertEquals(adjMatrix, matrix);
            Assert.assertEquals(adjMatrix.getEdgesSet(), matrix.getEdgesSet());
            Assert.assertEquals(adjMatrix.getRowToNumEqRowsMap(), matrix.getRowToNumEqRowsMap());
            Assert.assertEquals(adjMatrix.getBJDM(), matrix.getBJDM());
            System.out.println(samplingProb + " " + adjSamplingProb);
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
        System.out.println("Sample found.");
        final BJDMMatrix SampleM = new BJDMMatrix(sample);
        System.out.println("SampleM created.");
        System.out.println(M.getRowSums().length + " -- " + SampleM.getRowSums().length);
        System.out.println(M.getColSums().length + " --- " + SampleM.getColSums().length);
        Assert.assertArrayEquals(M.getRowSums(), SampleM.getRowSums());
        Assert.assertArrayEquals(M.getColSums(), SampleM.getColSums());
        Assert.assertEquals(M.getBJDM(), SampleM.getBJDM());
    }

    @Test
    @Ignore
    public void uniformity() {
        final Map<Matrix, Integer> matrixNumStatesMap = Maps.newHashMap();
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
            final Matrix matrix = entry.getKey();
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
