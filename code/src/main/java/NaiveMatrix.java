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
import java.util.Set;

/** This class extends {@link GmmtMatrix} and is used for the {@link NaiveSampler}. */
class NaiveMatrix extends GmmtMatrix {
  /** A map where each key is a row sum and the value is the number of rows with that row sum. */
  private final Map<Integer, Integer> rowSumToNumEqRowSumRows = new HashMap<>();

  /**
   * A map where each key is a row sum and the value is the unique set of rows with that row sum.
   */
  private final Map<Integer, Set<Vector>> rowSumToUniqueRows = new HashMap<>();

  /** A set of unique row sums. */
  private final Set<Integer> uniqueRowSums = new HashSet<>();

  /**
   * Creates an instance of {@link NaiveMatrix} from a 0-1 {@link SparseMatrix} by initializing necessary
   * data structures from the matrix.
   *
   * @param inMatrix a 0-1 matrix representation of the dataset
   */
  NaiveMatrix(SparseMatrix inMatrix) {
    this.matrix = new SparseMatrix(inMatrix.getNumRows(), inMatrix.getNumCols());
    this.rowSums = new int[inMatrix.getNumRows()];
    this.colSums = new int[inMatrix.getNumCols()];
    for (int r = 0; r < inMatrix.getNumRows(); r++) {
      for (int c : inMatrix.getNonzeroIndices(r)) {
        this.setVal(r, c, inMatrix.get(r, c));
        this.edges.add(new Edge(r, c));
        this.rowSums[r]++;
        this.colSums[c]++;
      }
      final Vector row = this.getRowInstance(r);
      final int rowSum = this.getRowSum(r);
      this.incNumEqRows(row);
      this.incNumEqRowSumRows(rowSum);
      this.addEqRowSumUniqueRow(rowSum, row);
      this.addUniqueRowSum(rowSum);
    }
  }

  private int getNumEqRowSumRows(int rowSum) {
    return this.rowSumToNumEqRowSumRows.getOrDefault(rowSum, 0);
  }

  private void incNumEqRowSumRows(int rowSum) {
    int numEqRowSumRows = this.getNumEqRowSumRows(rowSum);
    this.rowSumToNumEqRowSumRows.put(rowSum, numEqRowSumRows + 1);
  }

  private Set<Vector> getEqRowSumUniqueRows(int rowSum) {
    return this.rowSumToUniqueRows.getOrDefault(rowSum, new HashSet<>());
  }

  private void addEqRowSumUniqueRow(int rowSum, Vector row) {
    final Set<Vector> uniqueRows = this.getEqRowSumUniqueRows(rowSum);
    if (uniqueRows.contains(row)) {
      return;
    }
    row = row.copy();
    uniqueRows.add(row);
    this.rowSumToUniqueRows.put(rowSum, uniqueRows);
  }

  private void addUniqueRowSum(int rowSum) {
    this.uniqueRowSums.add(rowSum);
  }

  /**
   * Gets the log of the number of matrices in the chain that are equivalent to the current matrix
   * (i.e., matrices that represent the same dataset as the current matrix). Reference: Lemma 5.1 in
   * the paper.
   *
   * @return the log of the number of equivalent matrices
   */
  double getLogNumEquivMatrices() {
    double logNumEquivMatrices = 0;
    for (int rowSum : this.uniqueRowSums) {
      logNumEquivMatrices += this.getLogNumDistinctTransacOrderings(rowSum);
    }
    return logNumEquivMatrices;
  }

  /**
   * Gets the log of the number of distinct transaction orderings for the input row sum (transaction
   * length). Reference: Lemma 5.1 in the paper.
   *
   * @param rowSum the input row sum (transaction length)
   * @return the log of the number of distinct transaction orderings for the row sum
   */
  private double getLogNumDistinctTransacOrderings(int rowSum) {
    final Set<Vector> uniqueRows = this.getEqRowSumUniqueRows(rowSum);
    double logNumDistinctTransacOrderings = this.getLogNumEqRowSumRowsFac(rowSum);
    for (Vector row : uniqueRows) {
      logNumDistinctTransacOrderings -= this.getLogNumEqRowsFac(row);
    }
    return logNumDistinctTransacOrderings;
  }

  /**
   * Gets the log of the number of rows that have an equal row sum as the input row sum. Reference:
   * Lemma 5.1 in the paper.
   *
   * @param rowSum the input row sum
   * @return the log of the number of rows that have an equal row sum
   */
  private double getLogNumEqRowSumRowsFac(int rowSum) {
    double logNumEqRowSumRowsFac = 0;
    final int numEqRowSumRows = this.getNumEqRowSumRows(rowSum);
    for (int i = 0; i < numEqRowSumRows; i++) {
      logNumEqRowSumRowsFac += Math.log(numEqRowSumRows - i);
    }
    return logNumEqRowSumRowsFac;
  }

  /**
   * Gets the log of the number rows that are equal to the input row factorial. Reference: Lemma 5.1
   * in the paper.
   *
   * @param row the input row
   * @return the log of the number of rows that are equal to the given row factorial
   */
  private double getLogNumEqRowsFac(Vector row) {
    double logNumEqRowsFac = 0;
    final int numEqRows = this.getNumEqRows(row);
    for (int i = 0; i < numEqRows; i++) {
      logNumEqRowsFac += Math.log(numEqRows - i);
    }
    return logNumEqRowsFac;
  }

  /**
   * Gets the log of the number of matrices in the chain that are equivalent to the adjacent matrix
   * (i.e., matrices that represent the same dataset as the adjacent matrix). Reference: Section A.2
   * in the paper.
   *
   * @param logNumEquivMatrices the log of the number of equivalent matrices for the current matrix
   * @param swappableRow1 the first swappable row
   * @param swappableRow2 the second swappable row
   * @param newRow1 the first new row
   * @param newRow2 the second new row
   * @return the log of the number of equivalent adjacent matrices
   */
  double getLogNumEquivAdjMatrices(
      double logNumEquivMatrices,
      Vector swappableRow1,
      Vector swappableRow2,
      Vector newRow1,
      Vector newRow2) {
    // check if swap leads to a non-equivalent matrix
    // note: swappableRow1.equals(newRow2) iff swappableRow2.equals(newRow1)
    // (see notes/22-01-26-TransactionEqualsNewTransactionImplication.pdf)
    if (swappableRow1.equals(newRow2)) {
      return logNumEquivMatrices;
    }
    return logNumEquivMatrices
        + Math.log(this.getNumEqRows(swappableRow1))
        + Math.log(this.getNumEqRows(swappableRow2))
        - Math.log1p(this.getNumEqRows(newRow1))
        - Math.log1p(this.getNumEqRows(newRow2));
  }
}
