package alice.structures;

import alice.helpers.SwappableAndNewEdges;
import alice.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

/**
 *
 * @author giulia
 */
public class MultiGraph {

    RawFastIntCollectionFixedSizeWithOrder[] rowIdToNeighbors;
    RawFastIntCollectionFixedSizeWithOrder[] colIdToNeighbors;
    Int2ObjectOpenHashMap<int[]> rowSumToVertices;
    Int2ObjectOpenHashMap<int[]> colSumToVertices;
    double[] rowProbabilities;
    double[] colProbabilities;
    int[] id2rowSum;
    int[] id2colSum;
    Int2ObjectOpenHashMap<ObjectOpenHashSet<RawFastIntCollectionFixedSizeWithOrder>> rowSumToUniqueRows;
    Object2IntOpenHashMap<RawFastIntCollectionFixedSizeWithOrder> rowToNumEqRows;
    

    public MultiGraph(MultiGraph G) {
        
        this.rowIdToNeighbors = new RawFastIntCollectionFixedSizeWithOrder[G.rowIdToNeighbors.length];
        for (int i = 0; i < this.rowIdToNeighbors.length; i++) {
            this.rowIdToNeighbors[i] = new RawFastIntCollectionFixedSizeWithOrder(G.rowIdToNeighbors[i]);
        }
        this.colIdToNeighbors = new RawFastIntCollectionFixedSizeWithOrder[G.colIdToNeighbors.length];
        for (int i = 0; i < this.colIdToNeighbors.length; i++) {
            this.colIdToNeighbors[i] = new RawFastIntCollectionFixedSizeWithOrder(G.colIdToNeighbors[i]);
        }
        this.rowSumToVertices = new Int2ObjectOpenHashMap();
        G.rowSumToVertices.int2ObjectEntrySet().stream().forEach(entry -> {
            int[] newV = new int[entry.getValue().length];
            System.arraycopy(entry.getValue(), 0, newV, 0, newV.length);
            this.rowSumToVertices.put(entry.getIntKey(), newV);
        });
        this.colSumToVertices = new Int2ObjectOpenHashMap();
        G.colSumToVertices.int2ObjectEntrySet().stream().forEach(entry -> {
            int[] newV = new int[entry.getValue().length];
            System.arraycopy(entry.getValue(), 0, newV, 0, newV.length);
            this.colSumToVertices.put(entry.getIntKey(), newV);
        });
        this.rowProbabilities = new double[G.rowProbabilities.length];
        System.arraycopy(G.rowProbabilities, 0, this.rowProbabilities, 0, this.rowProbabilities.length);
        this.colProbabilities = new double[G.colProbabilities.length];
        System.arraycopy(G.colProbabilities, 0, this.colProbabilities, 0, this.colProbabilities.length);
        this.id2rowSum = new int[G.id2rowSum.length];
        System.arraycopy(G.id2rowSum, 0, this.id2rowSum, 0, this.id2rowSum.length);
        this.id2colSum = new int[G.id2colSum.length];
        System.arraycopy(G.id2colSum, 0, this.id2colSum, 0, this.id2colSum.length);
        this.rowSumToUniqueRows = new Int2ObjectOpenHashMap();
        G.rowSumToUniqueRows.int2ObjectEntrySet().stream().forEach(entry -> {
            ObjectOpenHashSet<RawFastIntCollectionFixedSizeWithOrder> uniqueRows = new ObjectOpenHashSet();
            for (RawFastIntCollectionFixedSizeWithOrder row : entry.getValue()) {
                uniqueRows.add(new RawFastIntCollectionFixedSizeWithOrder(row));
            }
            this.rowSumToUniqueRows.put(entry.getIntKey(), uniqueRows);
        });
        this.rowToNumEqRows = new Object2IntOpenHashMap();
        G.rowToNumEqRows.object2IntEntrySet().forEach(entry -> {
            this.rowToNumEqRows.put(new RawFastIntCollectionFixedSizeWithOrder(entry.getKey()), entry.getIntValue());
        });
    }
    
    public MultiGraph(RawFastIntCollectionFixedSizeWithOrder[] rowToNeighbors,
            RawFastIntCollectionFixedSizeWithOrder[] colToNeighbors,
            Int2ObjectOpenHashMap<int[]> rowSumToVertices,
            Int2ObjectOpenHashMap<int[]> colSumToVertices) {

        this.rowIdToNeighbors = rowToNeighbors;
        this.colIdToNeighbors = colToNeighbors;
        this.rowSumToVertices = rowSumToVertices;
        this.colSumToVertices = colSumToVertices;
        
        final int rsize = rowSumToVertices.size();
        final int csize = colSumToVertices.size();
        this.id2rowSum = new int[rsize];
        this.id2colSum = new int[csize];
        this.rowSumToUniqueRows = new Int2ObjectOpenHashMap();
        this.rowToNumEqRows = new Object2IntOpenHashMap();
        
        for (RawFastIntCollectionFixedSizeWithOrder row : rowIdToNeighbors) {
            final int rowSum = row.size();
            this.incNumEqRows(row);
            this.addEqRowSumUniqueRow(rowSum, row);
        }
        // row probabilities
        int r = 0;
        double[] P = new double[rsize];
        double total = 0;
        for (int rowSum : rowSumToVertices.keySet()) {
            this.id2rowSum[r] = rowSum;
            int count = rowSumToVertices.get(rowSum).length;
            P[r] = (count + 1) * count / 2.;
            total += P[r];
            r++;
        }
        for (r = 0; r < P.length; r++) {
            P[r] /= total;
        }
        this.rowProbabilities = Utils.cumSum(P);
        // col probabilities
        r = 0;
        P = new double[csize];
        total = 0;
        for (int colSum : colSumToVertices.keySet()) {
            this.id2colSum[r] = colSum;
            int count = colSumToVertices.get(colSum).length;
            P[r] = (count + 1) * count / 2.;
            total += P[r];
            r++;
        }
        for (r = 0; r < P.length; r++) {
            P[r] /= total;
        }
        this.colProbabilities = Utils.cumSum(P);
    }
    
    public RawFastIntCollectionFixedSizeWithOrder[] getRowIdToNeighbors() {
        return this.rowIdToNeighbors;
    }
    
    public RawFastIntCollectionFixedSizeWithOrder[] getColIdToNeighbors() {
        return this.colIdToNeighbors;
    }

    public Int2ObjectOpenHashMap<int[]> getRowSumToVertices() {
        return this.rowSumToVertices;
    }
            
    public Int2ObjectOpenHashMap<int[]> getColSumToVertices() {
        return this.colSumToVertices;
    }

    public double getLogNumEquivMatrices() {
        double logNumEquivMatrices = 0;
        for (int rowSum : rowSumToVertices.keySet()) {
            logNumEquivMatrices += this.getLogNumDistinctTransacOrderings(rowSum);
        }
        return logNumEquivMatrices;
    }
    
    private double getLogNumDistinctTransacOrderings(int rowSum) {
        final ObjectOpenHashSet<RawFastIntCollectionFixedSizeWithOrder> uniqueRows = rowSumToUniqueRows.getOrDefault(rowSum, new ObjectOpenHashSet());
        double logNumDistinctTransacOrderings = this.getLogNumEqRowSumRowsFac(rowSum);
        for (RawFastIntCollectionFixedSizeWithOrder row : uniqueRows) {
            logNumDistinctTransacOrderings -= this.getLogNumEqRowsFac(row);
        }
        return logNumDistinctTransacOrderings;
    }
    
    private double getLogNumEqRowSumRowsFac(int rowSum) {
        if (!rowSumToVertices.containsKey(rowSum)) {
            return 0;
        }
        final int numEqRowSumRows = rowSumToVertices.get(rowSum).length;
        return IntStream.range(0, numEqRowSumRows)
                .mapToDouble(i -> Math.log(numEqRowSumRows - i))
                .sum();
    }
    
    private double getLogNumEqRowsFac(RawFastIntCollectionFixedSizeWithOrder row) {
        final int numEqRows = this.getNumEqRows(row);
        return IntStream.range(0, numEqRows)
                .mapToDouble(i -> Math.log(numEqRows - i))
                .sum();
    }
    
    public double getLogNumEquivAdjMatrices(
            double logNumEquivMatrices,
            RawFastIntCollectionFixedSizeWithOrder swappableRow1,
            RawFastIntCollectionFixedSizeWithOrder swappableRow2,
            RawFastIntCollectionFixedSizeWithOrder newRow1,
            RawFastIntCollectionFixedSizeWithOrder newRow2) {
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
    
    private void addEqRowSumUniqueRow(int rowSum, RawFastIntCollectionFixedSizeWithOrder row) {
        ObjectOpenHashSet<RawFastIntCollectionFixedSizeWithOrder> uniqueRows = rowSumToUniqueRows.getOrDefault(rowSum, new ObjectOpenHashSet());
        if (!uniqueRows.contains(row)) {
            row = new RawFastIntCollectionFixedSizeWithOrder(row);
            uniqueRows.add(row);
            this.rowSumToUniqueRows.put(rowSum, uniqueRows);
        }
    }

    public SwappableAndNewEdges getSwappableAndNewEdges(Random rnd) {
        // sample rows or columns
        boolean rowSwap = rnd.nextBoolean();
        Int2ObjectOpenHashMap<int[]> sumToEqSumElements;
        double[] probabilities;
        int[] id2Sum;
        RawFastIntCollectionFixedSizeWithOrder[] instances;
        if (rowSwap) {
            sumToEqSumElements = rowSumToVertices;
            probabilities = rowProbabilities;
            id2Sum = id2rowSum;
            instances = rowIdToNeighbors;
        } else {
            sumToEqSumElements = colSumToVertices;
            probabilities = colProbabilities;
            id2Sum = id2colSum;
            instances = colIdToNeighbors;
        }
        // we select a row/col to select a row/col sum
        int sum = sampleSum(rnd, probabilities, id2Sum);
        int[] pair = samplePairOfIndices(sumToEqSumElements.get(sum), rnd, rowSwap);
        if (pair == null) {
            return null;
        }
        // select neighbors
        int[] neighs = samplePairRandomNeighbors(rnd, instances[pair[0]], instances[pair[1]]);
        if (neighs == null) {
            // self loop
            return null;
        }
        // select column/row pair
        Edge sampledEdge1;
        Edge sampledEdge2;
        if (rowSwap) {
            sampledEdge1 = new Edge(pair[0], neighs[0]);
            sampledEdge2 = new Edge(pair[1], neighs[1]);
        } else {
            sampledEdge1 = new Edge(neighs[0], pair[0]);
            sampledEdge2 = new Edge(neighs[1], pair[1]);
        }
        return new SwappableAndNewEdges(sampledEdge1, sampledEdge2, 0, 0);
    }
    
    private int sampleSum(Random rnd, double[] probabilities, int[] id2Sum) {
        double p = rnd.nextDouble();
        int id = Arrays.binarySearch(probabilities, p);
        if (id < 0) {
            id = (id + 1) * -1;
        }
        return id2Sum[id];
    }
    
    private int[] samplePairOfIndices(
            int[] elements,
            Random rnd,
            boolean rowSwap) {
        
        if (elements.length < 2 && !rowSwap) {
            return null;
        }
        if (elements.length == 2 && !rowSwap) {
            return elements;
        }
        int[] pair = new int[2];
        pair[0] = elements[rnd.nextInt(elements.length)];
        if (rowSwap) {
            pair[1] = elements[rnd.nextInt(elements.length)];
            return pair;
        }
        do {
            pair[1] = elements[rnd.nextInt(elements.length)];
        } while (pair[0] == pair[1]);
        return pair;
    }
    
    private int[] samplePairRandomNeighbors(Random rnd, 
            RawFastIntCollectionFixedSizeWithOrder first, 
            RawFastIntCollectionFixedSizeWithOrder second) {
        
        RawFastIntCollectionFixedSize[] xor = first.computeXORMultiSets(second);
        if (xor[0].isEmpty() || xor[1].isEmpty()) {
            return null;
        }
        int[] neighs = new int[2];
        neighs[0] = xor[0].getRandomElement(rnd);
        neighs[1] = xor[1].getRandomElement(rnd);
        return neighs;
    }

    public RawFastIntCollectionFixedSizeWithOrder getRowInstance(int r) {
        return rowIdToNeighbors[r];
    }
    
    public RawFastIntCollectionFixedSizeWithOrder getColInstance(int c) {
        return colIdToNeighbors[c];
    }

    public RawFastIntCollectionFixedSizeWithOrder getRowCopy(int r) {
        return new RawFastIntCollectionFixedSizeWithOrder(rowIdToNeighbors[r]);
    }

    public RawFastIntCollectionFixedSizeWithOrder[] getNewRows(SwappableAndNewEdges sne) {
        final RawFastIntCollectionFixedSizeWithOrder newRow1 = this.getRowCopy(sne.swappableEdge1.row);
        newRow1.fastReplaceWithoutChecks(sne.swappableEdge1.col, sne.swappableEdge2.col);
        final RawFastIntCollectionFixedSizeWithOrder newRow2 = this.getRowCopy(sne.swappableEdge2.row);
        newRow2.fastReplaceWithoutChecks(sne.swappableEdge2.col, sne.swappableEdge1.col);
        return new RawFastIntCollectionFixedSizeWithOrder[]{newRow1, newRow2};
    }

    public int getNumEqRows(RawFastIntCollectionFixedSizeWithOrder row) {
        return rowToNumEqRows.getOrDefault(row, 0);
    }

    public boolean setNumEqRows(RawFastIntCollectionFixedSizeWithOrder row, int eqRowsNum) {
        if (eqRowsNum == 0) {
            removeNumEqRows(row);
            return false;
        }
        if (!rowToNumEqRows.containsKey(row)) {
            rowToNumEqRows.put(new RawFastIntCollectionFixedSizeWithOrder(row), eqRowsNum);
            return true;
        }
        rowToNumEqRows.put(row, eqRowsNum);
        return false;
    }

    private void removeNumEqRows(RawFastIntCollectionFixedSizeWithOrder row) {
        rowToNumEqRows.removeInt(row);
    }

    /**
     * Increment the number of equal rows for the input row by 1.
     *
     * @param row the input row
     * @return true if the row was not aleady present in the map of equal rows;
     * false otherwise
     */
    private boolean incNumEqRows(RawFastIntCollectionFixedSizeWithOrder row) {
        return setNumEqRows(row, getNumEqRows(row) + 1);
    }

    /**
     * Decrement the number of equal rows for the input row by 1.
     *
     * @param row the input row
     * @throws IllegalArgumentException if the current number of equal rows is
     * not greater than 0
     */
    private void decNumEqRows(RawFastIntCollectionFixedSizeWithOrder row) {
        final int num = getNumEqRows(row);
        if (num <= 0) {
            throw new IllegalArgumentException(
                    "The number of rows equal to row " + row + " is " + num + ", which is non positive.");
        }
        setNumEqRows(row, num - 1);
    }

    /**
     * Transitions to the next state in the chain by updating the current matrix
     * to the adjacent matrix.
     *
     * @param sne swappable edges that transition to the adjacent matrix
     */
    public void transition(SwappableAndNewEdges sne) {
        colIdToNeighbors[sne.swappableEdge1.col].fastReplaceWithoutChecks(sne.swappableEdge1.row, sne.swappableEdge2.row);
        colIdToNeighbors[sne.swappableEdge2.col].fastReplaceWithoutChecks(sne.swappableEdge2.row, sne.swappableEdge1.row);
        rowIdToNeighbors[sne.swappableEdge1.row].fastReplaceWithoutChecks(sne.swappableEdge1.col, sne.swappableEdge2.col);
        rowIdToNeighbors[sne.swappableEdge2.row].fastReplaceWithoutChecks(sne.swappableEdge2.col, sne.swappableEdge1.col);
    }

    /**
     * Transitions to the next state in the chain by updating the current matrix
     * to the adjacent matrix and the rowToNumEqRows map.
     *
     * @param sne swappable edges that transition to the adjacent matrix
     * @param swappableRow1 the first swappable row of the matrix
     * @param swappableRow2 the second swappable row of the matrix
     * @param newRow1 the first new row of the adjacent matrix
     * @param newRow2 the second new row of the adjacent matrix
     */
    public void transition(
            SwappableAndNewEdges sne,
            RawFastIntCollectionFixedSizeWithOrder swappableRow1,
            RawFastIntCollectionFixedSizeWithOrder swappableRow2,
            RawFastIntCollectionFixedSizeWithOrder newRow1,
            RawFastIntCollectionFixedSizeWithOrder newRow2) {
        // update number of equal rows
        this.decNumEqRows(swappableRow1);
        this.decNumEqRows(swappableRow2);
        this.incNumEqRows(newRow1);
        this.incNumEqRows(newRow2);
        // update matrix and edges
        this.transition(sne);
    }
    
    public boolean areSwappable(SwappableAndNewEdges sne) {
        final Edge first = sne.swappableEdge1;
        final Edge second = sne.swappableEdge2;
        return !(first.row == second.row || first.col == second.col ||
                getRowInstance(first.row).contains(second.col) ||
                getRowInstance(second.row).contains(first.col) ||
                (getRowInstance(first.row).size() != getRowInstance(second.row).size() && 
                getColInstance(first.col).size() != getColInstance(second.col).size()));
    }
    
    public SwappableAndNewEdges getRandomSwappables(Random rnd) {
        
        final int v1Index = rnd.nextInt(rowIdToNeighbors.length);
        final int v2Index = rnd.nextInt(rowIdToNeighbors.length);
        final int n1Index = rowIdToNeighbors[v1Index].getRandomElement(rnd);
        final int n2Index = rowIdToNeighbors[v2Index].getRandomElement(rnd);
        final Edge firstEdge = new Edge(v1Index, n1Index);
        final Edge secondEdge = new Edge(v2Index, n2Index);
        return new SwappableAndNewEdges(firstEdge, secondEdge, 0, 0);
    }
    
    public int getNumEdges() {
        return rowSumToVertices
                .keySet()
                .intStream()
                .map(sum -> sum * rowSumToVertices.get(sum).length)
                .sum();
    }
    
    public int getNumRows() {
        return rowIdToNeighbors.length;
    }

    @Override
    public String toString() {
        String s = "[";
        for (RawFastIntCollectionFixedSizeWithOrder vertexToNeighbor : rowIdToNeighbors) {
            s += vertexToNeighbor.toString();
            s += "\n";
        }
        s += "]";
        return s;
    }

}
