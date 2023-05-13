package alice.structures;

import alice.helpers.Swappables;
import alice.helpers.SwappableLists;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
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

/*
 * Copyright (C) 2022 Giulia
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
/**
 * A class to represent the adjacency matrix of the bipartite graph representation
 * of a transactional dataset.
 */
public class BJDMMatrix extends SetMatrix {

    /**
     * A map where each key is a row sum and the value is the unique set of rows
     * with that row sum.
     */
    private final Map<Integer, Set<Vector>> rowSumToUniqueRows;

    /**
     * A map where each key is a row sum and the value is the set of rows with
     * that row sum.
     */
    private final Map<Integer, List<Integer>> rowSumToEqRowSumRows;

    /**
     * A map where each key is a col sum and the value is the set of cols with
     * that col sum.
     */
    private final Map<Integer, List<Integer>> colSumToEqColSumCols;
    
    /**
     * ids of rows such that there exists at least two rows with the same rowSum.
     */
    private final int[] samplableRows;
    
    /**
     * ids of cols such that there exists at least two cols with the same colSum.
     */
    private final int[] samplableCols;

    /**
     * Creates an instance of {@link BJDMMatrix} from a 0-1
     * {@link SparseMatrix} by initializing necessary data structures from the
     * matrix.
     *
     * @param inMatrix a 0-1 matrix representation of the dataset
     */
    public BJDMMatrix(SparseMatrix inMatrix) {
        super(inMatrix);
        this.rowSumToUniqueRows = Maps.newHashMap();
        for (int r = 0; r < inMatrix.getNumRows(); r++) {
            final Vector row = inMatrix.getRowInstance(r);
            final int rowSum = this.getRowSum(r);
            incNumEqRows(row);
            addEqRowSumUniqueRow(rowSum, row);
        }
        // initialize row sum map and samplable rows
        this.rowSumToEqRowSumRows = IntStream.range(0, rowSums.length)
                .boxed()
                .collect(Collectors.groupingBy(r -> rowSums[r], Collectors.toList()));
        List<Integer> tmpR = rowSumToEqRowSumRows
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
        if (!tmpR.isEmpty()) {
            this.samplableRows = Ints.toArray(tmpR);
        } else {
            this.samplableRows = null;
        }
        // initialize col sum map and samplable columns
        this.colSumToEqColSumCols = IntStream.range(0, colSums.length)
                .boxed()
                .collect(Collectors.groupingBy(c -> colSums[c], Collectors.toList()));
        List<Integer> tmpC = colSumToEqColSumCols
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
        if (!tmpC.isEmpty()) {
            this.samplableCols = Ints.toArray(tmpC);
        } else {
           this.samplableCols = null; 
        }
    }

    /**
     * 
     * @param rowSum row sum
     * @return distinct rows with row sum equal to rowSum
     */
    public Set<Vector> getEqRowSumUniqueRows(int rowSum) {
        return this.rowSumToUniqueRows.getOrDefault(rowSum, Sets.newHashSet());
    }

    /**
     * Adds the row to the set of distinct rows with row sum rowSum
     * @param rowSum row sum
     * @param row vector representing a row
     */
    private void addEqRowSumUniqueRow(int rowSum, Vector row) {
        Set<Vector> uniqueRows = getEqRowSumUniqueRows(rowSum);
        if (!uniqueRows.contains(row)) {
            row = row.copy();
            uniqueRows.add(row);
            this.rowSumToUniqueRows.put(rowSum, uniqueRows);
        }
    }

    /**
     * Gets the log of the number of matrices in the chain that are equivalent
     * to the current matrix (i.e., matrices that represent the same dataset as
     * the current matrix).
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
     * input row sum (transaction length).
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
     * input row sum.
     *
     * @param rowSum the input row sum
     * @return the log of the number of rows that have an equal row sum
     */
    public double getLogNumEqRowSumRowsFac(int rowSum) {
        final int numEqRowSumRows = rowSumToEqRowSumRows.getOrDefault(rowSum, Lists.newArrayList()).size();
        return IntStream.range(0, numEqRowSumRows)
                .mapToDouble(i -> Math.log(numEqRowSumRows - i))
                .sum();
    }

    /**
     * Gets the log of the number rows that are equal to the input row
     * factorial.
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
     * the adjacent matrix).
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
    
    /**
     * Gets the log of the number of matrices in the chain that are equivalent
     * to the adjacent matrix (i.e., matrices that represent the same dataset as
     * the adjacent matrix).
     *
     * @param logNumEquivMatrices the log of the number of equivalent matrices
     * for the current matrix
     * @param swappableRow1 the first swappable row
     * @param swappableRow2 the second swappable row
     * @param newRow1 the first new row
     * @param newRow2 the second new row
     * @param rowToEqRows for each row, the ids of the rows equivalent to that row
     * @return the log of the number of equivalent adjacent matrices
     */
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
        int s1, s2, n1, n2;
        if (rowToEqRows.containsKey(swappableRow1)) {
            s1 = rowToEqRows.get(swappableRow1);
        } else {
            s1 = getNumEqRows(swappableRow1);
            swappableRow1 = swappableRow1.copy();
        }
        if (rowToEqRows.containsKey(swappableRow2)) {
            s2 = rowToEqRows.get(swappableRow2);
        } else {
            s2 = getNumEqRows(swappableRow2);
            swappableRow2 = swappableRow2.copy();
        }
        if (rowToEqRows.containsKey(newRow1)) {
            n1 = rowToEqRows.get(newRow1);
        } else {
            n1 = getNumEqRows(newRow1);
        }
        if (rowToEqRows.containsKey(newRow2)) {
            n2 = rowToEqRows.get(newRow2);
        } else {
            n2 = getNumEqRows(newRow2);
        }
        rowToEqRows.put(swappableRow1, s1 - 1);
        rowToEqRows.put(swappableRow2, s2 - 1);
        rowToEqRows.put(newRow1, n1 + 1);
        rowToEqRows.put(newRow2, n2 + 1);
        return logNumEquivMatrices + Math.log(s1) + Math.log(s2)
                - Math.log1p(n1) - Math.log1p(n2);
    }

    /**
     * ALICE-A Sampling Strategy: flips a coin to decide whether two rows or two 
     * columns with equal sum should be sampled. 
     * Then, it samples two edges to swap. 
     * 
     * @param rnd random object
     * @return the two edges to swap
     */
    public Swappables getSwappables(Random rnd) {
        // sample rows or columns
        boolean rowSwap = rnd.nextBoolean();
        Map<Integer, List<Integer>> sumToEqSumElements;
        int[] samplable;
        Vector[] instances;
        int[] sums;
        if (rowSwap) {
            sumToEqSumElements = rowSumToEqRowSumRows;
            samplable = samplableRows;
            instances = getRows();
            sums = rowSums;
        } else {
            sumToEqSumElements = colSumToEqColSumCols;
            samplable = samplableCols;
            instances = getCols();
            sums = colSums;
        }
        if (samplable == null) {
            return null;
        }
        // we select a row/col to select a row/col sum
        Pair<Integer, Integer> pair = samplePairOfIndices(sumToEqSumElements, samplable, sums, rnd);
        // column/row differences
        Set<Integer> S1 = Sets.newHashSet(instances[pair.getValue0()].getNonzeroIndices());
        S1.removeAll(instances[pair.getValue1()].getNonzeroIndices());
        Set<Integer> S2 = Sets.newHashSet(instances[pair.getValue1()].getNonzeroIndices());
        S2.removeAll(instances[pair.getValue0()].getNonzeroIndices());
        if (S1.isEmpty()) {
            // self loop
            return null;
        }
        // select column/row pair
        int f1 = S1.stream().skip(rnd.nextInt(S1.size())).findFirst().get();
        int f2 = S2.stream().skip(rnd.nextInt(S2.size())).findFirst().get();
        Edge sampledEdge1;
        Edge sampledEdge2;
        if (rowSwap) {
            sampledEdge1 = new Edge(pair.getValue0(), f1);
            sampledEdge2 = new Edge(pair.getValue1(), f2);
        } else {
            sampledEdge1 = new Edge(f1, pair.getValue0());
            sampledEdge2 = new Edge(f2, pair.getValue1());
        }
        return new Swappables(sampledEdge1, sampledEdge2, 0, 0);
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
                edges.contains(new Edge(first.row, second.col)) || edges.contains(new Edge(second.row, first.col)) ||
                (rowSums[first.row] != rowSums[second.row] && colSums[first.col] != colSums[second.col]));
    }
    
    /**
     * Method used for validation.
     * 
     * @param rnd
     * @param rowSwap whether we swap rows or columns
     * @return the two edges to swap
     */
    public Swappables getSwappables(Random rnd, boolean rowSwap) {
        // sample rows or columns
        Map<Integer, List<Integer>> sumToEqSumElements;
        int[] samplable;
        Vector[] instances;
        int[] sums;
        if (rowSwap) {
            sumToEqSumElements = rowSumToEqRowSumRows;
            samplable = samplableRows;
            instances = getRows();
            sums = rowSums;
        } else {
            sumToEqSumElements = colSumToEqColSumCols;
            samplable = samplableCols;
            instances = getCols();
            sums = colSums;
        }
        if (samplable == null) {
            return null;
        }
        // we select a row/col to select a row/col sum
        Pair<Integer, Integer> pair = samplePairOfIndices(sumToEqSumElements, samplable, sums, rnd);
        // column/row differences
        Set<Integer> S1 = Sets.newHashSet(instances[pair.getValue0()].getNonzeroIndices());
        S1.removeAll(instances[pair.getValue1()].getNonzeroIndices());
        Set<Integer> S2 = Sets.newHashSet(instances[pair.getValue1()].getNonzeroIndices());
        S2.removeAll(instances[pair.getValue0()].getNonzeroIndices());
        List<Integer> candC1 = Lists.newArrayList(S1);
        List<Integer> candC2 = Lists.newArrayList(S2);
        if (S1.isEmpty()) {
            // self loop
            return null;
        }
        // select column/row pair
        int f1 = candC1.get(rnd.nextInt(candC1.size()));
        int f2 = candC2.get(rnd.nextInt(candC2.size()));
        Edge sampledEdge1;
        Edge sampledEdge2;
        if (rowSwap) {
            sampledEdge1 = new Edge(pair.getValue0(), f1);
            sampledEdge2 = new Edge(pair.getValue1(), f2);
        } else {
            sampledEdge1 = new Edge(f1, pair.getValue0());
            sampledEdge2 = new Edge(f2, pair.getValue1());
        }
        return new Swappables(sampledEdge1, sampledEdge2, 0, 0);
    }
    
    /**
     * ALICE-B Sampling Strategy: flips a coin to decide whether two rows or 
     * two columns with equal sum should be sampled. 
     * Then, it selects a set of elements to swap.
     * 
     * @param rnd random object
     * @return the two indices of the row/col sampled, and the new elements in the
     * corresponding vectors.
     */
    public SwappableLists getSwappablesNewEdges(Random rnd) {
        // sample rows or columns
        boolean rowSwap = rnd.nextBoolean();
        Map<Integer, List<Integer>> sumToEqSumElements;
        int[] samplable;
        Vector[] instances;
        int[] sums;
        if (rowSwap) {
            sumToEqSumElements = rowSumToEqRowSumRows;
            samplable = samplableRows;
            instances = getRows();
            sums = rowSums;
        } else {
            sumToEqSumElements = colSumToEqColSumCols;
            samplable = samplableCols;
            instances = getCols();
            sums = colSums;
        }
        if (samplable == null) {
            return null;
        }
        // we select a row/col to select a row/col sum
        Pair<Integer, Integer> pair = samplePairOfIndices(sumToEqSumElements, samplable, sums, rnd);
        // column/row differences
        Set<Integer> S1 = Sets.newHashSet(instances[pair.getValue0()].getNonzeroIndices());
        Set<Integer> S2 = Sets.newHashSet(instances[pair.getValue1()].getNonzeroIndices());
        Set<Integer> S12 = S1.stream().filter(i -> S2.contains(i)).collect(Collectors.toSet());
        S1.removeAll(S12);
        S2.removeAll(S12);
        int num1 = S1.size();
        if (num1 == 0) {
            return null;
        }
        Set<Integer> tmp = Sets.newHashSet(S1);
        tmp.addAll(S2);
        List<Integer> total = Lists.newArrayList(tmp);
        RandomSamplingCollector<Integer> collector = LiLSampling.collector(num1, rnd);
        List<Integer> L = IntStream.range(0, total.size())
                .boxed()
                .collect(collector)
                .stream()
                .map(i -> total.get(i))
                .collect(Collectors.toList());
        tmp.removeAll(L);
        L.addAll(S12);
        tmp.addAll(S12);
        return new SwappableLists(pair.getValue0(), pair.getValue1(), L, Lists.newArrayList(tmp), rowSwap);
    }
    
    /**
     * Method used for validation.
     * 
     * @param rnd random object
     * @param rowSwap whether we swap rows or columns
     * @return the two indices of the row/col sampled, and the new elements in the
     * corresponding vectors.
     */
    public SwappableLists getSwappablesNewEdges(Random rnd, boolean rowSwap) {
        // sample rows or columns
        Map<Integer, List<Integer>> sumToEqSumElements;
        int[] samplable;
        Vector[] instances;
        int[] sums;
        if (rowSwap) {
            sumToEqSumElements = rowSumToEqRowSumRows;
            samplable = samplableRows;
            instances = getRows();
            sums = rowSums;
        } else {
            sumToEqSumElements = colSumToEqColSumCols;
            samplable = samplableCols;
            instances = getCols();
            sums = colSums;
        }
        if (samplable == null) {
            return null;
        }
        // we select a row/col to select a row/col sum
        Pair<Integer, Integer> pair = samplePairOfIndices(sumToEqSumElements, samplable, sums, rnd);
        // column/row differences
        Set<Integer> S1 = Sets.newHashSet(instances[pair.getValue0()].getNonzeroIndices());
        Set<Integer> S2 = Sets.newHashSet(instances[pair.getValue1()].getNonzeroIndices());
        Set<Integer> S12 = S1.stream().filter(i -> S2.contains(i)).collect(Collectors.toSet());
        S1.removeAll(S12);
        S2.removeAll(S12);
        int num1 = S1.size();
        if (num1 == 0) {
            return null;
        }
        Set<Integer> tmp = Sets.newHashSet(S1);
        tmp.addAll(S2);
        List<Integer> total = Lists.newArrayList(tmp);
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

    /**
     * 
     * @param sumToEqSum sum to list of rows/cols with that sum
     * @param samplable list of samplable rows/cols
     * @param sums sum for each row/col
     * @param rnd random object
     * @return a pair of distinct rows/cols with the same row/col sum
     */
    private Pair<Integer, Integer> samplePairOfIndices(
            Map<Integer, List<Integer>> sumToEqSum,
            int[] samplable,
            int[] sums,
            Random rnd) {
        
        int e1 = samplable[rnd.nextInt(samplable.length)];
        int s = sums[e1];
        int numElems = sumToEqSum.get(s).size();
        if (numElems == 2) {
            return new Pair<>(sumToEqSum.get(s).get(0), sumToEqSum.get(s).get(1));
        }
        int e2;
        do {
            e2 = sumToEqSum.get(s).get(rnd.nextInt(numElems));
        } while (e1 == e2);
        return new Pair<>(e1, e2);
    }

    /**
     * 
     * @param swappableEdge1 first edge to swap
     * @param swappableEdge2 second edge to swap
     * @return probability of sampling the adjacent matrix where the two edges are swapped.
     */
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

    /**
     * 
     * @param sumToEqSum for each sum, ids of rows/cols with that sum
     * @param e1 first row/col
     * @param e2 second row/col
     * @return probability of selecting e1 and e2
     */
    private double getProb(Map<Integer, List<Integer>> sumToEqSum,
            Vector e1,
            Vector e2) {
        int sumSwappablePairs = sumToEqSum.values()
                .stream()
                .mapToInt(l -> getNumCombinations(l.size(), 2))
                .sum();
        Set<Integer> S1 = Sets.newHashSet(e1.getNonzeroIndices());
        S1.removeAll(e2.getNonzeroIndices());
        Set<Integer> S2 = Sets.newHashSet(e2.getNonzeroIndices());
        S2.removeAll(e1.getNonzeroIndices());
        int H12 = S1.size() * S2.size();
        return 1. / (2. * sumSwappablePairs * H12);
    }
    
    /**
     * 
     * @param n number of elements in the universe
     * @param k number of element in the combination
     * @return number of distinct combinations of k elements from the universe
     */
    private int getNumCombinations(int n, int k) {
        if (n < k) {
            return 0;
        }
        return (int) CombinatoricsUtils.binomialCoefficient(n, k);
    }

    /**
     * 
     * @param swappables elements to swap obtained via curveball sampling
     * @param rows list of rows
     * @param cols list of columns
     * @return probability of selecting the adjacent matrix where the elements 
     * in swappables are swapped 
     */
    public double curveballSamplingProb(SwappableLists swappables, 
            List<Vector> rows, List<Vector> cols) {
        Vector v1, v2;
        Map<Integer, List<Integer>> sumToEqSum;
        if (swappables.rowBased) {
            v1 = rows.get(swappables.swappable1);
            v2 = rows.get(swappables.swappable2);
            sumToEqSum = rowSumToEqRowSumRows;
        } else {
            v1 = cols.get(swappables.swappable1);
            v2 = cols.get(swappables.swappable2);
            sumToEqSum = colSumToEqColSumCols;
        }
        Set<Integer> S12 = v1.getNonzeroIndices().intStream()
                .filter(i -> v2.getNonzeroIndices().contains(i))
                .boxed()
                .collect(Collectors.toSet());
        int common = S12.size();
        int union = swappables.new2.size() + swappables.new1.size() - 2 * common;
        int l = swappables.new1.size() - common;
        double prob = getCurveBallProb(sumToEqSum, union, l);
        // case where |L| = 2
        if (l == 1) {
            Set<Integer> L = Sets.newHashSet(swappables.new1);
            Set<Integer> R = Sets.newHashSet(swappables.new2);
            L.removeAll(S12);
            R.removeAll(S12);
            int first = L.iterator().next();
            int second = R.iterator().next();
            boolean equal;
            Vector v3, v4;
            if (swappables.rowBased) { 
                equal = colSums[first] == colSums[second];
                v3 = cols.get(first);
                v4 = cols.get(second);
                sumToEqSum = colSumToEqColSumCols;
            } else {
                equal = rowSums[first] == rowSums[second];
                v3 = rows.get(first);
                v4 = rows.get(second);
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

    /**
     * 
     * @param sumToEqSum for each sum, ids of rows/cols with that sum
     * @param union int
     * @param l int
     * @return probability of selecting l given union
     */
    private double getCurveBallProb(Map<Integer, List<Integer>> sumToEqSum, int union, int l) {
        int sumSwappablePairs = sumToEqSum.values()
                .stream()
                .mapToInt(lst -> getNumCombinations(lst.size(), 2))
                .sum();
        int differentSubs = getNumCombinations(union, l);
        return 1. / (2. * sumSwappablePairs * differentSubs);
    }

    /**
     * Creates a list of SwappableAndNewEdges from a list of elements to swap.
     * 
     * @param swappables elements to swap obtained via curveball sampling
     * @return list of edges to swap obtained from the set of elements to swap
     */
    public List<Swappables> fromListToSwappables(SwappableLists swappables) {
        Vector v1, v2;
        if (swappables.rowBased) {
            v1 = getRowInstance(swappables.swappable1);
            v2 = getRowInstance(swappables.swappable2);
        } else {
            v1 = getColInstance(swappables.swappable1);
            v2 = getColInstance(swappables.swappable2);
        }
        Set<Integer> L = Sets.newHashSet(swappables.new1);
        L.removeAll(v1.getNonzeroIndices());
        Set<Integer> U = Sets.newHashSet(swappables.new2);
        U.removeAll(v2.getNonzeroIndices());
        assert(U.size()==L.size());
        List<Integer> new1 = Lists.newArrayList(L);
        List<Integer> new2 = Lists.newArrayList(U);
        List<Swappables> E = Lists.newArrayList();
        for (int c = 0; c < new1.size(); c++) {
            if (swappables.rowBased) {
                E.add(new Swappables(
                        new Edge(swappables.swappable1, new2.get(c)), 
                        new Edge(swappables.swappable2, new1.get(c)), 0, 0));
            } else {
                E.add(new Swappables(
                        new Edge(new1.get(c), swappables.swappable2),
                        new Edge(new2.get(c), swappables.swappable1), 0, 0));
            }
        }
        return E;
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
    
    /**
     * 
     * @return number of paths of length 3 in this graph
     */
    public long getNumCaterpillars() {
        return edges
                .parallelStream()
                .mapToLong(edge -> (this.getRowSum(edge.row) - 1) * (this.getColSum(edge.col) - 1))
                .sum();
    }

}
