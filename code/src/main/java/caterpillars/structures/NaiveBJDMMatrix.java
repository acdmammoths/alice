package caterpillars.structures;

import caterpillars.helpers.SwappableAndNewEdges;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gr.james.sampling.LiLSampling;
import gr.james.sampling.RandomSamplingCollector;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.javatuples.Quartet;


public class NaiveBJDMMatrix extends Matrix {

    /**
     * A map where each key is a row sum and the value is the unique set of rows
     * with that row sum.
     */
    private Map<Integer, Set<Vector>> rowSumToUniqueRows;
    
    /**
     * A map where each key is a col sum and the value is the unique set of cols
     * with that col sum.
     */
    private Map<Integer, Set<Vector>> colSumToUniqueCols;

    /**
     * A map where each key is a row sum and the value is the set of rows
     * with that row sum.
     */
    private Map<Integer, Set<Integer>> rowSumToEqRowSumRows;
    
    /**
     * A map where each key is a col sum and the value is the set of cols
     * with that col sum.
     */
    private Map<Integer, Set<Integer>> colSumToEqColSumCols;
    
    /**
     * Creates an instance of {@link NaiveBJDMMatrix} from a 0-1
     * {@link SparseMatrix} by initializing necessary data structures from the
     * matrix.
     *
     * @param inMatrix a 0-1 matrix representation of the dataset
     */
    public NaiveBJDMMatrix(SparseMatrix inMatrix) {
        super(inMatrix);
        this.rowSumToUniqueRows = Maps.newHashMap();
        this.colSumToUniqueCols = Maps.newHashMap();
        this.rowSumToEqRowSumRows = Maps.newHashMap();
        this.colSumToEqColSumCols = Maps.newHashMap();
        for (int r = 0; r < inMatrix.getNumRows(); r++) {
            final Vector row = this.getRowInstance(r);
            final int rowSum = this.getRowSum(r);
            incNumEqRows(row);
            this.incEqRowSumRows(rowSum, r);
            addEqRowSumUniqueRow(rowSum, row);
        }
        for (int c = 0; c < inMatrix.getNumCols(); c++) {
            final Vector col = this.getColInstance(c);
            final int colSum = this.getColSum(c);
//            this.incNumEqCols(col); 
            this.incEqColSumCols(colSum, c);
            addEqColSumUniqueCol(colSum, col);
        }
    }

    private void incEqRowSumRows(int rowSum, int row) {
        Set<Integer> tmpSet = rowSumToEqRowSumRows.getOrDefault(rowSum, Sets.newHashSet());
        tmpSet.add(row);
        rowSumToEqRowSumRows.put(rowSum, tmpSet);
    }
    
    private void incEqColSumCols(int colSum, int col) {
        Set<Integer> tmpSet = colSumToEqColSumCols.getOrDefault(colSum, Sets.newHashSet());
        tmpSet.add(col);
        colSumToEqColSumCols.put(colSum, tmpSet);
    }

    public Set<Vector> getEqRowSumUniqueRows(int rowSum) {
        return this.rowSumToUniqueRows.getOrDefault(rowSum, Sets.newHashSet());
    }
    
    private void addEqRowSumUniqueRow(int rowSum, Vector row) {
        Set<Vector> uniqueRows = this.getEqRowSumUniqueRows(rowSum);
        if (uniqueRows.contains(row)) {
            return;
        }
        row = row.copy();
        uniqueRows.add(row);
        this.rowSumToUniqueRows.put(rowSum, uniqueRows);
    }
    
    public Set<Vector> getEqColSumUniqueCols(int colSum) {
        return this.colSumToUniqueCols.getOrDefault(colSum, Sets.newHashSet());
    }
    
    private void addEqColSumUniqueCol(int colSum, Vector col) {
        Set<Vector> uniqueCols = this.getEqColSumUniqueCols(colSum);
        if (uniqueCols.contains(col)) {
            return;
        }
        col = col.copy();
        uniqueCols.add(col);
        this.colSumToUniqueCols.put(colSum, uniqueCols);
    }
    
    /**
     * Gets the log of the number of matrices in the chain that are equivalent
     * to the current matrix (i.e., matrices that represent the same dataset as
     * the current matrix). Reference: Lemma 5.1 in the paper.
     *
     * @return the log of the number of equivalent matrices
     */
    public double getLogNumEquivMatrices() {
        double logNumEquivMatrices = 0;
        for (int rowSum : rowSumToEqRowSumRows.keySet()) {
            logNumEquivMatrices += this.getLogNumDistinctTransacOrderings(rowSum);
        }
        return logNumEquivMatrices;
    }

    /**
     * Gets the log of the number of distinct transaction orderings for the
     * input row sum (transaction length). Reference: Lemma 5.1 in the paper.
     *
     * @param rowSum the input row sum (transaction length)
     * @return the log of the number of distinct transaction orderings for the
     * row sum
     */
    public double getLogNumDistinctTransacOrderings(int rowSum) {
        final Set<Vector> uniqueRows = this.getEqRowSumUniqueRows(rowSum);
        double logNumDistinctTransacOrderings = this.getLogNumEqRowSumRowsFac(rowSum);
        for (Vector row : uniqueRows) {
            logNumDistinctTransacOrderings -= this.getLogNumEqRowsFac(row);
        }
        return logNumDistinctTransacOrderings;
    }

    /**
     * Gets the log of the number of rows that have an equal row sum as the
     * input row sum. Reference: Lemma 5.1 in the paper.
     *
     * @param rowSum the input row sum
     * @return the log of the number of rows that have an equal row sum
     */
    public double getLogNumEqRowSumRowsFac(int rowSum) {
        final int numEqRowSumRows = this.rowSumToEqRowSumRows.getOrDefault(rowSum, Sets.newHashSet()).size();
        return IntStream.range(0, numEqRowSumRows)
                .mapToDouble(i -> Math.log(numEqRowSumRows - i))
                .sum();
    }

    /**
     * Gets the log of the number rows that are equal to the input row
     * factorial. Reference: Lemma 5.1 in the paper.
     *
     * @param row the input row
     * @return the log of the number of rows that are equal to the given row
     * factorial
     */
    public double getLogNumEqRowsFac(Vector row) {
        final int numEqRows = this.getNumEqRows(row);
        return IntStream.range(0, numEqRows)
                .mapToDouble(i -> Math.log(numEqRows - i))
                .sum();
    }

    /**
     * Gets the log of the number of matrices in the chain that are equivalent
     * to the adjacent matrix (i.e., matrices that represent the same dataset as
     * the adjacent matrix). Reference: Section A.2 in the paper.
     *
     * @param logNumEquivMatrices the log of the number of equivalent matrices
     * for the current matrix
     * @param swappableRow1 the first swappable row
     * @param swappableRow2 the second swappable row
     * @param newRow1 the first new row
     * @param newRow2 the second new row
     * @return the log of the number of equivalent adjacent matrices
     */
    public double getLogNumEquivAdjMatrices(
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
    
    public SwappableAndNewEdges getSwappableAndNewEdges(Random rnd) {
        Quartet<Integer, Integer, Integer, Integer> sampledEdges;
        int r1, r2, c1, c2;
        boolean rowBased = rnd.nextBoolean();
        if (rowBased) {
            sampledEdges = sampleEdge(rowSumToEqRowSumRows, getRows(), rowSums, rnd);
            r1 = sampledEdges.getValue0();
            r2 = sampledEdges.getValue1();
            c1 = sampledEdges.getValue2();
            c2 = sampledEdges.getValue3();
        } else {
            sampledEdges = sampleEdge(colSumToEqColSumCols, getCols(), colSums, rnd);
            r1 = sampledEdges.getValue2();
            r2 = sampledEdges.getValue3();
            c1 = sampledEdges.getValue0();
            c2 = sampledEdges.getValue1();
        }
        final Edge sampledEdge1 = new Edge(r1, c1);
        final Edge sampledEdge2 = new Edge(r2, c2);
        final Edge newEdge1 = new Edge(r1, c2);
        final Edge newEdge2 = new Edge(r2, c1);

        return new SwappableAndNewEdges(sampledEdge1, sampledEdge2, newEdge1, newEdge2);
    }
    
    private Quartet<Integer, Integer, Integer, Integer> sampleEdge(
            Map<Integer, Set<Integer>> sumToEqSumElements, 
            List<Vector> instances, 
            int[] sums,
            Random rnd) {

        // we select a row to select a row sum
        int e1 = rnd.nextInt(instances.size());
        int s = sums[e1];
        List<Integer> cands = Lists.newArrayList(sumToEqSumElements.get(s));
        int e2 = cands.get(rnd.nextInt(cands.size()));
        // column differences
        Set<Integer> S1 = Sets.newHashSet(instances.get(e1).getNonzeroIndices());
        S1.retainAll(instances.get(e2).getNonzeroIndices());
        Set<Integer> S2 = Sets.newHashSet(instances.get(e2).getNonzeroIndices());
        S2.retainAll(instances.get(e1).getNonzeroIndices());
        List<Integer> candC1 = Lists.newArrayList(S1);
        List<Integer> candC2 = Lists.newArrayList(S2);
        if (Math.min(S1.size(), S2.size()) == 0) {
            // self loop
            int f = instances.get(e1).getNonzeroIndices().iterator().next();
            return new Quartet<>(e1, e2, f, f); 
        }
        int f1 = candC1.get(rnd.nextInt(candC1.size()));
        int f2 = candC2.get(rnd.nextInt(candC2.size()));
        return new Quartet<>(e1, e2, f1, f2);
    }
    
    public List<SwappableAndNewEdges> getSwappablesNewEdges(Random rnd) {
        List<SwappableAndNewEdges> swappables = Lists.newArrayList();
        List<Quartet<Integer, Integer, Integer, Integer>> edgesSampled;
        boolean rowBased = rnd.nextBoolean();
        if (rowBased) {
            edgesSampled = sampleEdges(rowSumToEqRowSumRows, getRows(), rowSums, rnd);
            for (int i = 0; i < edgesSampled.size(); i++) {
                Quartet<Integer, Integer, Integer, Integer> edge = edgesSampled.get(i);
                Edge sampledEdge1 = new Edge(edge.getValue0(), edge.getValue2());
                Edge sampledEdge2 = new Edge(edge.getValue1(), edge.getValue3());
                Edge newEdge1 = new Edge(edge.getValue0(), edge.getValue3());
                Edge newEdge2 = new Edge(edge.getValue1(), edge.getValue2());
                swappables.add(new SwappableAndNewEdges(sampledEdge1, sampledEdge2, newEdge1, newEdge2));
            }
            return swappables;
        }
        edgesSampled = sampleEdges(colSumToEqColSumCols, getCols(), colSums, rnd);
        for (int i = 0; i < edgesSampled.size(); i++) {
            Quartet<Integer, Integer, Integer, Integer> edge = edgesSampled.get(i);
            Edge sampledEdge1 = new Edge(edge.getValue2(), edge.getValue0());
            Edge sampledEdge2 = new Edge(edge.getValue3(), edge.getValue1());
            Edge newEdge1 = new Edge(edge.getValue2(), edge.getValue1());
            Edge newEdge2 = new Edge(edge.getValue3(), edge.getValue0());
            swappables.add(new SwappableAndNewEdges(sampledEdge1, sampledEdge2, newEdge1, newEdge2));
        }
        return swappables;
    }
    
    private List<Quartet<Integer, Integer, Integer, Integer>> sampleEdges(
            Map<Integer, Set<Integer>> sumToEqSumElements, 
            List<Vector> instances, 
            int[] sums,
            Random rnd) {
    
        List<Quartet<Integer, Integer, Integer, Integer>> edgesSampled = Lists.newArrayList();
        // we select a row to select a row sum
        int e1 = rnd.nextInt(instances.size());
        int s = sums[e1];
        List<Integer> cands = Lists.newArrayList(sumToEqSumElements.get(s));
        int e2 = cands.get(rnd.nextInt(cands.size()));
        // column differences
        Set<Integer> S1 = Sets.newHashSet(instances.get(e1).getNonzeroIndices());
        S1.retainAll(instances.get(e2).getNonzeroIndices());
        List<Integer> cand1 = Lists.newArrayList(S1);
        Set<Integer> S2 = Sets.newHashSet(instances.get(e2).getNonzeroIndices());
        S2.retainAll(instances.get(e1).getNonzeroIndices());
        List<Integer> cand2 = Lists.newArrayList(S2);
        int minNum = Math.min(S1.size(), S2.size());
        if (minNum == 0) {
            int f = instances.get(e1).getNonzeroIndices().iterator().next();
            edgesSampled.add(new Quartet<>(e1, e2, f, f));
            return edgesSampled;
        }
        int numEdgesToSwap = rnd.nextInt(minNum) + 1;
        RandomSamplingCollector<Integer> collector = LiLSampling.collector(numEdgesToSwap, rnd);
        List<Integer> subS1 = IntStream.range(0, cand1.size())
                    .boxed()
                    .collect(collector)
                    .stream()
                    .map(i -> cand1.get(i))
                    .collect(Collectors.toList());
        List<Integer> subS2 = IntStream.range(0, cand2.size())
                    .boxed()
                    .collect(collector)
                    .stream()
                    .map(i -> cand2.get(i))
                    .collect(Collectors.toList());
        for (int i = 0; i < numEdgesToSwap; i++) {
            edgesSampled.add(new Quartet<>(e1, e2, subS1.get(i), subS2.get(i)));
        }
        return edgesSampled;
    }
    
    public Map<Integer, Map<Integer, Integer>> getBJDM() {
        Map<Integer, Map<Integer, Integer>> BJDM = Maps.newHashMap();
        edges.stream().forEach(edge -> {
            Map<Integer, Integer> entry = BJDM.getOrDefault(rowSums[edge.row], Maps.newHashMap());
            entry.put(colSums[edge.col], entry.getOrDefault(colSums[edge.col], 0) + 1);
            BJDM.put(rowSums[edge.row], entry);
        });
        return BJDM;
    }
    
    public double getSamplingProb(Edge swappableEdge1, Edge swappableEdge2) {
        
        double prob = 0.0;
        int rowSum1 = rowSums[swappableEdge1.row];
        int rowSum2 = rowSums[swappableEdge2.row];
        int colSum1 = colSums[swappableEdge1.col];
        int colSum2 = colSums[swappableEdge2.col];
        
        if (rowSum1 == rowSum2) {
            prob += getProb(rowSumToUniqueRows, getRows(), rowSum1, swappableEdge1.row, swappableEdge2.row);
        }
        if (colSum1 == colSum2) {
            prob += getProb(colSumToUniqueCols, getCols(), colSum1, swappableEdge1.col, swappableEdge2.col);
        }
        return prob;
    }
    
    private double getProb(Map<Integer, Set<Vector>> sumToEqSum, 
            List<Vector> instances, 
            int sum, 
            int e1, 
            int e2) {
        int size = sumToEqSum.get(sum).size();
        int dp = size * (size - 1) / 2;
        double numPairs = Math.pow(size, 2);
        int sumUniquePerSum = sumToEqSum.values()
                .stream()
                .mapToInt(l -> l.size() * (l.size() - 1) / 2)
                .sum();
        Set<Integer> S1 = Sets.newHashSet(instances.get(e1).getNonzeroIndices());
        S1.retainAll(instances.get(e2).getNonzeroIndices());
        Set<Integer> S2 = Sets.newHashSet(instances.get(e2).getNonzeroIndices());
        S2.retainAll(instances.get(e1).getNonzeroIndices());
        int H12 = S1.size() * S2.size();
        return dp / (2. * sumUniquePerSum * numPairs * H12);
    }
    
}
