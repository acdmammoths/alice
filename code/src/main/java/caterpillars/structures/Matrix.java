package caterpillars.structures;

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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import org.apache.commons.math3.ml.distance.EarthMoversDistance;
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
    int[] rowSums;

    /**
     * An array of the matrix's column sums indexed by column.
     */
    int[] colSums;

    /**
     * A list of the edges from the bipartite graph representation of the
     * matrix.
     */
    public final Set<Edge> edges;

    /**
     * A map where each key is a row and the value is the number of rows equal
     * to that row.
     */
    private final Map<Vector, Integer> rowToNumEqRows;
    
    /**
     * Creates an instance of {@link Matrix} from a 0-1 {@link SparseMatrix} by
     * initializing necessary data structures from the matrix.
     *
     * @param inMatrix a 0-1 matrix representation of the dataset
     */
    public Matrix(SparseMatrix inMatrix) {
        this.edges = Sets.newHashSet();
        this.rowToNumEqRows = Maps.newHashMap();
        this.matrix = new SparseMatrix(inMatrix.getNumRows(), inMatrix.getNumCols());
        this.rowSums = new int[inMatrix.getNumRows()];
        this.colSums = new int[inMatrix.getNumCols()];
        for (int r = 0; r < inMatrix.getNumRows(); r++) {
            for (int c : inMatrix.getNonzeroIndices(r)) {
                setRow(r, c, inMatrix.isInRow(r, c));
                setCol(r, c, inMatrix.isInRow(r, c));
                this.edges.add(new Edge(r, c));
                this.rowSums[r]++;
                this.colSums[c]++;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null) {
            return false;
        } else if (this.getClass() != o.getClass()) {
            return false;
        } else {
            Matrix otherMatrix = (Matrix) o;
            return Objects.equals(this.matrix, otherMatrix.matrix);
        }
    }
    
    @Override
    public int hashCode() {
        return this.matrix.hashCode();
    }

    @Override
    public String toString() {
        return this.matrix.toString();
    }

    public SparseMatrix getMatrix() {
        return this.matrix;
    }

    public int getVal(int row, int col) {
        return this.matrix.isInRow(row, col);
    }
    
    public void setRow(int row, int col, int val) {
        this.matrix.setInRow(row, col, val);
    }
    
    public void setRow(int id, Vector row) {
        this.matrix.replaceRow(id, row);
    }
    
    public void setCol(int row, int col, int val) {
        this.matrix.setInCol(row, col, val);
    }
    
    public void setCol(int id, Vector col) {
        this.matrix.replaceCol(id, col);
    }

    public Vector getRowCopy(int row) {
        return this.matrix.getRowCopy(row);
    }

    public Vector getRowInstance(int row) {
        return this.matrix.getRowInstance(row);
    }
    
    public List<Vector> getRows() {
        return getMatrix().getRows();
    }
    
    public List<Vector> getCols() {
        return getMatrix().getCols();
    }
    
    public Vector getColCopy(int col) {
        return this.matrix.getColCopy(col);
    }

    public Vector getColInstance(int col) {
        return this.matrix.getColInstance(col);
    }

    public Set<Integer> getNonzeroIndices(int row) {
        return this.matrix.getNonzeroIndices(row);
    }
    
    public Set<Integer> getNonzeroColIndices(int col) {
        return this.matrix.getNonzeroColIndices(col);
    }

    public int getNumRows() {
        return this.matrix.getNumRows();
    }

    public int[] getRowSums() {
        return this.rowSums;
    }

    public int getRowSum(int row) {
        return this.rowSums[row];
    }

    public int[] getColSums() {
        return this.colSums;
    }

    public int getColSum(int col) {
        return this.colSums[col];
    }

    public Set<Edge> getEdgesSet() {
        return new HashSet<>(this.edges);
    }

    public int getNumEdges() {
        return this.edges.size();
    }

    /**
     * Samples edges from the graph representation of the matrix uniformly at
     * random.
     *
     * @param rnd a {@link Random} instance
     * @return the two sampled edges as a length two array of {@link Edge}
     */
    public Edge[] sampleEdges(Random rnd) {
        List<Edge> edgeList = Lists.newArrayList(edges);
        final int edge1Index = rnd.nextInt(this.getNumEdges());
        int edge2Index;

        do {
            edge2Index = rnd.nextInt(this.getNumEdges());
        } while (edge1Index == edge2Index);

        final Edge edge1 = edgeList.get(edge1Index);
        final Edge edge2 = edgeList.get(edge2Index);

        return new Edge[]{edge1, edge2};
    }
    
    /**
     * Swaps 1s across two rows in the matrix.
     *
     * @param swappableEdge1 the first swappable edge
     * @param swappableEdge2 the second swappable edge
     * @param newEdge1 the first new edge to be added
     * @param newEdge2 the second new edge to be added
     */
    public void swapVals(Edge swappableEdge1, Edge swappableEdge2, Edge newEdge1, Edge newEdge2) {
        this.setRow(swappableEdge1.row, swappableEdge1.col, 0);
        this.setCol(swappableEdge1.row, swappableEdge1.col, 0);
        this.setRow(swappableEdge2.row, swappableEdge2.col, 0);
        this.setCol(swappableEdge2.row, swappableEdge2.col, 0);
        this.setRow(newEdge1.row, newEdge1.col, 1);
        this.setCol(newEdge1.row, newEdge1.col, 1);
        this.setRow(newEdge2.row, newEdge2.col, 1);
        this.setCol(newEdge2.row, newEdge2.col, 1);
    }

    /**
     * Swaps the edges in the graph representation of the matrix.
     *
     * @param swappableEdge1 the first swappable edge
     * @param swappableEdge2 the second swappable edge
     * @param newEdge1 the first new edge to be added
     * @param newEdge2 the second new edge to be added
     */
    public void swapEdges(Edge swappableEdge1, Edge swappableEdge2, Edge newEdge1, Edge newEdge2) {
        this.edges.remove(swappableEdge1);
        this.edges.remove(swappableEdge2);
        this.edges.add(newEdge1);
        this.edges.add(newEdge2);
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
    
    public Map<Vector, Integer> getRowToNumEqRowsMap() {
        return this.rowToNumEqRows;
    }
    
    public int getNumEqRows(Vector row) {
        return rowToNumEqRows.getOrDefault(row, 0);
    }
    
    public boolean setNumEqRows(Vector row, int eqRowsNum) {
        if (eqRowsNum == 0) {
            removeNumEqRows(row);
            return false;
        }
        if (!rowToNumEqRows.containsKey(row)) {
            rowToNumEqRows.put(row.copy(), eqRowsNum);
            return true;
        }
        rowToNumEqRows.put(row, eqRowsNum);
        return false;
    }
    
    public void removeNumEqRows(Vector row) {
        rowToNumEqRows.remove(row);
    }

    /**
     * Increment the number of equal rows for the input row by 1.
     * 
     * @param row the input row
     * @return true if the row was not aleady present in the map of equal rows;
     * false otherwise
     */
    public boolean incNumEqRows(Vector row) {
        return setNumEqRows(row, getNumEqRows(row) + 1);
    }

    /**
     * Decrement the number of equal rows for the input row by 1.
     *
     * @param row the input row
     * @throws IllegalArgumentException if the current number of equal rows is
     * not greater than 0
     */
    public void decNumEqRows(Vector row) {
        final int num = getNumEqRows(row);
        if (num <= 0) {
            throw new IllegalArgumentException(
                    "The number of rows equal to row " + row + " is " + num + ", which is non positive.");
        }
        setNumEqRows(row, num - 1);
    }
    
    /**
     * Overwrites the entries of the map storing the number of equal 
     * rows for each row, with the entries in the input map.
     * 
     * @param rowsToEqRows number of equal rows for each row
     */
    public void replaceNumEqRows(Map<Vector, Integer> rowsToEqRows) {
        rowsToEqRows.entrySet().stream()
                .forEach(entry -> {
                    if (entry.getValue() == 0) {
                        rowToNumEqRows.remove(entry.getKey());
                    } else {
                        rowToNumEqRows.put(entry.getKey(), entry.getValue());
                    }
                });
    }
    
    /**
     * Transitions to the next state in the chain by updating the current matrix
     * to the adjacent matrix.
     *
     * @param swappableEdge1 the first swappable edge that transitions to the
     * adjacent matrix
     * @param swappableEdge2 the second swappable edge that transitions to the
     * adjacent matrix
     * @param newEdge1 the first new edge that transitions to the adjacent
     * matrix
     * @param newEdge2 the second new edge that transitions to the adjacent
     * matrix
     */
    public void transition(Edge swappableEdge1, Edge swappableEdge2, Edge newEdge1, Edge newEdge2) {
        this.swapVals(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
        this.swapEdges(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
    }

    /**
     * Transitions to the next state in the chain by updating the current matrix
     * to the adjacent matrix and the rowToNumEqRows map.
     *
     * @param swappableEdge1 the first swappable edge that transitions to the
     * adjacent matrix
     * @param swappableEdge2 the second swappable edge that transitions to the
     * adjacent matrix
     * @param newEdge1 the first new edge that transitions to the adjacent
     * matrix
     * @param newEdge2 the second new edge that transitions to the adjacent
     * matrix
     * @param swappableRow1 the first swappable row of the matrix
     * @param swappableRow2 the second swappable row of the matrix
     * @param newRow1 the first new row of the adjacent matrix
     * @param newRow2 the second new row of the adjacent matrix
     */
    public void transition(
            Edge swappableEdge1,
            Edge swappableEdge2,
            Edge newEdge1,
            Edge newEdge2,
            Vector swappableRow1,
            Vector swappableRow2,
            Vector newRow1,
            Vector newRow2) {
        // update number of equal rows
        this.decNumEqRows(swappableRow1);
        this.decNumEqRows(swappableRow2);
        this.incNumEqRows(newRow1);
        this.incNumEqRows(newRow2);

        // update matrix and edges
        // call this last so that the swap doesn't update the Vector instances first
        this.transition(swappableEdge1, swappableEdge2, newEdge1, newEdge2);
    }
    
    /**
     * 
     * @return BJDM of bipartite graph represented by this matrix
     */
    public Map<Integer, Map<Integer, Integer>> getBJDM() {
        Map<Integer, Map<Integer, Integer>> BJDM = Maps.newHashMap();
        edges.stream().forEach(edge -> {
            Map<Integer, Integer> entry = BJDM.getOrDefault(rowSums[edge.row], Maps.newHashMap());
            entry.put(colSums[edge.col], entry.getOrDefault(colSums[edge.col], 0) + 1);
            BJDM.put(rowSums[edge.row], entry);
        });
        return BJDM;
    }
    
    /**
     * 
     * @param normalize if the BJDM matrix should be divided by edges.size()
     * @return BJDM of bipartite graph represented by this matrix
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
                M[r-1][c-1] /= 1. * edges.size();
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
    
}
