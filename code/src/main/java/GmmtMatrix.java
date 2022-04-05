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

/** This class extends {@link Matrix} and is used for the {@link GmmtSampler}. */
class GmmtMatrix extends Matrix {
  GmmtMatrix() {}

  /**
   * Creates an instance of {@link GmmtMatrix} from a 0-1 {@link SparseMatrix} by initializing
   * necessary data structures from the matrix.
   *
   * @param inMatrix a 0-1 matrix representation of the dataset
   */
  GmmtMatrix(SparseMatrix inMatrix) {
    super(inMatrix);
  }

  int getRowProdMatrixVal(int row, int col) {
    return this.matrix.getRowDotProd(row, col);
  }

  /**
   * Gets the degree of the matrix in the Markov chain. Reference: Gionis et al., Theorem 4.3,
   * Equation (2).
   *
   * @return the degree of the matrix
   */
  int getDegree() {
    final int numDisjPairsOfEdges = this.getNumDisjPairsOfEdges();
    final int numZstructs = this.getNumZstructs();
    final int numK22Cliques = this.getNumK22Cliques();
    return numDisjPairsOfEdges - numZstructs + 2 * numK22Cliques;
  }

  /**
   * Gets the degree of the adjacent matrix in the Markov chain given the current matrix. Reference:
   * Gionis et al., proof of Corollary 4.4.
   *
   * @param swappableEdge1 the first swappable edge that leads to the adjacent matrix
   * @param swappableEdge2 the second swappable edge that leads to the adjacent matrix
   * @param matrixDegree the degree of the current matrix
   * @return the degree of the adjacent matrix
   */
  int getAdjMatrixDegree(Edge swappableEdge1, Edge swappableEdge2, int matrixDegree) {
    final int changeInNumZstructs = this.getChangeInNumZstructs(swappableEdge1, swappableEdge2);
    final int changeInNumK22Cliques = this.getChangeInNumK22Cliques(swappableEdge1, swappableEdge2);
    return matrixDegree - changeInNumZstructs + 2 * changeInNumK22Cliques;
  }

  /**
   * Gets the number of disjoint pairs of edges in the graph representation of the matrix.
   * Reference: Gionis et al., Theorem 4.3, Equation (3).
   *
   * @return the number of disjoint pairs of edges
   */
  int getNumDisjPairsOfEdges() {
    int sumOfRowSumsSqrd = 0;
    int sumOfColSumsSqrd = 0;
    for (int rowSum : this.getRowSums()) {
      sumOfRowSumsSqrd += (rowSum * rowSum);
    }
    for (int colSum : this.getColSums()) {
      sumOfColSumsSqrd += (colSum * colSum);
    }
    final int numEdges = this.getNumEdges();
    return (numEdges * (numEdges + 1) - sumOfRowSumsSqrd - sumOfColSumsSqrd) / 2;
  }

  /**
   * Gets the number of Z structures in the graph representation of the matrix. Reference: Gionis et
   * al., Theorem 4.3, Equation (4).
   *
   * @return the number of Z structures
   */
  int getNumZstructs() {
    int numZstructs = 0;
    for (Edge edge : this.edges) {
      final int rowSum = this.getRowSum(edge.row);
      final int colSum = this.getColSum(edge.col);
      numZstructs += ((rowSum - 1) * (colSum - 1));
    }
    return numZstructs;
  }

  /**
   * Gets the number of K22 cliques in the graph representation of the matrix. Reference: Gionis et
   * al., Theorem 4.3, Equation (5).
   *
   * @return the number of K22 cliques
   */
  private int getNumK22Cliques() {
    int sum = 0;
    for (int i = 0; i < this.getNumRows(); i++) {
      for (int k = i + 1; k < this.getNumRows(); k++) {
        final int val = this.getRowProdMatrixVal(i, k);
        sum += (val * val - val);
      }
    }
    return sum / 2;
  }

  /**
   * Gets the change in the number of Z structures for the possible adjacent graph defined by the
   * two swappable edges. Reference: Gionis et al., proof of Corollary 4.4.
   *
   * @param swappableEdge1 the first swappable edge that transitions to the adjacent matrix
   * @param swappableEdge2 the second swappable edge that transitions to the adjacent matrix
   * @return the change in the number of Z structures
   */
  private int getChangeInNumZstructs(Edge swappableEdge1, Edge swappableEdge2) {
    return (this.getRowSum(swappableEdge1.row) - this.getRowSum(swappableEdge2.row))
        * (this.getColSum(swappableEdge2.col) - this.getColSum(swappableEdge1.col));
  }

  /**
   * Gets the change in the number of K22 cliques for the possible adjacent graph defined by the two
   * swappable edges. Reference: Gionis et al., proof of Corollary 4.4.
   *
   * @param swappableEdge1 the first swappable edge that transitions to the adjacent matrix
   * @param swappableEdge2 the second swappable edge that transitions to the adjacent matrix
   * @return the change in the number of K22 cliques
   */
  private int getChangeInNumK22Cliques(Edge swappableEdge1, Edge swappableEdge2) {
    int sum = 0;
    final int[] swappableRows = {swappableEdge1.row, swappableEdge2.row};
    for (int swappableRow : swappableRows) {
      for (int row = 0; row < swappableRow; row++) {
        sum += this.sqrdDiff(row, swappableRow, swappableEdge1, swappableEdge2);
      }
      for (int row = swappableRow + 1; row < this.getNumRows(); row++) {
        sum += this.sqrdDiff(swappableRow, row, swappableEdge1, swappableEdge2);
      }
    }
    return sum / 2;
  }

  /**
   * A helper function to help compute the change in the number of K22 cliques. Its returned value
   * does not have much meaning.
   *
   * @param rowProdMatrixRow the row index of the row product matrix
   * @param rowProdMatrixCol the column index of the row product matrix
   * @param swappableEdge1 the first swappable edge that transitions to the adjacent matrix
   * @param swappableEdge2 the second swappable edge that transitions to the adjacent matrix
   * @return refer to the return statement
   */
  private int sqrdDiff(
      int rowProdMatrixRow, int rowProdMatrixCol, Edge swappableEdge1, Edge swappableEdge2) {
    final int rowProdMatrixVal = this.getRowProdMatrixVal(rowProdMatrixRow, rowProdMatrixCol);
    final int rowProdMatrixValSqrd = rowProdMatrixVal * rowProdMatrixVal;

    final int rowProdAdjMatrixVal =
        this.getRowProdAdjMatrixVal(
            rowProdMatrixRow, rowProdMatrixCol, swappableEdge1, swappableEdge2, rowProdMatrixVal);
    final int rowProdAdjMatrixValSqrd = rowProdAdjMatrixVal * rowProdAdjMatrixVal;

    return (rowProdAdjMatrixValSqrd - rowProdMatrixValSqrd)
        - (rowProdAdjMatrixVal - rowProdMatrixVal);
  }

  /**
   * Gets the value in (rowProdMatrixRow, rowProdMatrixCol) of the adjacent matrix's row product
   * matrix, where the adjacent matrix is defined by the two swappable pairs. Reference: Gionis et
   * al., proof of Corollary 4.4.
   *
   * @param rowProdMatrixRow the row index of the row product matrix
   * @param rowProdMatrixCol the column index of the row product matrix
   * @param swappableEdge1 the first swappable edge that transitions to the adjacent matrix
   * @param swappableEdge2 the second swappable edge that transitions to the adjacent matrix
   * @param rowProdMatrixVal the row product matrix value in (rowProdMatrixRow, rowProdMatrixCol) of
   *     the current matrix
   * @return the value in (rowProdMatrixRow, rowProdMatrixCol) of the adjacent matrix's row product
   *     matrix
   */
  private int getRowProdAdjMatrixVal(
      int rowProdMatrixRow,
      int rowProdMatrixCol,
      Edge swappableEdge1,
      Edge swappableEdge2,
      int rowProdMatrixVal) {
    if (rowProdMatrixRow >= rowProdMatrixCol) {
      throw new IllegalArgumentException(
          "Row argument was not less than column argument. This would cause unexpected behavior.");
    }
    if ((rowProdMatrixRow != swappableEdge1.row)
        && (rowProdMatrixRow != swappableEdge2.row)
        && (rowProdMatrixCol != swappableEdge1.row)
        && (rowProdMatrixCol != swappableEdge2.row)) {
      throw new IllegalArgumentException("No row arguments are equal to any of the swapped rows.");
    }

    if ((rowProdMatrixRow == swappableEdge1.row && rowProdMatrixCol == swappableEdge2.row)
        || (rowProdMatrixRow == swappableEdge2.row && rowProdMatrixCol == swappableEdge1.row)) {
      return rowProdMatrixVal;
    }
    if (rowProdMatrixRow == swappableEdge1.row) {
      return rowProdMatrixVal
          - this.getVal(rowProdMatrixCol, swappableEdge1.col)
          + this.getVal(rowProdMatrixCol, swappableEdge2.col);
    } else if (rowProdMatrixRow == swappableEdge2.row) {
      return rowProdMatrixVal
          - this.getVal(rowProdMatrixCol, swappableEdge2.col)
          + this.getVal(rowProdMatrixCol, swappableEdge1.col);
    } else if (rowProdMatrixCol == swappableEdge1.row) {
      return rowProdMatrixVal
          - this.getVal(rowProdMatrixRow, swappableEdge1.col)
          + this.getVal(rowProdMatrixRow, swappableEdge2.col);
    } else {
      return rowProdMatrixVal
          - this.getVal(rowProdMatrixRow, swappableEdge2.col)
          + this.getVal(rowProdMatrixRow, swappableEdge1.col);
    }
  }

  /**
   * Gets the swappable edges and new edges that will potentially be added to the list of edges for
   * the matrix for the GMMT chain. Reference: Gionis et al., Algorithm 2 Find_adjacent.
   *
   * @param rnd an instance of a {@link Random} object to sample edges
   * @return an instance of {@link SwappableAndNewEdges}
   */
  SwappableAndNewEdges getSwappableAndNewEdges(Random rnd) {
    Edge sampledEdge1;
    Edge sampledEdge2;

    do {
      final Edge[] sampledEdges = this.sampleEdges(rnd);
      sampledEdge1 = sampledEdges[0];
      sampledEdge2 = sampledEdges[1];
    } while ((this.getVal(sampledEdge1.row, sampledEdge2.col) == 1)
        || (this.getVal(sampledEdge2.row, sampledEdge1.col) == 1));

    final Edge newEdge1 = new Edge(sampledEdge1.row, sampledEdge2.col);
    final Edge newEdge2 = new Edge(sampledEdge2.row, sampledEdge1.col);

    return new SwappableAndNewEdges(sampledEdge1, sampledEdge2, newEdge1, newEdge2);
  }
}
