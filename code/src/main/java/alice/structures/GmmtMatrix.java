package alice.structures;

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
import alice.helpers.CountingWedges;
import alice.helpers.Swappables;
import java.util.Arrays;
import java.util.Random;

/**
 * This class extends {@link Matrix} and is used for the {@link GmmtSampler}.
 */
public class GmmtMatrix extends Matrix {

    /**
     * Creates an instance of {@link GmmtMatrix} from a 0-1 {@link SparseMatrix}
     * by initializing necessary data structures from the matrix.
     *
     * @param inMatrix a 0-1 matrix representation of the dataset
     */
    public GmmtMatrix(SparseMatrix inMatrix) {
        super(inMatrix);
    }
    
    /**
     * Creates an instance of {@link GmmtMatrix} from a 0-1 {@link SparseMatrix}
     * by initializing necessary data structures from the matrix.
     *
     * @param inMatrix a 0-1 matrix representation of the dataset
     * @param edges
     */
    public GmmtMatrix(SparseMatrix inMatrix, Edge[] edges) {
        super(inMatrix, edges);
    }

    /**
     * Gets the degree of the matrix in the Markov chain. Reference: Gionis et
     * al., Theorem 4.3, Equation (2).
     *
     * @return the degree of the matrix
     */
    public long getDegree() {
        final long numDisjPairsOfEdges = this.getNumDisjPairsOfEdges();
        final long numZstructs = this.getNumZstructs();
//        System.out.println("NUM Caterpillars " + numZstructs);
        final long numK22Cliques = this.getNumK22Cliques();
//        System.out.println("NUM Butterflies " + numK22Cliques);
        return numDisjPairsOfEdges - numZstructs + 2 * numK22Cliques;
    }

    /**
     * Gets the degree of the adjacent matrix in the Markov chain given the
     * current matrix. Reference: Gionis et al., proof of Corollary 4.4.
     *
     * @param swappableEdge1 the first swappable edge that leads to the adjacent
     * matrix
     * @param swappableEdge2 the second swappable edge that leads to the
     * adjacent matrix
     * @param matrixDegree the degree of the current matrix
     * @return the degree of the adjacent matrix
     */
    public long getAdjMatrixDegree(Edge swappableEdge1, Edge swappableEdge2, long matrixDegree) {
        final int changeInNumZstructs = this.getChangeInNumZstructs(swappableEdge1, swappableEdge2);
        final int changeInNumK22Cliques = this.getChangeInNumK22Cliques(swappableEdge1, swappableEdge2);
        return matrixDegree - changeInNumZstructs + 2 * changeInNumK22Cliques;
    }

    /**
     * Gets the number of disjoint pairs of edges in the graph representation of
     * the matrix. Reference: Gionis et al., Theorem 4.3, Equation (3).
     *
     * @return the number of disjoint pairs of edges
     */
    public long getNumDisjPairsOfEdges() {
        long sumOfRowSumsSqrd = Arrays.stream(this.getRowSums())
                .mapToLong(rowSum -> rowSum * rowSum)
                .sum();
        long sumOfColSumsSqrd = Arrays.stream(this.getColSums())
                .mapToLong(colSum -> colSum * colSum)
                .sum();
        final long numEdges = this.getNumEdges();
        return (numEdges * (numEdges + 1) - sumOfRowSumsSqrd - sumOfColSumsSqrd) / 2;
    }
    
    /**
     * Gets the number of disjoint pairs of edges in the graph representation of
     * the matrix. Reference: Gionis et al., Theorem 4.3, Equation (3).
     *
     * @return the number of disjoint pairs of edges
     */
    public int getNumDisjPairsOfEdges_old() {
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
     * Gets the number of Z structures in the graph representation of the
     * matrix. Reference: Gionis et al., Theorem 4.3, Equation (4).
     *
     * @return the number of Z structures
     */
    public long getNumZstructs() {
        return Arrays.stream(edges)
                .parallel()
                .mapToLong(edge -> (this.getRowSum(edge.row) - 1) * (this.getColSum(edge.col) - 1))
                .sum();
    }

    /**
     * Gets the number of K22 cliques in the graph representation of the matrix.
     * Reference: Gionis et al., Theorem 4.3, Equation (5).
     *
     * @return the number of K22 cliques
     */
    public long getNumK22Cliques() {
        return CountingWedges.countWedges(this.getRows(), getNumRows() + 1);
    }

    /**
     * Gets the change in the number of Z structures for the possible adjacent
     * graph defined by the two swappable edges. Reference: Gionis et al., proof
     * of Corollary 4.4.
     *
     * @param swappableEdge1 the first swappable edge that transitions to the
     * adjacent matrix
     * @param swappableEdge2 the second swappable edge that transitions to the
     * adjacent matrix
     * @return the change in the number of Z structures
     */
    private int getChangeInNumZstructs(Edge swappableEdge1, Edge swappableEdge2) {
        return (this.getRowSum(swappableEdge1.row) - this.getRowSum(swappableEdge2.row))
                * (this.getColSum(swappableEdge2.col) - this.getColSum(swappableEdge1.col));
    }

    /**
     * Gets the change in the number of K22 cliques for the possible adjacent
     * graph defined by the two swappable edges. Reference: Gionis et al., proof
     * of Corollary 4.4.
     *
     * @param swappableEdge1 the first swappable edge that transitions to the
     * adjacent matrix
     * @param swappableEdge2 the second swappable edge that transitions to the
     * adjacent matrix
     * @return the change in the number of K22 cliques
     */
    protected int getChangeInNumK22Cliques(Edge swappableEdge1, Edge swappableEdge2) {
        int output = 0;
        final int[] srcs = {swappableEdge1.row, swappableEdge2.row};
        final int[] dsts = {swappableEdge1.col, swappableEdge2.col};
        int[][] common = new int[this.getNumRows()][2];
        for (int i = 0; i < srcs.length; i++) {
            // for each b, stores the number of neighbors 
            // before and after the swap
            if (i != 0) {
                for (int[] common1 : common) {
                    common1[0] = 0;
                    common1[1] = 0;
                }
            }
            // neighbors of src
            Vector neighs = getRowInstance(srcs[i]);
            for (int h : neighs.getNonzeroIndices()) {
                Vector hneighs = getColInstance(h);
                for (int v : hneighs.getNonzeroIndices()) {
                    if (v == srcs[i]) {
                        continue;
                    }
                    // update num neighs before
                    common[v][0]++;
                    // update num neighs after
                    if (dsts[i] != h) {
                        common[v][1]++;
                    }
                }
            }
            Vector NewNeigh = getColInstance(dsts[(i + 1) % 2]);
            for (int v : NewNeigh.getNonzeroIndices()) {
                if (v == srcs[i] || v == srcs[(i + 1) % 2]) {
                    continue;
                }
                // update num neighs after
                common[v][1]++;
            }
            // compute binomial coefficients
            int sumDiff = 0;
            for (int[] common1 : common) {
                sumDiff += (common1[1] * (common1[1] - 1))
                        - (common1[0] * (common1[0] - 1));
            }
            sumDiff /= 2;
            output += sumDiff;
        }
        return output;
    }

    /**
     * Gets the swappable edges and new edges that will potentially be added to
     * the list of edges for the matrix for the GMMT chain. Reference: Gionis et
     * al., Algorithm 2 Find_adjacent.
     *
     * @param rnd an instance of a {@link Random} object to sample edges
     * @return an instance of {@link Swappables}
     */
    public Swappables getSwappableAndNewEdges(Random rnd) {
        Swappables sne;
        Edge edge1;
        Edge edge2;
        do {
            sne = this.sampleEdges(rnd);
            edge1 = sne.swappableEdge1;
            edge2 = sne.swappableEdge2;
        } while ((this.getVal(edge1.row, edge2.col) == 1)
                || (this.getVal(edge2.row, edge1.col) == 1));

        return sne;
    }
    
    /**
     * Checks whether two edges are swappable.
     * 
     * @param sne candidate swappables
     * @return True if the two edges are swappable; False otherwise
     */
    public boolean areSwappable(Swappables sne) {
        final Edge first = sne.swappableEdge1;
        final Edge second = sne.swappableEdge2;
        return !(first.row == second.row || first.col == second.col ||
                getRowInstance(first.row).get(second.col) == 1 || 
                getRowInstance(second.row).get(first.col) == 1);
    }
}
