package caterpillars.structures;

import caterpillars.helpers.SwappableAndNewEdges;
import caterpillars.helpers.SwappableLists;
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
import org.javatuples.Pair;
import org.apache.commons.math3.util.CombinatoricsUtils;

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
     * A map where each key is a row sum and the value is the set of rows with
     * that row sum.
     */
    private Map<Integer, Set<Integer>> rowSumToEqRowSumRows;

    /**
     * A map where each key is a col sum and the value is the set of cols with
     * that col sum.
     */
    private Map<Integer, Set<Integer>> colSumToEqColSumCols;
    
    private List<Integer> samplableRows;
    
    private List<Integer> samplableCols;

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
        this.samplableRows = rowSumToEqRowSumRows
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
        this.samplableCols = colSumToEqColSumCols
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
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
    
    public double getLogNumEquivAdjMatrices(
            double logNumEquivMatrices,
            Vector swappableRow1,
            Vector swappableRow2,
            Vector newRow1,
            Vector newRow2,
            Map<Vector, Integer> rowToEqRows) {
        // check if swap leads to a non-equivalent matrix
        // note: swappableRow1.equals(newRow2) iff swappableRow2.equals(newRow1)
        // (see notes/22-01-26-TransactionEqualsNewTransactionImplication.pdf)
        if (swappableRow1.equals(newRow2)) {
            return logNumEquivMatrices;
        }
        return logNumEquivMatrices
                + Math.log(rowToEqRows.getOrDefault(swappableRow1, 0))
                + Math.log(rowToEqRows.getOrDefault(swappableRow2, 0))
                - Math.log1p(rowToEqRows.getOrDefault(newRow1, 0))
                - Math.log1p(rowToEqRows.getOrDefault(newRow2, 0));
    }

    public SwappableAndNewEdges getSwappableAndNewEdges(Random rnd) {
        boolean rowSwap = rnd.nextBoolean();
        if (rowSwap) {
            return sampleEdge(rowSumToEqRowSumRows, samplableRows, 
                    getRows(), rowSums, true, rnd);
        }
        return sampleEdge(colSumToEqColSumCols, samplableCols, 
                getCols(), colSums, false, rnd);
    }
    
    private SwappableAndNewEdges sampleEdge(
            Map<Integer, Set<Integer>> sumToEqSumElements,
            List<Integer> samplable,
            List<Vector> instances,
            int[] sums,
            boolean rowSwap,
            Random rnd) {
        // we select a row/col to select a row/col sum
        Pair<Integer, Integer> pair = samplePairOfIndices(sumToEqSumElements, samplable, sums, rnd);
        // column differences
        Set<Integer> S1 = Sets.newHashSet(instances.get(pair.getValue0()).getNonzeroIndices());
        S1.removeAll(instances.get(pair.getValue1()).getNonzeroIndices());
        Set<Integer> S2 = Sets.newHashSet(instances.get(pair.getValue1()).getNonzeroIndices());
        S2.removeAll(instances.get(pair.getValue0()).getNonzeroIndices());
        List<Integer> candC1 = Lists.newArrayList(S1);
        List<Integer> candC2 = Lists.newArrayList(S2);
        if (S1.isEmpty()) {
            // self loop
            return null;
        }
        int f1 = candC1.get(rnd.nextInt(candC1.size()));
        int f2 = candC2.get(rnd.nextInt(candC2.size()));
        Edge sampledEdge1;
        Edge sampledEdge2;
        Edge newEdge1;
        Edge newEdge2;
        if (rowSwap) {
            sampledEdge1 = new Edge(pair.getValue0(), f1);
            sampledEdge2 = new Edge(pair.getValue1(), f2);
            newEdge1 = new Edge(pair.getValue0(), f2);
            newEdge2 = new Edge(pair.getValue1(), f1);
        } else {
            sampledEdge1 = new Edge(f1, pair.getValue0());
            sampledEdge2 = new Edge(f2, pair.getValue1());
            newEdge1 = new Edge(f1, pair.getValue1());
            newEdge2 = new Edge(f2, pair.getValue0()); 
        }
        return new SwappableAndNewEdges(sampledEdge1, sampledEdge2, newEdge1, newEdge2);
    }

    public SwappableLists getSwappablesNewEdges(Random rnd) {
        boolean rowSwap = rnd.nextBoolean();
        if (rowSwap) {
            return sampleEdges(rowSumToEqRowSumRows, samplableRows, getRows(), 
                    rowSums, true, rnd);
        }
        return sampleEdges(colSumToEqColSumCols, samplableCols, getCols(), 
                colSums, false, rnd);
    }

    private SwappableLists sampleEdges(
            Map<Integer, Set<Integer>> sumToEqSumElements,
            List<Integer> samplable,
            List<Vector> instances,
            int[] sums,
            boolean rowSwap,
            Random rnd) {

        // we select a row/col to select a row/col sum
        Pair<Integer, Integer> pair = samplePairOfIndices(sumToEqSumElements, samplable, sums, rnd);
        // column/row differences
        Set<Integer> S1 = Sets.newHashSet(instances.get(pair.getValue0()).getNonzeroIndices());
        Set<Integer> S2 = Sets.newHashSet(instances.get(pair.getValue1()).getNonzeroIndices());
        Set<Integer> S12 = S1.stream().filter(i -> S2.contains(i)).collect(Collectors.toSet());
        S1.removeAll(S12);
        S2.removeAll(S12);
        int num1 = S1.size();
        if (num1 == 0) {
            return null;
        }
        List<Integer> total = Lists.newArrayList(S1);
        total.addAll(S2);
        RandomSamplingCollector<Integer> collector = LiLSampling.collector(num1, rnd);
        List<Integer> L = IntStream.range(0, total.size())
                .boxed()
                .collect(collector)
                .stream()
                .map(i -> total.get(i))
                .collect(Collectors.toList());
        total.removeAll(L);
        L.addAll(S12);
        total.addAll(S12);
        return new SwappableLists(pair.getValue0(), pair.getValue1(), L, total, rowSwap);
    }
    
    private Pair<Integer, Integer> samplePairOfIndices(Map<Integer, Set<Integer>> sumToEqSum,
            List<Integer> samplable,
            int[] sums,
            Random rnd) {
        int e1 = samplable.get(rnd.nextInt(samplable.size()));
        int s = sums[e1];
        List<Integer> cands = Lists.newArrayList(sumToEqSum.get(s));
        if (cands.size() == 2) {
            return new Pair<>(cands.get(0), cands.get(1));
        }
        int e2;
        do {
            e2 = cands.get(rnd.nextInt(cands.size()));
        } while (e1 == e2);
        return new Pair<>(e1, e2);
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

    public double samplingProb(Edge swappableEdge1, Edge swappableEdge2) {

        double prob = 0.0;
        int rowSum1 = rowSums[swappableEdge1.row];
        int rowSum2 = rowSums[swappableEdge2.row];
        int colSum1 = colSums[swappableEdge1.col];
        int colSum2 = colSums[swappableEdge2.col];

        if (rowSum1 == rowSum2) {
            Vector v1 = getRowInstance(swappableEdge1.row);
            Vector v2 = getRowInstance(swappableEdge2.row);
            prob += getProb(rowSumToEqRowSumRows, v1, v2);
        }
        if (colSum1 == colSum2) {
            Vector v1 = getColInstance(swappableEdge1.col);
            Vector v2 = getColInstance(swappableEdge2.col);
            prob += getProb(colSumToEqColSumCols, v1, v2);
        }
        return prob;
    }

    private double getProb(Map<Integer, Set<Integer>> sumToEqSum,
            Vector e1,
            Vector e2) {
        int sumSwappablePairs = sumToEqSum.values()
                .stream()
                .mapToInt(l -> (int) CombinatoricsUtils.binomialCoefficient(l.size(), 2))
                .sum();
        Set<Integer> S1 = Sets.newHashSet(e1.getNonzeroIndices());
        S1.removeAll(e2.getNonzeroIndices());
        Set<Integer> S2 = Sets.newHashSet(e2.getNonzeroIndices());
        S2.removeAll(e1.getNonzeroIndices());
        int H12 = S1.size() * S2.size();
        return 1. / (2. * sumSwappablePairs * H12);
    }

    public double curveballSamplingProb(SwappableLists swappables, 
            List<Vector> rows, List<Vector> cols) {
        Vector v1, v2;
        Map<Integer, Set<Integer>> sumToEqSum;
        if (swappables.rowBased) {
            v1 = rows.get(swappables.swappable1);
            v2 = rows.get(swappables.swappable2);
            sumToEqSum = rowSumToEqRowSumRows;
        } else {
            v1 = cols.get(swappables.swappable1);
            v2 = cols.get(swappables.swappable2);
            sumToEqSum = colSumToEqColSumCols;
        }
        Set<Integer> S12 = v1.getNonzeroIndices().stream()
                .filter(i -> v2.getNonzeroIndices().contains(i))
                .collect(Collectors.toSet());
        int common = S12.size();
        int union = swappables.new2.size() + swappables.new1.size() - common;
        int l = swappables.new1.size() - common;
        double prob = getCurveBallProb(sumToEqSum, union, l);
        // case where |L| = 2
        if (l == 2) {
            Set<Integer> L = Sets.newHashSet(swappables.new1.size());
            L.removeAll(S12);
            List<Integer> pair = Lists.newArrayList(L);
            boolean equal;
            Vector v3, v4;
            if (swappables.rowBased) { 
                equal = colSums[pair.get(0)] == colSums[pair.get(1)];
                v3 = cols.get(pair.get(0));
                v4 = cols.get(pair.get(1));
                sumToEqSum = colSumToEqColSumCols;
            } else {
                equal = rowSums[pair.get(0)] == rowSums[pair.get(1)];
                v3 = rows.get(pair.get(0));
                v4 = rows.get(pair.get(1));
                sumToEqSum = rowSumToEqRowSumRows;
            }
            if (equal) {
                Set<Integer> S1 = Sets.newHashSet(v3.getNonzeroIndices());
                S1.removeAll(v4.getNonzeroIndices());
                Set<Integer> S2 = Sets.newHashSet(v4.getNonzeroIndices());
                S2.removeAll(v3.getNonzeroIndices());
                union = S1.size() + S2.size();
                prob += getCurveBallProb(sumToEqSum, union, l);
            }
        }
        return prob;
    }

    private double getCurveBallProb(Map<Integer, Set<Integer>> sumToEqSum, int union, int l) {
        int sumSwappablePairs = sumToEqSum.values()
                .stream()
                .mapToInt(lst -> (int) CombinatoricsUtils.binomialCoefficient(lst.size(), 2))
                .sum();
        int differentSubs = (int) CombinatoricsUtils.binomialCoefficient(union, l);
        return 1. / (2. * sumSwappablePairs * differentSubs);
    }

    public List<SwappableAndNewEdges> fromListToSwappables(SwappableLists swappables) {
        Vector v1, v2;
        if (swappables.rowBased) {
            v1 = getRowInstance(swappables.swappable1);
            v2 = getRowInstance(swappables.swappable2);
        } else {
            v1 = getColInstance(swappables.swappable1);
            v2 = getColInstance(swappables.swappable2);
        }
        Set<Integer> S1 = Sets.newHashSet(v1.getNonzeroIndices());
        Set<Integer> S2 = Sets.newHashSet(v2.getNonzeroIndices());
        Set<Integer> S12 = S1.stream().filter(i -> S2.contains(i)).collect(Collectors.toSet());
        Set<Integer> L = Sets.newHashSet(swappables.new1);
        L.removeAll(S12);
        L.removeAll(S1);
        Set<Integer> U = Sets.newHashSet(S1);
        U.addAll(S2);
        U.removeAll(L);
        U.removeAll(S2);
        assert(U.size()==L.size());
        List<Integer> new1 = Lists.newArrayList(L);
        List<Integer> new2 = Lists.newArrayList(U);
        List<SwappableAndNewEdges> edges = Lists.newArrayList();
        for (int c = 0; c < new1.size(); c++) {
            if (swappables.rowBased) {
                edges.add(new SwappableAndNewEdges(
                        new Edge(swappables.swappable1, new2.get(c)), 
                        new Edge(swappables.swappable2, new1.get(c)),
                        new Edge(swappables.swappable1, new1.get(c)),
                        new Edge(swappables.swappable2, new2.get(c))));
            } else {
                edges.add(new SwappableAndNewEdges(
                        new Edge(new2.get(c), swappables.swappable1), 
                        new Edge(new1.get(c), swappables.swappable2),
                        new Edge(new1.get(c), swappables.swappable1),
                        new Edge(new2.get(c), swappables.swappable2)));
            }
        }
        return edges;
    }
    
    /**
     * Gets the new rows of the matrix defined by the swappable and new edges.
     *
     * @param swappableEdge1 the first row
     * @param swappableEdge2 the second row
     * @param newEdge1 the first new edge to be added
     * @param newEdge2 the second new edge to be added
     * @return the two new rows of the matrix
     */
    public Vector[] getNewRows(Vector swappableEdge1, Vector swappableEdge2, Edge newEdge1, Edge newEdge2) {
        final Vector newRow1 = swappableEdge1.copy();
        newRow1.set(newEdge2.col, 0);
        newRow1.set(newEdge1.col, 1);

        final Vector newRow2 = swappableEdge2.copy();
        newRow2.set(newEdge1.col, 0);
        newRow2.set(newEdge2.col, 1);

        return new Vector[]{newRow1, newRow2};
    }

}
