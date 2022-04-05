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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

/** A class to test {@link GmmtSampler}. */
public class GmmtSamplerTest {
  private final Transformer transformer = new Transformer();
  private final GmmtSampler sampler = new GmmtSampler();
  private final Random rnd = new Random();

  @Test
  public void getMatrixDegree() {
    final Map<GmmtMatrix, Integer> matrixDegreeMap = new HashMap<>();
    matrixDegreeMap.put(
        new GmmtMatrix(
            new SparseMatrix(
                new int[][] {
                  {0, 1},
                  {1, 0}
                })),
        1);
    matrixDegreeMap.put(
        new GmmtMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 1},
                  {1, 0}
                })),
        0);
    matrixDegreeMap.put(
        new GmmtMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 1},
                  {0, 0}
                })),
        0);
    matrixDegreeMap.put(
        new GmmtMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 1},
                  {1, 1}
                })),
        0);
    matrixDegreeMap.put(
        new GmmtMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 0},
                  {1, 1},
                  {1, 1},
                  {0, 1}
                })),
        1);
    matrixDegreeMap.put(
        new GmmtMatrix(
            new SparseMatrix(
                new int[][] {
                  {0, 1, 1},
                  {1, 0, 0},
                  {1, 0, 0}
                })),
        4);
    matrixDegreeMap.put(
        new GmmtMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 0, 1},
                  {0, 1, 0},
                  {1, 0, 0}
                })),
        3);

    for (Entry<GmmtMatrix, Integer> entry : matrixDegreeMap.entrySet()) {
      final GmmtMatrix matrix = entry.getKey();
      final int expectedMatrixDegree = entry.getValue();
      final int actualMatrixDegree = matrix.getDegree();

      Assert.assertEquals(expectedMatrixDegree, actualMatrixDegree);
    }
  }

  @Test
  public void getAdjMatrixDegree() {
    final List<AdjMatrixTestCase> testCases = new ArrayList<>();
    // no change in numZstructs or numK22Cliques
    testCases.add(
        new AdjMatrixTestCase(
            new GmmtMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0},
                      {1, 1},
                      {1, 1},
                      {0, 1}
                    })),
            new GmmtMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1},
                      {1, 1},
                      {1, 1},
                      {1, 0}
                    })),
            new Edge(0, 0),
            new Edge(3, 1)));
    testCases.add(
        new AdjMatrixTestCase(
            new GmmtMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 1},
                      {1, 0, 0},
                      {0, 0, 1},
                    })),
            new GmmtMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 1},
                      {0, 0, 1},
                      {1, 0, 0},
                    })),
            new Edge(1, 0),
            new Edge(2, 2)));
    // numZstructs minus 1 and no change in numK22Cliques
    testCases.add(
        new AdjMatrixTestCase(
            new GmmtMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0, 0, 0},
                      {1, 1, 0, 0, 0},
                      {0, 0, 1, 1, 0},
                      {0, 0, 1, 0, 0},
                      {0, 0, 0, 0, 1},
                    })),
            new GmmtMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0, 0, 0},
                      {1, 1, 0, 0, 0},
                      {0, 0, 0, 1, 1},
                      {0, 0, 1, 0, 0},
                      {0, 0, 1, 0, 0},
                    })),
            new Edge(2, 2),
            new Edge(4, 4)));
    // numZstructs plus 1 and no change in numK22Cliques
    testCases.add(
        new AdjMatrixTestCase(
            new GmmtMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0, 0, 0},
                      {1, 1, 0, 0, 0},
                      {0, 0, 0, 1, 1},
                      {0, 0, 1, 0, 0},
                      {0, 0, 1, 0, 0},
                    })),
            new GmmtMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0, 0, 0},
                      {1, 1, 0, 0, 0},
                      {0, 0, 1, 1, 0},
                      {0, 0, 1, 0, 0},
                      {0, 0, 0, 0, 1},
                    })),
            new Edge(2, 4),
            new Edge(4, 2)));
    // numZstructs and numK22Cliques minus 1
    testCases.add(
        new AdjMatrixTestCase(
            new GmmtMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0},
                      {1, 1, 0},
                      {0, 0, 1},
                    })),
            new GmmtMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0},
                      {1, 0, 1},
                      {0, 1, 0},
                    })),
            new Edge(1, 1),
            new Edge(2, 2)));
    // numZstructs and numK22Cliques plus 1
    testCases.add(
        new AdjMatrixTestCase(
            new GmmtMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0},
                      {1, 0, 1},
                      {0, 1, 0},
                    })),
            new GmmtMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0},
                      {1, 1, 0},
                      {0, 0, 1},
                    })),
            new Edge(1, 2),
            new Edge(2, 1)));

    for (AdjMatrixTestCase testCase : testCases) {
      final GmmtMatrix matrix = (GmmtMatrix) testCase.matrix;
      final GmmtMatrix adjMatrix = (GmmtMatrix) testCase.adjMatrix;
      final Edge swappableEdge1 = testCase.swappableEdge1;
      final Edge swappableEdge2 = testCase.swappableEdge2;
      final int matrixDegree = matrix.getDegree();
      final int expectedAdjMatrixDegree = adjMatrix.getDegree();

      final int actualAdjMatrixDegree =
          matrix.getAdjMatrixDegree(swappableEdge1, swappableEdge2, matrixDegree);

      Assert.assertEquals(expectedAdjMatrixDegree, actualAdjMatrixDegree);
    }
  }

  @Test
  public void transition() {
    final String datasetPath = Paths.concat(Paths.datasetsDir, DatasetNames.foodmart);
    final SparseMatrix inMatrix = this.transformer.createMatrix(datasetPath);
    final GmmtMatrix matrix = new GmmtMatrix(inMatrix);

    for (int t = 0; t < 1000; t++) {
      GmmtMatrix adjMatrix = new GmmtMatrix(matrix.getMatrix());
      final SwappableAndNewEdges sne = adjMatrix.getSwappableAndNewEdges(this.rnd);
      adjMatrix.transition(sne.swappableEdge1, sne.swappableEdge2, sne.newEdge1, sne.newEdge2);
      adjMatrix = new GmmtMatrix(adjMatrix.getMatrix());

      matrix.transition(sne.swappableEdge1, sne.swappableEdge2, sne.newEdge1, sne.newEdge2);

      Assert.assertEquals(adjMatrix, matrix);
      Assert.assertEquals(adjMatrix.getEdgesSet(), matrix.getEdgesSet());
    }
  }

  @Test
  public void equalMargins() {
    final String datasetPath = Paths.concat(Paths.datasetsDir, DatasetNames.foodmart);
    final SparseMatrix matrix = this.transformer.createMatrix(datasetPath);
    final Matrix diffusrMatrix = new Matrix(matrix);
    final int numSwaps = 100;

    final SparseMatrix sample =
        this.sampler.sample(matrix, numSwaps, this.rnd.nextLong(), new Timer(false));
    final Matrix diffusrSampleMatrix = new Matrix(sample);

    Assert.assertArrayEquals(diffusrMatrix.getRowSums(), diffusrSampleMatrix.getRowSums());
    Assert.assertArrayEquals(diffusrMatrix.getColSums(), diffusrSampleMatrix.getColSums());
  }

  @Test
  public void uniformity() {
    final Map<Matrix, Integer> matrixNumStatesMap = new HashMap<>();
    matrixNumStatesMap.put(
        new GmmtMatrix(
            new SparseMatrix(
                new int[][] {
                  {0, 1, 1},
                  {1, 0, 0},
                  {1, 0, 0},
                })),
        5);
    matrixNumStatesMap.put(
        new GmmtMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 1, 0},
                  {1, 1, 0},
                  {0, 0, 1},
                })),
        5);
    matrixNumStatesMap.put(
        new GmmtMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 1, 1, 0},
                  {0, 0, 1, 1},
                  {0, 0, 0, 1},
                  {0, 0, 0, 1}
                })),
        13);
    matrixNumStatesMap.put(
        new GmmtMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 0, 1, 0},
                  {0, 1, 0, 1},
                })),
        6);
    for (Entry<Matrix, Integer> entry : matrixNumStatesMap.entrySet()) {
      final Matrix matrix = entry.getKey();
      final int numStates = entry.getValue();
      final int numSamples = numStates * 10_000;
      final int numSwaps = 2 * matrix.getSum(); // as per our experiments
      final Map<SparseMatrix, Integer> sampleCounts = new HashMap<>();

      for (int i = 0; i < numSamples; i++) {
        final SparseMatrix sample =
            this.sampler.sample(
                matrix.getMatrix(), numSwaps, this.rnd.nextLong(), new Timer(false));
        final int count = sampleCounts.getOrDefault(sample, 0) + 1;
        sampleCounts.put(sample, count);
      }

      final int numDistinctSamples = sampleCounts.size();
      Assert.assertEquals(numStates, numDistinctSamples);

      for (int count : sampleCounts.values()) {
        final double expectedProb = (double) 1 / numDistinctSamples;
        final double actualProb = (double) count / numSamples;
        final double delta = 0.1 * expectedProb;
        Assert.assertEquals(expectedProb, actualProb, delta);
      }
    }
  }
}
