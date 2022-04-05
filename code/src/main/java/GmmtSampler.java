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

/** This class implements {@link Sampler} using the Metropolis-Hastings method by Gionis et al. */
class GmmtSampler implements Sampler {
  /**
   * Samples a matrix from the uniform distribution of matrices with the same row and column margins
   * as the original matrix by using the Metropolis-Hastings method by Gionis et al. Reference:
   * Gionis et al., Algorithm 4 Metropolis-Hastings.
   *
   * @param inMatrix a {@link SparseMatrix} representation of the dataset
   * @param numSwaps the number of swaps to make such that the chain sufficiently mixes
   * @param seed the random seed
   * @param timer a timer
   * @return the sampled matrix
   */
  @Override
  public SparseMatrix sample(SparseMatrix inMatrix, int numSwaps, long seed, Timer timer) {
    final long setupTimeStart = System.currentTimeMillis();

    final GmmtMatrix matrix = new GmmtMatrix(inMatrix);

    final Random rnd = new Random(seed);

    int matrixDegree = matrix.getDegree();

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

      final double acceptanceProb = Math.min(1, (double) matrixDegree / adjMatrixDegree);

      if (rnd.nextDouble() <= acceptanceProb) {
        matrix.transition(swappableEdge1, swappableEdge2, newEdge1, newEdge2);

        matrixDegree = adjMatrixDegree;
      }

      timer.stop();
    }

    return matrix.getMatrix();
  }
}
