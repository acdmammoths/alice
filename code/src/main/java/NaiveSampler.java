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

/** This class implements {@link Sampler} using the DiFfuSR-N (Naive) method. */
class NaiveSampler implements Sampler {
  /**
   * Samples a dataset from the uniform distribution of datasets with the same row and column
   * margins as the original dataset using the DiFfuSR-N method.
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

    final NaiveMatrix matrix = new NaiveMatrix(inMatrix);

    final Random rnd = new Random(seed);

    int matrixDegree = matrix.getDegree();
    double logNumEquivMatrices = matrix.getLogNumEquivMatrices();

    final long setupTime = System.currentTimeMillis() - setupTimeStart;
    timer.save(setupTime);

    for (int i = 0; i < numSwaps; i++) {
      timer.start();

      final SwappableAndNewEdges sne = matrix.getSwappableAndNewEdges(rnd);
      final Edge swappableEdge1 = sne.swappableEdge1;
      final Edge swappableEdge2 = sne.swappableEdge2;
      final Edge newEdge1 = sne.newEdge1;
      final Edge newEdge2 = sne.newEdge2;

      final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
      final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
      final Vector[] newRows =
          matrix.getNewRows(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
      final Vector newRow1 = newRows[0];
      final Vector newRow2 = newRows[1];

      final int adjMatrixDegree =
          matrix.getAdjMatrixDegree(swappableEdge1, swappableEdge2, matrixDegree);

      final double logNumEquivAdjMatrices =
          matrix.getLogNumEquivAdjMatrices(
              logNumEquivMatrices, swappableRow1, swappableRow2, newRow1, newRow2);

      final double frac =
          Math.exp(logNumEquivMatrices - logNumEquivAdjMatrices)
              * ((double) matrixDegree / adjMatrixDegree);
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
        logNumEquivMatrices = logNumEquivAdjMatrices;
      }

      timer.stop();
    }

    return matrix.getMatrix();
  }

  /**
   * Saves the natural logarithm of the number of equivalent matrices for each matrix the sampler
   * reaches and always transitions to the adjacent matrix. We always transition to the adjacent
   * matrix so that the number of data points we save is equal to the number of swaps (assuming
   * there are no hash collisions when saving the data points). This method is used specifically for
   * {@link DistortionExperiment}.
   *
   * @param inMatrix a {@link SparseMatrix} representation of the dataset
   * @param numSwaps the number of swaps to make such that the chain sufficiently mixes
   * @param seed the random seed
   * @param logNumEquivMatricesTracker a tracker for the log of the number of equivalent matrices
   */
  static void sample(
      SparseMatrix inMatrix,
      int numSwaps,
      long seed,
      LogNumEquivMatricesTracker logNumEquivMatricesTracker) {
    final NaiveMatrix matrix = new NaiveMatrix(inMatrix);

    final Random rnd = new Random(seed);

    int matrixDegree = matrix.getDegree();
    double logNumEquivMatrices = matrix.getLogNumEquivMatrices();

    for (int i = 0; i < numSwaps; i++) {
      logNumEquivMatricesTracker.save(matrix, logNumEquivMatrices);

      final SwappableAndNewEdges sne = matrix.getSwappableAndNewEdges(rnd);
      final Edge swappableEdge1 = sne.swappableEdge1;
      final Edge swappableEdge2 = sne.swappableEdge2;
      final Edge newEdge1 = sne.newEdge1;
      final Edge newEdge2 = sne.newEdge2;

      final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
      final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
      final Vector[] newRows =
          matrix.getNewRows(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
      final Vector newRow1 = newRows[0];
      final Vector newRow2 = newRows[1];

      final int adjMatrixDegree =
          matrix.getAdjMatrixDegree(swappableEdge1, swappableEdge2, matrixDegree);

      final double logNumEquivAdjMatrices =
          matrix.getLogNumEquivAdjMatrices(
              logNumEquivMatrices, swappableRow1, swappableRow2, newRow1, newRow2);

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
      logNumEquivMatrices = logNumEquivAdjMatrices;
    }
  }
}
