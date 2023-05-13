package alice.structures;

/*
 * Copyright (C) 2022 Alexander Lee, Giulia Preti, and Matteo Riondato
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
import alice.helpers.Swappables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import org.apache.commons.math3.ml.distance.EarthMoversDistance;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.javatuples.Pair;

/**
 * A wrapper class around an instance of a {@link SparseMatrix} and useful data
 * structures that store the matrix's properties.
 */
public class Matrix {

    /**
     * A 0-1 matrix representation of the dataset.
     */
    private SparseMatrix matrix;

    /**
     * An array of the matrix's row sums indexed by row.
     */
    private int[] rowSums;

    /**
     * An array of the matrix's column sums indexed by column.
     */
    private int[] colSums;

    /**
     * A list of the edges from the bipartite graph representation of the
     * matrix.
     */
    public Edge[] edges;

    /**
     * Creates an instance of {@link Matrix} from a 0-1 {@link SparseMatrix} by
     * initializing necessary data structures from the matrix.
     *
     * @param inMatrix a 0-1 matrix representation of the dataset
     */
    public Matrix(SparseMatrix inMatrix) {
        this.matrix = new SparseMatrix(inMatrix.getNumRows(), inMatrix.getNumCols());
        this.rowSums = new int[inMatrix.getNumRows()];
        this.colSums = new int[inMatrix.getNumCols()];
        Set<Edge> tmpEdges = Sets.newHashSet();
        for (int r = 0; r < inMatrix.getNumRows(); r++) {
            matrix.replaceRow(r, new Vector(inMatrix.getNonzeroIndices(r)));
            rowSums[r] = inMatrix.getNumNonzeroIndices(r);
            for (int c : inMatrix.getNonzeroIndices(r)) {
                matrix.setInCol(r, c, inMatrix.isInRow(r, c));
                tmpEdges.add(new Edge(r, c));
                this.colSums[c]++;
            }
        }
        int counter = 0;
        this.edges = new Edge[tmpEdges.size()];
        for (Edge edge : tmpEdges) {
            this.edges[counter] = edge;
            counter ++;
        }
    }
    
    /**
     * Creates an instance of {@link Matrix} from a 0-1 {@link SparseMatrix} by
     * initializing necessary data structures from the matrix.
     *
     * @param inMatrix a 0-1 matrix representation of the dataset
     * @param edges edges in the graph represented by inMatrix
     */
    public Matrix(SparseMatrix inMatrix, Edge[] edges) {
        this.matrix = new SparseMatrix(inMatrix.getNumRows(), inMatrix.getNumCols());
        this.rowSums = new int[inMatrix.getNumRows()];
        this.colSums = new int[inMatrix.getNumCols()];
        for (int r = 0; r < inMatrix.getNumRows(); r++) {
            matrix.replaceRow(r, new Vector(inMatrix.getNonzeroIndices(r)));
            rowSums[r] = inMatrix.getNumNonzeroIndices(r);
            for (int c : inMatrix.getNonzeroIndices(r)) {
                matrix.setInCol(r, c, inMatrix.isInRow(r, c));
                this.colSums[c]++;
            }
        }
        this.edges = new Edge[edges.length];
        for (int i = 0; i < edges.length; i++) {
            this.edges[i] = edges[i].copy();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        Matrix otherMatrix = (Matrix) o;
        return this.matrix.equals(otherMatrix.matrix);
    }
    
    @Override
    public int hashCode() {
        return this.matrix.hashCode();
    }

    @Override
    public String toString() {
        return this.matrix.toString();
    }

    /**
     * 
     * @return this matrix
     */
    public SparseMatrix getMatrix() {
        return this.matrix;
    }

    /**
     * 
     * @param row row id
     * @param col col id
     * @return 1 if col is in row; 0 otherwise
     */
    public int getVal(int row, int col) {
        return this.matrix.isInRow(row, col);
    }
    
    /**
     * If 0, removes col from row; if 1 adds col to row.
     * @param row row id
     * @param col col id
     * @param val value (0 or 1)
     */
    public void setRow(int row, int col, int val) {
        this.matrix.setInRow(row, col, val);
    }
    
    /**
     * Replace the id-th row with this row.
     * @param id row id
     * @param row new row
     */
    public void setRow(int id, Vector row) {
        this.matrix.replaceRow(id, row);
    }
    
    /**
     * If 0, removes row from col; if 1 adds row to col.
     * @param row row id
     * @param col col id
     * @param val value (0 or 1)
     */
    public void setCol(int row, int col, int val) {
        this.matrix.setInCol(row, col, val);
    }
    
    /**
     * Replace the id-th col with this col.
     * @param id col id
     * @param col new col
     */
    public void setCol(int id, Vector col) {
        this.matrix.replaceCol(id, col);
    }

    /**
     * 
     * @param row row id
     * @return a copy of row
     */
    public Vector getRowCopy(int row) {
        return this.matrix.getRowCopy(row);
    }

    /**
     * 
     * @param row row id
     * @return reference to row
     */
    public Vector getRowInstance(int row) {
        return this.matrix.getRowInstance(row);
    }
    
    /**
     * 
     * @return array with the rows in this matrix
     */
    public Vector[] getRows() {
        return getMatrix().getRows();
    }
    
    /**
     * 
     * @return array with the cols in this matrix
     */
    public Vector[] getCols() {
        return getMatrix().getCols();
    }
    
    /**
     * 
     * @param col col id
     * @return a copy of col
     */
    public Vector getColCopy(int col) {
        return this.matrix.getColCopy(col);
    }

    /**
     * 
     * @param col col id
     * @return reference to col
     */
    public Vector getColInstance(int col) {
        return this.matrix.getColInstance(col);
    }

    /**
     * 
     * @param row row id
     * @return cols in this row
     */
    public IntOpenHashSet getNonzeroIndices(int row) {
        return this.matrix.getNonzeroIndices(row);
    }
    
    /**
     * 
     * @param col col id
     * @return rows in this col
     */
    public IntOpenHashSet getNonzeroColIndices(int col) {
        return this.matrix.getNonzeroColIndices(col);
    }

    /**
     * 
     * @return number of rows in this matrix
     */
    public int getNumRows() {
        return this.matrix.getNumRows();
    }
    
    /**
     * 
     * @return number of columns in this matrix
     */
    public int getNumCols() {
        return this.matrix.getNumCols();
    }

    /**
     * 
     * @return number of non-zero entries in each row
     */
    public int[] getRowSums() {
        return this.rowSums;
    }

    /**
     * 
     * @param row row id
     * @return number of non-zero entries in row
     */
    public int getRowSum(int row) {
        return this.rowSums[row];
    }

    /**
     * 
     * @return number of non-zero entries in each col
     */
    public int[] getColSums() {
        return this.colSums;
    }

    /**
     * \
     * @param col col id
     * @return number of non-zero entries in col
     */
    public int getColSum(int col) {
        return this.colSums[col];
    }

    /**
     * 
     * @return edges in the bipartite graph represented by this matrix
     */
    public Set<Edge> getEdgesSet() {
        return Sets.newHashSet(this.edges);
    }

    /**
     * 
     * @return number of edges in the bipartite graph represented by this matrix
     */
    public int getNumEdges() {
        return this.edges.length;
    }

    /**
     * Samples edges from the graph representation of the matrix uniformly at
     * random.
     *
     * @param rnd a {@link Random} instance
     * @return the two sampled edges as a length two array of {@link Edge}
     */
    public Swappables sampleEdges(Random rnd) {
        final int edge1Index = rnd.nextInt(this.getNumEdges());
        int edge2Index;
        do {
            edge2Index = rnd.nextInt(this.getNumEdges());
        } while (edge1Index == edge2Index);

        return new Swappables(edges[edge1Index], edges[edge2Index], edge1Index, edge2Index);
    }
    
    /**
     * Swaps 1s across two rows in the matrix.
     *
     * @param sne edges to swap
     */
    public void swapVals(Swappables sne) {
        
        this.setRow(sne.swappableEdge1.row, sne.swappableEdge1.col, 0);
        this.setCol(sne.swappableEdge1.row, sne.swappableEdge1.col, 0);
        this.setRow(sne.swappableEdge2.row, sne.swappableEdge2.col, 0);
        this.setCol(sne.swappableEdge2.row, sne.swappableEdge2.col, 0);
        this.setRow(sne.swappableEdge1.row, sne.swappableEdge2.col, 1);
        this.setCol(sne.swappableEdge1.row, sne.swappableEdge2.col, 1);
        this.setRow(sne.swappableEdge2.row, sne.swappableEdge1.col, 1);
        this.setCol(sne.swappableEdge2.row, sne.swappableEdge1.col, 1);
    }

    /**
     * Swaps the edges in the graph representation of the matrix.
     *
     * @param sne edges to swap and their current position in the edge array
     */
    public void swapEdges(Swappables sne) {
        final Edge newEdge1 = new Edge(sne.swappableEdge1.row, sne.swappableEdge2.col);
        final Edge newEdge2 = new Edge(sne.swappableEdge2.row, sne.swappableEdge1.col);
        assert(edges[sne.e1Index].equals(sne.swappableEdge1));
        assert(edges[sne.e2Index].equals(sne.swappableEdge2));
        this.edges[sne.e1Index] = newEdge1;
        this.edges[sne.e2Index] = newEdge2;
    }

    /**
     * Gets the new rows of the matrix defined by the swappable and new edges.
     *
     * @param newEdge1 the first new edge to be added
     * @param newEdge2 the second new edge to be added
     * @return the two new rows of the matrix
     */
    public Vector[] getNewRows(Edge newEdge1, Edge newEdge2) {
        final Vector newRow1 = this.getRowCopy(newEdge1.row);
        newRow1.set(newEdge2.col, 0);
        newRow1.set(newEdge1.col, 1);

        final Vector newRow2 = this.getRowCopy(newEdge2.row);
        newRow2.set(newEdge1.col, 0);
        newRow2.set(newEdge2.col, 1);

        return new Vector[]{newRow1, newRow2};
    }
    
    /**
     * Gets the new cols of the matrix defined by the swappable and new edges.
     *
     * @param newEdge1 the first new edge to be added
     * @param newEdge2 the second new edge to be added
     * @return the two new cols of the matrix
     */
    public Vector[] getNewCols(Edge newEdge1, Edge newEdge2) {
        final Vector newCol1 = this.getColCopy(newEdge1.col);
        newCol1.set(newEdge2.row, 0);
        newCol1.set(newEdge1.row, 1);

        final Vector newCol2 = this.getColCopy(newEdge2.col);
        newCol2.set(newEdge1.row, 0);
        newCol2.set(newEdge2.row, 1);
        return new Vector[]{newCol1, newCol2};
    }
    
    /**
     * Transitions to the next state in the chain by updating the current matrix
     * to the adjacent matrix.
     *
     * @param sne swappable edges that transition to the
     * adjacent matrix
     */
    public void transition(Swappables sne) {
        this.swapVals(sne);
        this.swapEdges(sne);
    }

    /**
     * 
     * @return BJDM of bipartite graph represented by this matrix
     */
    public Map<Integer, Map<Integer, Integer>> getBJDM() {
        Map<Integer, Map<Integer, Integer>> BJDM = Maps.newHashMap();
        for (Edge edge : edges) {
            Map<Integer, Integer> entry = BJDM.getOrDefault(rowSums[edge.row], Maps.newHashMap());
            entry.put(colSums[edge.col], entry.getOrDefault(colSums[edge.col], 0) + 1);
            BJDM.put(rowSums[edge.row], entry);
        }
        return BJDM;
    }
    
    /**
     * Returns the BJDM of the bipartite graph represented by the matrix, 
     * where the rows are concatenated to obtain a vector 
     * of dimension 1 x (maxRowSum * maxColSum).
     * 
     * @param normalize if the BJDM matrix should be divided by edges.size()
     * @return BJDM of the bipartite graph represented by this matrix
     */
    public double[] getBJDMVector(boolean normalize) {
        Map<Pair<Integer, Integer>, Integer> BJDM = Maps.newHashMap();
        int maxRowSum = 0;
        int maxColSum = 0;
        for (Edge edge : edges) {
            Pair<Integer, Integer> p = new Pair<>(rowSums[edge.row], colSums[edge.col]);
            BJDM.put(p, BJDM.getOrDefault(p, 0) + 1);
            maxRowSum = Math.max(maxRowSum, rowSums[edge.row]);
            maxColSum = Math.max(maxColSum, colSums[edge.col]);
        }
        List<Entry<Pair<Integer, Integer>, Integer>> entries = Lists.newArrayList(BJDM.entrySet());
        Collections.sort(entries, 
                (Entry<Pair<Integer, Integer>, Integer> o1, Entry<Pair<Integer, Integer>, Integer> o2) -> {
                    int compF = o1.getKey().getValue0().compareTo(o2.getKey().getValue0());
                    if (compF != 0) {
                        return compF;
                    }
                    return o1.getKey().getValue1().compareTo(o2.getKey().getValue1());
                });
        double[][] M = new double[maxRowSum][maxColSum];
        IntStream.range(0, entries.size()).forEach(i -> {
            int r = entries.get(i).getKey().getValue0();
            int c = entries.get(i).getKey().getValue1();
            
            M[r-1][c-1] = entries.get(i).getValue();
            if (normalize) {
                M[r-1][c-1] /= 1. * edges.length;
            }
        });
        double[] bjdmV = new double[maxRowSum * maxColSum];
        for (int i = 0; i < maxRowSum; i++) {
            System.arraycopy(M[i], 0, bjdmV, i * maxColSum, maxColSum);
        }
        return bjdmV;
    }
    
    /**
     * 
     * @param otherBJDM other BJDM vector
     * @param normalize whether the BJDM vector should be normalized
     * @return earth's mover distance between the BJDM of this matrix and the other
     */
    public double getDistanceFrom(double[] otherBJDM, boolean normalize) {
        double[] thisBJDM = getBJDMVector(normalize);
        EarthMoversDistance emd = new EarthMoversDistance();
        return emd.compute(thisBJDM, otherBJDM);
    }
    
    /**
     * 
     * @return number of k22 cliques in the bipartite graph represented by this matrix
     */
    public long getNumButterflies() {
        Vector[] vec = matrix.getCols();
        if (matrix.getNumRows() < matrix.getNumCols()) {
            vec = matrix.getRows();
        }
        long butt = 0;
        for (int i = 0; i < vec.length; i++) {
            for (int j = i + 1; j < vec.length; j++) {
                int inter = (int) vec[i].interSize(vec[j]);
                if (inter > 1) {
                    butt += CombinatoricsUtils.binomialCoefficient(inter, 2);   
                } 
            }
        }
        return butt;
    }
    
    /**
     * 
     * @param rnd a {@link Random} instance
     * @return a pair of edges extracted uniformly at random from the set of edges of the bipartite graph
     */
    public Swappables getRandomSwappables(Random rnd) {
        final int e1Index = rnd.nextInt(edges.length);
        final int e2Index = rnd.nextInt(edges.length);
        return new Swappables(edges[e1Index], edges[e2Index], e1Index, e2Index);
    }
    
}
