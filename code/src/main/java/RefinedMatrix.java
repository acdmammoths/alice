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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** This class extends {@link GmmtMatrix} and is used for the {@link RefinedSampler}. */
class RefinedMatrix extends GmmtMatrix {
  /** A map where each key is a row sum and the value is the set of rows with that row sum. */
  private final Map<Integer, Set<Integer>> rowSumToEqRowSumRows = new HashMap<>();

  RefinedMatrix(SparseMatrix inMatrix) {
    super(inMatrix);
  }

  private Set<Integer> getEqRowSumRows(int rowSum) {
    return this.rowSumToEqRowSumRows.getOrDefault(rowSum, new HashSet<>());
  }

  private void addEqRowSumRow(int rowSum, int row) {
    final Set<Integer> rows = this.getEqRowSumRows(rowSum);
    rows.add(row);
    this.rowSumToEqRowSumRows.put(rowSum, rows);
  }

  private int getNumEntriesNeq(int row1, int row2) {
    return this.matrix.getNumEntriesNeq(row1, row2);
  }

  /**
   * Gets (1) the degree of the matrix in the Markov chain and (2) the total number of swappable
   * pairs of edges for the matrix that don't include swapping to an equivalent "adjacent" matrix.
   * We compute both quantities in one method because computing the number of K22 cliques, which is
   * needed to compute the degree, requires doing the same work as computing the number of
   * equivalent adjacent matrices, which is needed to compute the total number of swappable pairs of
   * edges. Specifically, the work that is needed in both computations is the nested for loops
   * iterating over the rows. Reference: (1) Gionis et al., Theorem 4.3, Equation (2) Theorem 5.5 in
   * the paper.
   *
   * @return the degree and total number of swappable pairs of the matrix
   */
  int[] getDegreeAndTotalNumSwapPairs() {
    final int numDisjPairsOfEdges = this.getNumDisjPairsOfEdges();
    final int numZstructs = this.getNumZstructs();

    int numK22CliquesSum = 0;

    // The number of equivalent "adjacent" matrices, where an "adjacent" matrix is defined according
    // to Gionis et al. Reference: Gionis et al., Algorithm 2 Find_adjacent.
    int numEquivAdjMatrices = 0;

    for (int i = 0; i < this.getNumRows(); i++) {
      final int rowSum = this.getRowSum(i);

      for (int k = i + 1; k < this.getNumRows(); k++) {
        final int rowProdMatrixVal = this.getRowProdMatrixVal(i, k);
        numK22CliquesSum += (rowProdMatrixVal * rowProdMatrixVal - rowProdMatrixVal);

        if (rowSum != this.getRowSum(k)) {
          continue;
        }

        // initialize rowSumToEqRowSumRows map
        this.addEqRowSumRow(rowSum, i);
        this.addEqRowSumRow(rowSum, k);

        final int numEntriesNeq = this.getNumEntriesNeq(i, k);
        if (numEntriesNeq == 2) {
          numEquivAdjMatrices++;
        }
      }

      // initialize rowToNumEqRows map
      // get instance of row instead of copy to avoid creating a ton of temporary Vector objects
      final Vector row = this.getRowInstance(i);
      this.incNumEqRows(row);
    }

    // we just add numK22CliquesSum here and don't do 2 * (numK22CliquesSum / 2) here because the
    // 2's cancel out
    final int degree = numDisjPairsOfEdges - numZstructs + numK22CliquesSum;

    final int totalNumSwapPairs = degree - numEquivAdjMatrices;

    return new int[] {degree, totalNumSwapPairs};
  }

  /**
   * Gets the total number of swappable pairs of edges for the adjacent matrix defined by the
   * swappable edges that don't include swapping to an equivalent matrix.
   *
   * @param swappableEdge1 the first swappable edge
   * @param swappableEdge2 the second swappable edge
   * @param newEdge1 the first new edge to be added
   * @param newEdge2 the second new edge to be added
   * @param totalNumSwapPairs the total number of swappable pairs for the current matrix
   * @param matrixDegree the degree of the matrix
   * @param adjMatrixDegree the degree of the adjacent matrix
   * @return the total number of swappable pairs for the adjacent matrix
   */
  int getAdjMatrixTotalNumSwapPairs(
      Edge swappableEdge1,
      Edge swappableEdge2,
      Edge newEdge1,
      Edge newEdge2,
      int totalNumSwapPairs,
      int matrixDegree,
      int adjMatrixDegree) {
    final int changeInMatrixDegree = adjMatrixDegree - matrixDegree;
    final int changeInNumEquivAdjMatrices =
        this.getChangeInNumEquivAdjMatrices(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
    return totalNumSwapPairs + changeInMatrixDegree - changeInNumEquivAdjMatrices;
  }

  /**
   * Gets the change in the number of equivalent adjacent matrices for transitioning from the
   * current matrix to the adjacent matrix defined by the swappable edges. Reference: Algorithm 1 in
   * the paper.
   *
   * @param swappableEdge1 the first swappable edge
   * @param swappableEdge2 the second swappable edge
   * @param newEdge1 the first new edge to be added
   * @param newEdge2 the second new edge to be added
   * @param updatedRowPairToNumEntriesNeqPairs an empty map that the updated row pair to number of
   *     entries not equal pairs are added to
   * @return the change in the number of equivalent adjacent matrices
   */
  private int getChangeInNumEquivAdjMatrices(
      Edge swappableEdge1, Edge swappableEdge2, Edge newEdge1, Edge newEdge2) {
    int changeInNumEquivAdjMatrices = 0;

    final List<Integer> swappableRows = Arrays.asList(swappableEdge1.row, swappableEdge2.row);
    final int[] swappableCols = {swappableEdge1.col, swappableEdge2.col};

    for (int i = 0; i < 2; i++) {
      final int swappableRow = swappableRows.get(i);
      final int rowSum = this.getRowSum(swappableRow);
      final Set<Integer> eqRowSumRows = this.getEqRowSumRows(rowSum);
      // There are cases in which "removeAll" is slow (see
      // https://codeblog.jonskeet.uk/2010/07/29/there-s-a-hole-in-my-abstraction-dear-liza-dear-liza/
      // and https://bugs.openjdk.java.net/browse/JDK-6394757, and
      // https://docs.oracle.com/en/java/javase/16/docs/api/java.base/java/util/AbstractSet.html#removeAll(java.util.Collection)).
      // While using eqRowSumRows.removeAll(swappableRows) would likely not
      // have been slow because swappableRows is a HashSet, the following
      // code may be even faster, as it doesn't need to keep track of whether
      // any element actually gets removed.
      swappableRows.forEach(eqRowSumRows::remove);

      for (int row : eqRowSumRows) {
        final int oldNumEntriesNeq = this.getNumEntriesNeq(row, swappableRow);
        if (oldNumEntriesNeq > 4) {
          continue;
        }

        int newNumEntriesNeq = oldNumEntriesNeq;
        for (int swappableCol : swappableCols) {
          if (this.getVal(row, swappableCol) == this.getVal(swappableRow, swappableCol)) {
            newNumEntriesNeq++;
          } else {
            newNumEntriesNeq--;
          }
        }

        if (newNumEntriesNeq == 2 && oldNumEntriesNeq != 2) {
          changeInNumEquivAdjMatrices++;
        } else if (newNumEntriesNeq != 2 && oldNumEntriesNeq == 2) {
          changeInNumEquivAdjMatrices--;
        }
      }

      eqRowSumRows.add(swappableRow);
      final int otherSwappableRow = swappableRows.get((i + 1) % 2);
      if (this.getRowSum(otherSwappableRow) == rowSum) {
        eqRowSumRows.add(otherSwappableRow);
      }
    }

    return changeInNumEquivAdjMatrices;
  }

  /**
   * Gets the correction factor for the number of swappable pairs of edges that transition from the
   * matrix to the adjacent matrix defined by the input rows. Note that the correction factor is the
   * same for the number of swappable pairs of edges that transition from the adjacent matrix to the
   * current matrix. Reference: Lemma 5.3 in the paper.
   *
   * @param row1 the first row index
   * @param row2 the second row index
   * @return the correction factor
   */
  int getNumSwapPairsFactor(int row1, int row2) {
    if (this.getRowSum(row1) != this.getRowSum(row2)) {
      return 1;
    }

    int asymSetDiff = 0;
    // row1 and row2 have the same length, so it doesn't matter which one we iterate over
    for (int c : this.getNonzeroIndices(row1)) {
      if (this.getVal(row2, c) == 0) {
        if (asymSetDiff == 2) {
          return 1;
        } else {
          asymSetDiff++;
        }
      }
    }

    return asymSetDiff == 2 ? 2 : 1;
  }

  /**
   * Gets the number of swappable pairs of edges that transition from the matrix to the adjacent
   * matrix defined by the swappable rows. Reference: Lemma 5.3 in the paper.
   *
   * @param numSwapPairsFactor the correction factor
   * @param swappableRow1 the row that contains the first swappable edge
   * @param swappableRow2 the row that contains the second swappable edge
   * @return the number of swappable pairs of edges that transition from the current matrix to the
   *     adjacent matrix
   */
  int getNumSwapPairs(int numSwapPairsFactor, Vector swappableRow1, Vector swappableRow2) {
    return numSwapPairsFactor * this.getNumEqRows(swappableRow1) * this.getNumEqRows(swappableRow2);
  }

  /**
   * Gets the number of swappable pairs of edges that transition from the adjacent matrix defined by
   * the new rows to the current matrix. Reference: Corollary 5.4 in the paper.
   *
   * @param numSwapPairsFactor the correction factor
   * @param newRow1 the row that contains the first new edge
   * @param newRow2 the row that contains the second new edge
   * @return the number of swappable pairs of edges that transition from the adjacent matrix to the
   *     current matrix
   */
  int getAdjMatrixNumSwapPairs(int numSwapPairsFactor, Vector newRow1, Vector newRow2) {
    return numSwapPairsFactor * (this.getNumEqRows(newRow1) + 1) * (this.getNumEqRows(newRow2) + 1);
  }

  /**
   * Determines whether all entries are equal across two rows, excluding the entries in the
   * specified skipped columns.
   *
   * @param row1 the first row
   * @param row2 the second row
   * @param skipCol1 the first column to skip
   * @param skipCol2 the second column to skip
   * @return whether the all values are equal across the two rows
   */
  private boolean entriesEqual(int row1, int row2, int skipCol1, int skipCol2) {
    return this.matrix.entriesEqual(row1, row2, skipCol1, skipCol2);
  }

  /**
   * Gets the swappable edges and new edges that will potentially be added to the list of edges for
   * the matrix for the Refined chain. Note that this method only returns edges that lead to a
   * matrix that does not represent the same dataset.
   *
   * @param rnd an instance of a {@link Random} object to sample edges
   * @return an instance of {@link SwappableAndNewEdges}
   */
  @Override
  SwappableAndNewEdges getSwappableAndNewEdges(Random rnd) {
    Edge sampledEdge1;
    Edge sampledEdge2;

    do {
      final Edge[] sampledEdges = this.sampleEdges(rnd);
      sampledEdge1 = sampledEdges[0];
      sampledEdge2 = sampledEdges[1];
    } while (((this.getVal(sampledEdge1.row, sampledEdge2.col) == 1)
            || (this.getVal(sampledEdge2.row, sampledEdge1.col) == 1))
        || this.entriesEqual(
            sampledEdge1.row, sampledEdge2.row, sampledEdge1.col, sampledEdge2.col));

    final Edge newEdge1 = new Edge(sampledEdge1.row, sampledEdge2.col);
    final Edge newEdge2 = new Edge(sampledEdge2.row, sampledEdge1.col);

    return new SwappableAndNewEdges(sampledEdge1, sampledEdge2, newEdge1, newEdge2);
  }
}
