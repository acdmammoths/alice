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

public class RefinedSamplerTest {
  private final Transformer transformer = new Transformer();
  private final RefinedSampler sampler = new RefinedSampler();
  private final Random rnd = new Random();

  @Test
  public void getMatrixTotalNumSwapPairs() {
    final Map<RefinedMatrix, Integer> matrixTotalNumSwapPairsMap = new HashMap<>();
    matrixTotalNumSwapPairsMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 0},
                  {0, 1},
                })),
        0);
    matrixTotalNumSwapPairsMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 1},
                  {1, 0},
                })),
        0);
    matrixTotalNumSwapPairsMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 1},
                  {0, 0},
                })),
        0);
    matrixTotalNumSwapPairsMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 1},
                  {1, 1},
                })),
        0);
    matrixTotalNumSwapPairsMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 0},
                  {1, 1},
                  {1, 1},
                  {0, 1},
                })),
        0);
    matrixTotalNumSwapPairsMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {0, 1, 1},
                  {1, 0, 0},
                  {1, 0, 0}
                })),
        4);
    matrixTotalNumSwapPairsMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 0, 1},
                  {0, 1, 0},
                  {1, 0, 0}
                })),
        2);
    matrixTotalNumSwapPairsMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 1, 0},
                  {0, 0, 1},
                  {1, 0, 0}
                })),
        2);

    for (Entry<RefinedMatrix, Integer> entry : matrixTotalNumSwapPairsMap.entrySet()) {
      final RefinedMatrix matrix = entry.getKey();
      final int expectedMatrixTotalNumSwapPairs = entry.getValue();
      final int actualMatrixTotalNumSwapPairs = matrix.getDegreeAndTotalNumSwapPairs()[1];

      Assert.assertEquals(expectedMatrixTotalNumSwapPairs, actualMatrixTotalNumSwapPairs);
    }
  }

  @Test
  public void getAdjMatrixTotalNumSwapPairs() {
    final List<AdjMatrixTestCase> testCases = new ArrayList<>();
    // degree minus 1 and numEquivAdjMatrices plus 1
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1, 1},
                      {1, 0, 0},
                      {1, 0, 0}
                    })),
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1},
                      {0, 1, 0},
                      {1, 0, 0}
                    })),
            new Edge(0, 1),
            new Edge(1, 0)));
    // degree minus 1 and numEquivAdjMatrices plus 1
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1, 1},
                      {1, 0, 0},
                      {1, 0, 0}
                    })),
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0},
                      {0, 0, 1},
                      {1, 0, 0}
                    })),
            new Edge(0, 2),
            new Edge(1, 0)));
    // degree plus 1 and numEquivAdjMatrices minus 1
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0},
                      {0, 0, 1},
                      {1, 0, 0}
                    })),
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1, 1},
                      {1, 0, 0},
                      {1, 0, 0}
                    })),
            new Edge(0, 0),
            new Edge(1, 2)));
    // no change in degree and numEquivAdjMatrices
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0},
                      {0, 0, 1},
                      {1, 0, 0}
                    })),
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1},
                      {0, 1, 0},
                      {1, 0, 0}
                    })),
            new Edge(0, 1),
            new Edge(1, 2)));
    // degree plus 1 and numEquivAdjMatrices minus 1
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0, 0, 0},
                      {1, 1, 0, 0, 0},
                      {0, 0, 1, 1, 0},
                      {0, 0, 1, 0, 0},
                      {0, 0, 0, 0, 1},
                    })),
            new RefinedMatrix(
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
    // degree minus 1 and numEquivAdjMatrices plus 1
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0, 0, 0},
                      {1, 1, 0, 0, 0},
                      {0, 0, 0, 1, 1},
                      {0, 0, 1, 0, 0},
                      {0, 0, 1, 0, 0},
                    })),
            new RefinedMatrix(
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
    for (AdjMatrixTestCase testCase : testCases) {
      final RefinedMatrix matrix = (RefinedMatrix) testCase.matrix;
      final RefinedMatrix adjMatrix = (RefinedMatrix) testCase.adjMatrix;
      final Edge swappableEdge1 = testCase.swappableEdge1;
      final Edge swappableEdge2 = testCase.swappableEdge2;
      final Edge newEdge1 = new Edge(swappableEdge1.row, swappableEdge2.col);
      final Edge newEdge2 = new Edge(swappableEdge2.row, swappableEdge1.col);
      final int[] adjMatrixDegreeAndTotalNumSwapPairs = adjMatrix.getDegreeAndTotalNumSwapPairs();
      int adjMatrixDegree = adjMatrixDegreeAndTotalNumSwapPairs[0];
      final int expectedAdjMatrixTotalNumSwapPairs = adjMatrixDegreeAndTotalNumSwapPairs[1];

      final int[] matrixDegreeAndTotalNumSwapPairs = matrix.getDegreeAndTotalNumSwapPairs();
      final int matrixDegree = matrixDegreeAndTotalNumSwapPairs[0];
      final int matrixTotalNumSwapPairs = matrixDegreeAndTotalNumSwapPairs[1];
      adjMatrixDegree = matrix.getAdjMatrixDegree(swappableEdge1, swappableEdge2, matrixDegree);
      final int actualAdjMatrixTotalNumSwapPairs =
          matrix.getAdjMatrixTotalNumSwapPairs(
              swappableEdge1,
              swappableEdge2,
              newEdge1,
              newEdge2,
              matrixTotalNumSwapPairs,
              matrixDegree,
              adjMatrixDegree);

      Assert.assertEquals(expectedAdjMatrixTotalNumSwapPairs, actualAdjMatrixTotalNumSwapPairs);
    }
  }

  @Test
  public void getMatrixNumSwapPairs() {
    final List<MatrixNumSwapPairsTestCase> testCases = new ArrayList<>();
    // numSwapPairsFactor = 1
    testCases.add(
        new MatrixNumSwapPairsTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1, 1},
                      {1, 0, 0},
                      {1, 0, 0}
                    })),
            0,
            1,
            2));
    testCases.add(
        new MatrixNumSwapPairsTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1, 1},
                      {1, 0, 0},
                      {1, 0, 0}
                    })),
            0,
            2,
            2));
    testCases.add(
        new MatrixNumSwapPairsTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0},
                      {0, 0, 1},
                      {0, 0, 1}
                    })),
            0,
            1,
            2));
    testCases.add(
        new MatrixNumSwapPairsTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0},
                      {1, 1, 0},
                      {0, 0, 1},
                      {0, 0, 1}
                    })),
            0,
            2,
            4));
    testCases.add(
        new MatrixNumSwapPairsTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 1, 0},
                      {1, 1, 0},
                      {1, 1, 0},
                      {0, 0, 1}
                    })),
            2,
            3,
            3));
    testCases.add(
        new MatrixNumSwapPairsTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1, 0, 1, 1},
                      {0, 1, 0, 1, 0, 1},
                      {1, 1, 0, 0, 1, 1}
                    })),
            0,
            1,
            1));
    // numSwapPairsFactor = 2
    testCases.add(
        new MatrixNumSwapPairsTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1, 0},
                      {0, 1, 0, 1},
                    })),
            0,
            1,
            2));
    testCases.add(
        new MatrixNumSwapPairsTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1, 0, 1},
                      {0, 1, 0, 1, 1},
                      {1, 1, 0, 0, 1}
                    })),
            0,
            1,
            2));
    testCases.add(
        new MatrixNumSwapPairsTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1, 0},
                      {0, 1, 0, 1},
                      {0, 1, 0, 1},
                      {1, 0, 1, 0},
                      {1, 0, 1, 0},
                      {0, 1, 1, 1}
                    })),
            0,
            1,
            12));

    for (MatrixNumSwapPairsTestCase testCase : testCases) {
      final RefinedMatrix matrix = (RefinedMatrix) testCase.matrix;
      final Vector swappableRow1 = matrix.getRowInstance(testCase.swappableRowIndex1);
      final Vector swappableRow2 = matrix.getRowInstance(testCase.swappableRowIndex2);
      final int expectedMatrixNumSwapPairs = testCase.matrixNumSwapPairs;
      matrix.getDegreeAndTotalNumSwapPairs(); // execute this method to initialize eqRowsNumMap
      final int numSwapPairsFactor =
          matrix.getNumSwapPairsFactor(testCase.swappableRowIndex1, testCase.swappableRowIndex2);

      final int actualMatrixNumSwapPairs =
          matrix.getNumSwapPairs(numSwapPairsFactor, swappableRow1, swappableRow2);

      Assert.assertEquals(expectedMatrixNumSwapPairs, actualMatrixNumSwapPairs);
    }
  }

  @Test
  public void getAdjMatrixNumSwapPairs() {
    final List<AdjMatrixTestCase> testCases = new ArrayList<>();
    // numSwapPairsFactor = 1
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1},
                      {0, 1, 0},
                      {1, 0, 0}
                    })),
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1, 1},
                      {1, 0, 0},
                      {1, 0, 0}
                    })),
            new Edge(0, 0),
            new Edge(1, 1)));
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1, 1},
                      {1, 0, 0},
                      {1, 0, 0}
                    })),
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1},
                      {0, 1, 0},
                      {1, 0, 0}
                    })),
            new Edge(0, 1),
            new Edge(1, 0)));
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1, 1},
                      {1, 0, 0},
                      {1, 0, 0},
                      {1, 0, 0}
                    })),
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1},
                      {0, 1, 0},
                      {1, 0, 0},
                      {1, 0, 0}
                    })),
            new Edge(0, 1),
            new Edge(1, 0)));
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1, 1},
                      {0, 1, 1},
                      {1, 0, 0},
                      {1, 0, 0},
                      {1, 0, 0}
                    })),
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1, 1},
                      {1, 0, 1},
                      {0, 1, 0},
                      {1, 0, 0},
                      {1, 0, 0}
                    })),
            new Edge(1, 1),
            new Edge(2, 0)));
    // numSwapPairsFactor = 2
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1, 0},
                      {0, 1, 0, 1},
                    })),
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1, 1, 0},
                      {1, 0, 0, 1},
                    })),
            new Edge(0, 0),
            new Edge(1, 1)));
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1, 0},
                      {0, 1, 0, 1},
                    })),
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 0, 1, 1},
                      {1, 1, 0, 0},
                    })),
            new Edge(0, 0),
            new Edge(1, 3)));
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1, 0, 1},
                      {0, 1, 0, 1, 1},
                      {1, 1, 0, 0, 1}
                    })),
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 1, 1, 0, 1},
                      {1, 0, 0, 1, 1},
                      {1, 1, 0, 0, 1}
                    })),
            new Edge(0, 0),
            new Edge(1, 1)));
    testCases.add(
        new AdjMatrixTestCase(
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {1, 0, 1, 0},
                      {0, 1, 0, 1},
                      {0, 1, 0, 1},
                      {1, 0, 1, 0},
                      {1, 0, 1, 0},
                      {0, 1, 1, 1}
                    })),
            new RefinedMatrix(
                new SparseMatrix(
                    new int[][] {
                      {0, 0, 1, 1},
                      {1, 1, 0, 0},
                      {0, 1, 0, 1},
                      {1, 0, 1, 0},
                      {1, 0, 1, 0},
                      {0, 1, 1, 1}
                    })),
            new Edge(0, 0),
            new Edge(1, 3)));

    for (AdjMatrixTestCase testCase : testCases) {
      final RefinedMatrix matrix = (RefinedMatrix) testCase.matrix;
      final RefinedMatrix adjMatrix = (RefinedMatrix) testCase.adjMatrix;
      final Edge swappableEdge1 = testCase.swappableEdge1;
      final Edge swappableEdge2 = testCase.swappableEdge2;

      Vector newRow1;
      Vector newRow2;

      adjMatrix.getDegreeAndTotalNumSwapPairs(); // execute this method to initialize eqRowsNumMap
      newRow1 = adjMatrix.getRowInstance(swappableEdge1.row);
      newRow2 = adjMatrix.getRowInstance(swappableEdge2.row);
      final int numSwapPairsFactor =
          adjMatrix.getNumSwapPairsFactor(swappableEdge1.row, swappableEdge2.row);
      final int expectedAdjMatrixNumSwapPairs =
          adjMatrix.getNumSwapPairs(numSwapPairsFactor, newRow1, newRow2);

      matrix.getDegreeAndTotalNumSwapPairs(); // execute this method to initialize eqRowsNumMap
      final Edge newEdge1 = new Edge(swappableEdge1.row, swappableEdge2.col);
      final Edge newEdge2 = new Edge(swappableEdge2.row, swappableEdge1.col);
      final Vector[] newRows =
          matrix.getNewRows(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
      newRow1 = newRows[0];
      newRow2 = newRows[1];
      final int actualAdjMatrixNumSwapPairs =
          matrix.getAdjMatrixNumSwapPairs(numSwapPairsFactor, newRow1, newRow2);

      Assert.assertEquals(expectedAdjMatrixNumSwapPairs, actualAdjMatrixNumSwapPairs);
    }
  }

  @Test
  public void transition() {
    final String datasetPath = Paths.concat(Paths.datasetsDir, DatasetNames.foodmart);
    final SparseMatrix inMatrix = this.transformer.createMatrix(datasetPath);
    final RefinedMatrix matrix = new RefinedMatrix(inMatrix);
    matrix.getDegreeAndTotalNumSwapPairs(); // execute this method to initialize data structures

    for (int t = 0; t < 10; t++) {
      final RefinedMatrix adjMatrix = new RefinedMatrix(matrix.getMatrix());
      final SwappableAndNewEdges sne = adjMatrix.getSwappableAndNewEdges(this.rnd);
      final Edge swappableEdge1 = sne.swappableEdge1;
      final Edge swappableEdge2 = sne.swappableEdge2;
      final Edge newEdge1 = sne.newEdge1;
      final Edge newEdge2 = sne.newEdge2;
      adjMatrix.transition(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
      adjMatrix
          .getDegreeAndTotalNumSwapPairs(); // execute this method to initialize data structures

      final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
      final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
      final Vector[] newRows =
          matrix.getNewRows(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
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
    final HashMap<Matrix, Integer> matrixNumStatesMap = new HashMap<>();
    matrixNumStatesMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {0, 1, 1},
                  {1, 0, 0},
                  {1, 0, 0},
                })),
        3);
    matrixNumStatesMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 1, 0},
                  {1, 1, 0},
                  {0, 0, 1},
                })),
        3);
    matrixNumStatesMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 1, 1, 0},
                  {0, 0, 1, 1},
                  {0, 0, 0, 1},
                  {0, 0, 0, 1}
                })),
        8);
    matrixNumStatesMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 0, 1, 0},
                  {0, 1, 0, 1},
                })),
        3);
    matrixNumStatesMap.put(
        new RefinedMatrix(
            new SparseMatrix(
                new int[][] {
                  {1, 0, 1, 0},
                  {1, 0, 1, 0},
                  {0, 1, 0, 1},
                  {0, 1, 0, 1},
                })),
        6);
    for (Entry<Matrix, Integer> entry : matrixNumStatesMap.entrySet()) {
      final Matrix matrix = entry.getKey();
      final int numStates = entry.getValue();
      final int numSamples = numStates * 10_000;
      final int numSwaps = 2 * matrix.getSum(); // as per our experiments
      final Map<Dataset, Integer> datasetCounts = new HashMap<>();

      for (int i = 0; i < numSamples; i++) {
        final SparseMatrix sample =
            this.sampler.sample(
                matrix.getMatrix(), numSwaps, this.rnd.nextLong(), new Timer(false));
        final Dataset dataset = new Dataset(sample);
        final int count = datasetCounts.getOrDefault(dataset, 0) + 1;
        datasetCounts.put(dataset, count);
      }

      final int numDatasets = datasetCounts.size();
      Assert.assertEquals(numStates, numDatasets);

      for (Integer count : datasetCounts.values()) {
        final double expectedProb = (double) 1 / numStates;
        final double actualProb = (double) count / numSamples;
        final double delta = 0.1 * expectedProb;
        Assert.assertEquals(expectedProb, actualProb, delta);
      }
    }
  }
}
