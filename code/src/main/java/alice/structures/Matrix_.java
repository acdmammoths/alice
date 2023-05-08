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
import alice.helpers.SwappableAndNewEdges;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
public class Matrix_ {

    /**
     * A 0-1 matrix representation of the dataset.
     */
    private SparseMatrix_ matrix;

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
    public final Edge[] edges;

    /**
     * A map where each key is a row and the value is the number of rows equal
     * to that row.
     */
    private final Map<RawFastIntCollectionFixedSize, Integer> rowToNumEqRows;
    
    /**
     * Creates an instance of {@link Matrix} from a 0-1 {@link SparseMatrix} by
     * initializing necessary data structures from the matrix.
     *
     * @param inMatrix a 0-1 matrix representation of the dataset
     */
    public Matrix_(SparseMatrix_ inMatrix) {
        this.rowToNumEqRows = Maps.newHashMap();
        this.matrix = new SparseMatrix_(inMatrix.getNumRows(), inMatrix.getNumCols());
        this.rowSums = new int[inMatrix.getNumRows()];
        this.colSums = new int[inMatrix.getNumCols()];
        Set<Edge> tmpEdges = Sets.newHashSet();
        for (int r = 0; r < inMatrix.getNumRows(); r++) {
            matrix.replaceRow(r, new RawFastIntCollectionFixedSize(inMatrix.getRowInstance(r)));
            rowSums[r] = inMatrix.getNumNonzeroIndices(r);
            for (int c : inMatrix.getNonzeroIndices(r)) {
                tmpEdges.add(new Edge(r, c));
                this.colSums[c]++;
            }
        }
        for (int c = 0; c < inMatrix.getNumCols(); c++) {
            matrix.replaceCol(c, new RawFastIntCollectionFixedSize(inMatrix.getColInstance(c)));
        }
        int counter = 0;
        this.edges = new Edge[tmpEdges.size()];
        for (Edge edge : tmpEdges) {
            this.edges[counter] = edge;
            counter ++;
        }
    }
    
    public Matrix_(SparseMatrix_ inMatrix, Edge[] edges) {
        this.rowToNumEqRows = Maps.newHashMap();
        this.matrix = new SparseMatrix_(inMatrix.getNumRows(), inMatrix.getNumCols());
        this.rowSums = new int[inMatrix.getNumRows()];
        this.colSums = new int[inMatrix.getNumCols()];
        for (int r = 0; r < inMatrix.getNumRows(); r++) {
            matrix.replaceRow(r, new RawFastIntCollectionFixedSize(inMatrix.getRowInstance(r)));
            rowSums[r] = inMatrix.getNumNonzeroIndices(r);
            for (int c : inMatrix.getNonzeroIndices(r)) {
                this.colSums[c]++;
            }
        }
        for (int c = 0; c < inMatrix.getNumCols(); c++) {
            matrix.replaceCol(c, new RawFastIntCollectionFixedSize(inMatrix.getColInstance(c)));
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
        Matrix_ otherMatrix = (Matrix_) o;
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

    public SparseMatrix_ getMatrix() {
        return this.matrix;
    }

    public int getVal(int row, int col) {
        return this.matrix.isInRow(row, col);
    }
    
    public void setInRow(int row, int oldc, int newc) {
        this.matrix.replaceValueInRow(row, oldc, newc);
    }
    
    public void setRow(int id, RawFastIntCollectionFixedSize row) {
        this.matrix.replaceRow(id, row);
    }
    
    public void setInCol(int col, int oldr, int newr) {
        this.matrix.replaceValueInCol(col, oldr, newr);
    }
    
    public void setCol(int id, RawFastIntCollectionFixedSize col) {
        this.matrix.replaceCol(id, col);
    }

    public RawFastIntCollectionFixedSize getRowCopy(int row) {
        return this.matrix.getRowCopy(row);
    }

    public RawFastIntCollectionFixedSize getRowInstance(int row) {
        return this.matrix.getRowInstance(row);
    }
    
    public RawFastIntCollectionFixedSize[] getRows() {
        return getMatrix().getRows();
    }
    
    public RawFastIntCollectionFixedSize[] getCols() {
        return getMatrix().getCols();
    }
    
    public RawFastIntCollectionFixedSize getColCopy(int col) {
        return this.matrix.getColCopy(col);
    }

    public RawFastIntCollectionFixedSize getColInstance(int col) {
        return this.matrix.getColInstance(col);
    }

    public int[] getNonzeroIndices(int row) {
        return this.matrix.getNonzeroIndices(row);
    }
    
    public int[] getNonzeroColIndices(int col) {
        return this.matrix.getNonzeroColIndices(col);
    }

    public int getNumRows() {
        return this.matrix.getNumRows();
    }
    
    public int getNumCols() {
        return this.matrix.getNumCols();
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
        return Sets.newHashSet(this.edges);
    }

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
    public SwappableAndNewEdges sampleEdges(Random rnd) {
        final int edge1Index = rnd.nextInt(this.getNumEdges());
        int edge2Index;
        do {
            edge2Index = rnd.nextInt(this.getNumEdges());
        } while (edge1Index == edge2Index);

        return new SwappableAndNewEdges(edges[edge1Index], edges[edge2Index], edge1Index, edge2Index);
    }
    
    /**
     * Swaps 1s across two rows in the matrix.
     *
     * @param sne edges to swap
     */
    public void swapVals(SwappableAndNewEdges sne) {
        this.setInRow(sne.swappableEdge1.row, sne.swappableEdge1.col, sne.swappableEdge2.col);
        this.setInRow(sne.swappableEdge2.row, sne.swappableEdge2.col, sne.swappableEdge1.col);
        this.setInCol(sne.swappableEdge1.col, sne.swappableEdge1.row, sne.swappableEdge2.row);
        this.setInCol(sne.swappableEdge2.col, sne.swappableEdge2.row, sne.swappableEdge1.row);
    }

    /**
     * Swaps the edges in the graph representation of the matrix.
     *
     * @param sne edges to swap and their current position in the edge array
     */
    public void swapEdges(SwappableAndNewEdges sne) {
        final Edge newEdge1 = new Edge(sne.swappableEdge1.row, sne.swappableEdge2.col);
        final Edge newEdge2 = new Edge(sne.swappableEdge2.row, sne.swappableEdge1.col);
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
    public RawFastIntCollectionFixedSize[] getNewRows(Edge newEdge1, Edge newEdge2) {
        final RawFastIntCollectionFixedSize newRow1 = this.getRowCopy(newEdge1.row);
        newRow1.fastReplaceWithoutChecks(newEdge2.col, newEdge1.col);
        final RawFastIntCollectionFixedSize newRow2 = this.getRowCopy(newEdge2.row);
        newRow2.fastReplaceWithoutChecks(newEdge1.col, newEdge2.col);
        return new RawFastIntCollectionFixedSize[]{newRow1, newRow2};
    }
    
    /**
     * Gets the new cols of the matrix defined by the swappable and new edges.
     *
     * @param newEdge1 the first new edge to be added
     * @param newEdge2 the second new edge to be added
     * @return the two new cols of the matrix
     */
    public RawFastIntCollectionFixedSize[] getNewCols(Edge newEdge1, Edge newEdge2) {
        final RawFastIntCollectionFixedSize newCol1 = this.getColCopy(newEdge1.col);
        newCol1.fastReplaceWithoutChecks(newEdge2.row, newEdge1.row);
        final RawFastIntCollectionFixedSize newCol2 = this.getColCopy(newEdge2.col);
        newCol2.fastReplaceWithoutChecks(newEdge1.row, newEdge2.row);
        return new RawFastIntCollectionFixedSize[]{newCol1, newCol2};
    }
    
    public Map<RawFastIntCollectionFixedSize, Integer> getRowToNumEqRowsMap() {
        return this.rowToNumEqRows;
    }
    
    public int getNumEqRows(RawFastIntCollectionFixedSize row) {
        return rowToNumEqRows.getOrDefault(row, 0);
    }
    
    public boolean setNumEqRows(RawFastIntCollectionFixedSize row, int eqRowsNum) {
        if (eqRowsNum == 0) {
            removeNumEqRows(row);
            return false;
        }
        if (!rowToNumEqRows.containsKey(row)) {
            rowToNumEqRows.put(new RawFastIntCollectionFixedSize(row), eqRowsNum);
            return true;
        }
        rowToNumEqRows.put(row, eqRowsNum);
        return false;
    }
    
    public void removeNumEqRows(RawFastIntCollectionFixedSize row) {
        rowToNumEqRows.remove(row);
    }

    /**
     * Increment the number of equal rows for the input row by 1.
     * 
     * @param row the input row
     * @return true if the row was not aleady present in the map of equal rows;
     * false otherwise
     */
    public boolean incNumEqRows(RawFastIntCollectionFixedSize row) {
        return setNumEqRows(row, getNumEqRows(row) + 1);
    }

    /**
     * Decrement the number of equal rows for the input row by 1.
     *
     * @param row the input row
     * @throws IllegalArgumentException if the current number of equal rows is
     * not greater than 0
     */
    public void decNumEqRows(RawFastIntCollectionFixedSize row) {
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
    public void replaceNumEqRows(Map<RawFastIntCollectionFixedSize, Integer> rowsToEqRows) {
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
     * @param sne swappable edges that transition to the
     * adjacent matrix
     */
    public void transition(SwappableAndNewEdges sne) {
        this.swapVals(sne);
        this.swapEdges(sne);
    }

    /**
     * Transitions to the next state in the chain by updating the current matrix
     * to the adjacent matrix and the rowToNumEqRows map.
     *
     * @param sne swappable edges that transition to the
     * adjacent matrix
     * @param swappableRow1 the first swappable row of the matrix
     * @param swappableRow2 the second swappable row of the matrix
     * @param newRow1 the first new row of the adjacent matrix
     * @param newRow2 the second new row of the adjacent matrix
     */
    public void transition(
            SwappableAndNewEdges sne,
            RawFastIntCollectionFixedSize swappableRow1,
            RawFastIntCollectionFixedSize swappableRow2,
            RawFastIntCollectionFixedSize newRow1,
            RawFastIntCollectionFixedSize newRow2) {
        // update number of equal rows
        this.decNumEqRows(swappableRow1);
        this.decNumEqRows(swappableRow2);
        this.incNumEqRows(newRow1);
        this.incNumEqRows(newRow2);

        // update matrix and edges
        // call this last so that the swap doesn't update the Vector instances first
        this.transition(sne);
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
        };
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
    
    public long getNumButterflies() {
        RawFastIntCollectionFixedSize[] vec = matrix.getCols();
        if (matrix.getNumRows() < matrix.getNumCols()) {
            vec = matrix.getRows();
        }
        long butt = 0;
        for (int i = 0; i < vec.length; i++) {
            for (int j = i + 1; j < vec.length; j++) {
                int inter = (int) vec[i].computeInterSize(vec[j]);
                if (inter > 1) {
                    butt += CombinatoricsUtils.binomialCoefficient(inter, 2);   
                } 
            }
        }
        return butt;
    }
    
    public SwappableAndNewEdges getRandomSwappables(Random rnd) {
        final int e1Index = rnd.nextInt(edges.length);
        final int e2Index = rnd.nextInt(edges.length);
        return new SwappableAndNewEdges(edges[e1Index], edges[e2Index], e1Index, e2Index);
    }
    
}
