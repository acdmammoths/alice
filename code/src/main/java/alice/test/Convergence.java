package alice.test;

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
import alice.structures.SparseMatrix;
import alice.config.Paths;
import alice.fpm.FreqItemsetMiner;
import alice.config.JsonKeys;
import alice.utils.JsonFile;
import alice.config.Delimiters;
import alice.samplers.CurveballBJDMSampler;
import alice.samplers.BJDMSampler;
import alice.samplers.GmmtSampler;
import alice.structures.Matrix;
import alice.utils.CMDLineParser;
import alice.utils.Config;
import alice.samplers.Sampler;
import alice.samplers.SelfLoopBJDMSampler;
import alice.utils.Transformer;
import alice.utils.Timer;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.javatuples.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class runs the convergence experiment by measuring the average relative
 * support/frequency difference for different values of the swap number
 * multiplier/factor.
 */
public class Convergence {

    public static void main(String[] args) {

        CMDLineParser.parse(args);

        final Sampler[] samplers = {new SelfLoopBJDMSampler(), new BJDMSampler(), new CurveballBJDMSampler()};

        System.out.println("Executing convergence experiment for dataset at " + Config.datasetPath);

        System.out.println("Mining frequent itemsets");
        final Object2IntOpenHashMap<IntOpenHashSet> freqItemsetToSup = FreqItemsetMiner.mine(Config.datasetPath, Config.minFreq);

        final int numFreqItemsets = freqItemsetToSup.size();
        System.out.println(JsonKeys.numFreqItemsets + ": " + freqItemsetToSup.size());

        final Transformer transformer = new Transformer();
        final SparseMatrix realMatrix = transformer.createMatrix(Config.datasetPath);
        System.out.println("Matrix created from dataset");
        
        final int numOnes = new Matrix(realMatrix).getNumEdges();
        System.out.println(JsonKeys.numOnes + ": " + numOnes);
        
        final Random rnd = new Random(Config.seed);

        final List<Double> numSwapsFactors = Lists.newArrayList();
        // use more granular factors at first
        for (double i = 0; i < 2; i += 0.15) {
            numSwapsFactors.add(i);
        }
        for (double i = 2; i <= Config.maxNumSwapsFactor; i++) {
            numSwapsFactors.add(i);
        }
        // create object for convergenceStats
        final JSONObject convergenceStats = new JSONObject();
        // file to store the JSON
        final String datasetBaseName = new Paths(Config.datasetPath, "").datasetBaseName;
        final String resultsBaseName
                = String.join(
                        Delimiters.dash,
                        datasetBaseName,
                        String.valueOf(Config.maxNumSwapsFactor),
                        String.valueOf(Config.minFreq),
                        String.valueOf(Config.seed));
        final String resultPath = Paths.getJsonFilePath(Config.resultsDir, resultsBaseName);
        
        for (Sampler sampler : samplers) {
            final String samplerName = sampler.getClass().getName();
            System.out.println(JsonKeys.sampler + ": " + samplerName);
            
            final JSONArray samplerConvergenceStats = new JSONArray();

            SparseMatrix startMatrix = realMatrix;
            
            for (int i = 0; i < numSwapsFactors.size(); i++) {
                final double numSwapsFactor = numSwapsFactors.get(i);
                System.out.println("\t" + JsonKeys.numSwapsFactor + ": " + numSwapsFactor);

                final double prevNumSwapsFactor = i > 0 ? numSwapsFactors.get(i - 1) : 0;
                final int numSwaps = (int) ((numSwapsFactor - prevNumSwapsFactor) * numOnes);
                System.out.println("\t\t" + JsonKeys.numSwaps + ": " + numSwaps);

                System.out.println("\t\tGetting sample from matrix");
                final Timer timer = new Timer(true);
                final long start = System.currentTimeMillis();
                final SparseMatrix sample = sampler.sample(startMatrix,
                        numSwaps,
                        rnd.nextLong(),
                        timer);
                final long end = System.currentTimeMillis() - start;
                final long setupTime = timer.getSavedTime();
                final double minStepTime = timer.getMin();
                final double c10StepTime = timer.getPercentile(10);
                final double q1StepTime = timer.getPercentile(25);
                final double medianStepTime = timer.getPercentile(50);
                final double q3StepTime = timer.getPercentile(75);
                final double c90StepTime = timer.getPercentile(90);
                final double maxStepTime = timer.getMax();

                System.out.println("\t\t" + JsonKeys.minStepTime + ": " + minStepTime);
                System.out.println("\t\t" + JsonKeys.c10StepTime + ": " + c10StepTime);
                System.out.println("\t\t" + JsonKeys.q1StepTime + ": " + q1StepTime);
                System.out.println("\t\t" + JsonKeys.medianStepTime + ": " + medianStepTime);
                System.out.println("\t\t" + JsonKeys.q3StepTime + ": " + q3StepTime);
                System.out.println("\t\t" + JsonKeys.c90StepTime + ": " + c90StepTime);
                System.out.println("\t\t" + JsonKeys.maxStepTime + ": " + maxStepTime);

                System.out.println("\t\tGetting sample itemset to support map");
                Map<IntOpenHashSet, Integer> sampleFreqItemsetToSup = getItemsetToSupMap(sample, transformer, freqItemsetToSup.keySet());
                // compute convergence statistics
                final double avgRelFreqDiff = getAvgRelFreqDiff(freqItemsetToSup, sampleFreqItemsetToSup);
                System.out.println("\t\t" + JsonKeys.avgRelFreqDiff + ": " + avgRelFreqDiff);

                // create object for factorConvergenceStats
                final JSONObject factorConvergenceStats = new JSONObject();
                factorConvergenceStats.put(JsonKeys.numSwapsFactor, numSwapsFactor);
                factorConvergenceStats.put(JsonKeys.avgRelFreqDiff, avgRelFreqDiff);
                factorConvergenceStats.put(JsonKeys.setupTime, setupTime);
                factorConvergenceStats.put(JsonKeys.minStepTime, minStepTime);
                factorConvergenceStats.put(JsonKeys.c10StepTime, c10StepTime);
                factorConvergenceStats.put(JsonKeys.q1StepTime, q1StepTime);
                factorConvergenceStats.put(JsonKeys.medianStepTime, medianStepTime);
                factorConvergenceStats.put(JsonKeys.q3StepTime, q3StepTime);
                factorConvergenceStats.put(JsonKeys.c90StepTime, c90StepTime);
                factorConvergenceStats.put(JsonKeys.maxStepTime, maxStepTime);
                factorConvergenceStats.put(JsonKeys.totalTime, end);

                samplerConvergenceStats.put(factorConvergenceStats);

                // run the chain again starting from the last sample
                startMatrix = sample;
            }
            convergenceStats.put(samplerName, samplerConvergenceStats);
        }

        // create object for runInfo
        final JSONObject runInfo = new JSONObject();
        runInfo.put(JsonKeys.timestamp, LocalDateTime.now());
        runInfo.put(JsonKeys.numOnes, numOnes);
        runInfo.put(JsonKeys.numFreqItemsets, numFreqItemsets);

        // create base object
        final JSONObject results = new JSONObject();
        results.put(JsonKeys.runInfo, runInfo);
        results.put(JsonKeys.convergenceStats, convergenceStats);

        // save JSON
        JsonFile.write(results, resultPath);
        System.out.println("Result written to " + resultPath);
    }

    /**
     * Gets the sample's itemset to support map such that the map only contains
     * itemsets that are frequent itemsets of the observed dataset.
     *
     * @param sample the matrix representation of the sampled dataset
     * @param transformer the transformer used to create the matrix of the
     * sample
     * @param freqItemsets the set of frequent itemsets in the observed dataset
     * @return a map where each key is an itemset and the value is the itemset's
     * support in the sampled dataset such that the map only contains itemsets
     * that are frequent itemsets of the observed dataset
     */
    public static Object2IntOpenHashMap<IntOpenHashSet> getSampleItemsetToSupMap(
            SparseMatrix sample, Transformer transformer, ObjectSet<IntOpenHashSet> freqItemsets) {
        final Object2IntOpenHashMap<IntOpenHashSet> sampleItemsetToSup = new Object2IntOpenHashMap();
        for (int r = 0; r < sample.getNumRows(); r++) {
            for (IntOpenHashSet freqItemset : freqItemsets) {
                if (isItemsetInSampleTransaction(freqItemset, sample, r, transformer.getItemToColIndex())) {
                    sampleItemsetToSup.put(freqItemset, sampleItemsetToSup.getOrDefault(freqItemset, 0) + 1);
                }
            }
        }
        return sampleItemsetToSup;
    }
    
    public static Map<IntOpenHashSet, Integer> getItemsetToSupMap(
            SparseMatrix sample, Transformer transformer, ObjectSet<IntOpenHashSet> freqItemsets) {
        return freqItemsets.parallelStream().map(freqItemset -> 
                new Pair<IntOpenHashSet, Integer>(freqItemset, getItemsetInSampleCount(sample, transformer.getItemToColIndex(), freqItemset)))
                .collect(Collectors.toMap(e -> e.getValue0(),e -> e.getValue1()));
    }
    
    public static int getItemsetInSampleCount(SparseMatrix sample,
            Map<Integer, Integer> itemToColIndex,
            IntOpenHashSet itemset) {

        final int firstV = itemToColIndex.get(itemset.iterator().nextInt());
        IntOpenHashSet tmp = new IntOpenHashSet(sample.getNonzeroColIndices(firstV));
        if (itemset.size() > 1) {
            for (int item : itemset) {
                tmp.retainAll(sample.getNonzeroColIndices(itemToColIndex.get(item)));
                if (tmp.isEmpty()) {
                    return 0;
                }
            }
        }
        return tmp.size();
    }
    
    /**
     * Determines whether the itemset is in the transaction of the sample
     * specified by rowIndex.
     *
     * @param itemset the itemset
     * @param sample the sample
     * @param rowIndex the row index that corresponds to the transaction
     * @param itemToColIndex a map where each key is an item and the value is
     * the item's column index in the sample matrix
     * @return whether the itemset is in the transaction of the sample
     */
    public static boolean isItemsetInSampleTransaction(
            IntOpenHashSet itemset,
            SparseMatrix sample,
            int rowIndex,
            Map<Integer, Integer> itemToColIndex) {

        return itemset.intStream()
                .allMatch(item -> sample.isInRow(rowIndex, itemToColIndex.get(item)) != 0);
    }
    
    /**
     * Gets the average relative frequency/support difference.
     *
     * @param freqItemsetToSup a map where each key is a frequent itemset in the
     * observed dataset and the value is the frequent itemset's support
     * @param sampleItemsetToSup a map where each key is an itemset and the
     * value is the itemset's support in the sampled dataset such that the map
     * only contains itemsets that are frequent itemsets of the observed dataset
     * @return the average relative frequency/support difference
     */
    public static double getAvgRelFreqDiff(
            Object2IntOpenHashMap<IntOpenHashSet> freqItemsetToSup,
            Map<IntOpenHashSet, Integer> sampleItemsetToSup) {

        double sumRelFreqDiff = freqItemsetToSup.object2IntEntrySet()
                .stream()
                .mapToDouble(entry
                        -> Math.abs(1. * entry.getIntValue() - sampleItemsetToSup.getOrDefault(entry.getKey(), 0))
                / entry.getIntValue())
                .sum();
        return sumRelFreqDiff / freqItemsetToSup.size();
    }

}
