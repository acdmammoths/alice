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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * A class to track the log of the number of equivalent matrices for each matrix/state in the GMMT
 * chain. This class is used for {@link DistortionExperiment}.
 */
class LogNumEquivMatricesTracker {
  /**
   * A map where each key is the log of the number of equivalent matrices and the value is the set
   * of hash codes for the matrices with the same log of the number of equivalent matrices. It is
   * possible for two different matrices to have the same hash code, but this doesn't happen in our
   * experiments.
   */
  private final Map<Double, Set<Integer>> logNumEquivMatricesToMatrixHashes = new HashMap<>();

  /**
   * An object to compute the quartiles for the distribution of the log of the number of equivalent
   * matrices.
   */
  private final DescriptiveStatistics logNumEquivMatricesStats = new DescriptiveStatistics();

  /**
   * The number of times we call the save method, which can be different from the number of samples.
   */
  int saveCount = 0;

  /**
   * Saves the value for the log of the number of equivalent matrices for the input matrix.
   *
   * @param matrix the input matrix
   * @param logNumEquivMatrices the log of the number of equivalent matrices
   */
  void save(Matrix matrix, double logNumEquivMatrices) {
    int matrixHash = matrix.hashCode();
    Set<Integer> matrixHashes =
        this.logNumEquivMatricesToMatrixHashes.getOrDefault(logNumEquivMatrices, new HashSet<>());
    matrixHashes.add(matrixHash);
    this.logNumEquivMatricesToMatrixHashes.put(logNumEquivMatrices, matrixHashes);
    this.saveCount++;
  }

  /**
   * Populates logNumEquivMatricesStats with all the saved values for the log of the number of
   * equivalent matrices.
   */
  void initStats() {
    for (Entry<Double, Set<Integer>> entry : logNumEquivMatricesToMatrixHashes.entrySet()) {
      double logNumEquivMatrices = entry.getKey();
      Set<Integer> matrixHashes = entry.getValue();
      for (int i = 0; i < matrixHashes.size(); i++) {
        this.logNumEquivMatricesStats.addValue(logNumEquivMatrices);
      }
    }
  }

  double getMin() {
    return this.logNumEquivMatricesStats.getMin();
  }

  double getMax() {
    return this.logNumEquivMatricesStats.getMax();
  }

  double getPercentile(double percentile) {
    return this.logNumEquivMatricesStats.getPercentile(percentile);
  }

  long getNumSamples() {
    return this.logNumEquivMatricesStats.getN();
  }
}
