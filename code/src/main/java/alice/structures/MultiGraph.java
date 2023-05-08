package alice.structures;

import alice.helpers.SwappableAndNewEdges;
import alice.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import java.util.Map;
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
     * Overwrites the entries of the map storing the number of equal rows for
     * each row, with the entries in the input map.
     *
     * @param rowsToEqRows number of equal rows for each row
     */
    private void replaceNumEqRows(Map<RawFastIntCollectionFixedSizeWithOrder, Integer> rowsToEqRows) {
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
