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
import java.util.Random;

/** This class implements {@link Sampler} using the DiFfuSR-R (Refined) method. */
class RefinedSampler implements Sampler {
  /**
   * Samples a dataset from the uniform distribution of datasets with the same row and column
   * margins as the original dataset using the DiFfuSR-R method.
   *
   * @param inMatrix a {@link SparseMatrix} representation of the dataset
   * @param numSwaps the number of swaps to make such that the chain sufficiently mixes
   * @param seed the random seed
   * @param timer a timer
   * @return the matrix representation of the sampled dataset
   */
  @Override
  public SparseMatrix sample(SparseMatrix inMatrix, int numSwaps, long seed, Timer timer) {
    final long setupTimeStart = System.currentTimeMillis();

    final RefinedMatrix matrix = new RefinedMatrix(inMatrix);

    final Random rnd = new Random(seed);

    final int[] degreeAndTotalNumSwapPairs = matrix.getDegreeAndTotalNumSwapPairs();
    int matrixDegree = degreeAndTotalNumSwapPairs[0];
    int matrixTotalNumSwapPairs = degreeAndTotalNumSwapPairs[1];

    final long setupTime = System.currentTimeMillis() - setupTimeStart;
    timer.save(setupTime);

    for (int i = 0; i < numSwaps; i++) {
      timer.start();

      final SwappableAndNewEdges sne = matrix.getSwappableAndNewEdges(rnd);
      final Edge swappableEdge1 = sne.swappableEdge1;
      final Edge swappableEdge2 = sne.swappableEdge2;
      final Edge newEdge1 = sne.newEdge1;
      final Edge newEdge2 = sne.newEdge2;

      final int adjMatrixDegree =
          matrix.getAdjMatrixDegree(swappableEdge1, swappableEdge2, matrixDegree);

      final int adjMatrixTotalNumSwapPairs =
          matrix.getAdjMatrixTotalNumSwapPairs(
              swappableEdge1,
              swappableEdge2,
              newEdge1,
              newEdge2,
              matrixTotalNumSwapPairs,
              matrixDegree,
              adjMatrixDegree);

      final int rowIndex1 = swappableEdge1.row;
      int rowIndex2 = swappableEdge2.row;
      final Vector swappableRow1 = matrix.getRowInstance(rowIndex1);
      final Vector swappableRow2 = matrix.getRowInstance(rowIndex2);
      final Vector[] newRows =
          matrix.getNewRows(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
      final Vector newRow1 = newRows[0];
      final Vector newRow2 = newRows[1];

      final int numSwapPairsFactor = matrix.getNumSwapPairsFactor(rowIndex1, rowIndex2);
      final int matrixNumSwapPairs =
          matrix.getNumSwapPairs(numSwapPairsFactor, swappableRow1, swappableRow2);
      final int adjMatrixNumSwapPairs =
          matrix.getAdjMatrixNumSwapPairs(numSwapPairsFactor, newRow1, newRow2);

      // compute as the product of the two ratios to avoid potential overflow
      final double frac =
          ((double) adjMatrixNumSwapPairs / matrixNumSwapPairs)
              * ((double) matrixTotalNumSwapPairs / adjMatrixTotalNumSwapPairs);
      final double acceptanceProb = Math.min(1, frac);
      if (rnd.nextDouble() <= acceptanceProb) {
        matrix.transition(
            swappableEdge1,
            swappableEdge2,
            newEdge1,
            newEdge2,
            swappableRow1,
            swappableRow2,
            newRow1,
            newRow2);

        matrixDegree = adjMatrixDegree;
        matrixTotalNumSwapPairs = adjMatrixTotalNumSwapPairs;
      }

      timer.stop();
    }

    return matrix.getMatrix();
  }
}
