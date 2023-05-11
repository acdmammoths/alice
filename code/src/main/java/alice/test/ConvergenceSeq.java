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
import alice.config.Paths;
import alice.config.JsonKeys;
import alice.utils.JsonFile;
import alice.config.Delimiters;
import alice.samplers.AliceCSampler;
import alice.samplers.GmmtSeqSampler;
import alice.samplers.Sampler;
import alice.samplers.SeqSampler;
import alice.spm.FreqSequenceMiner;
import alice.spm.Itemset;
import alice.spm.SequentialPattern;
import alice.spm.SequentialPatterns;
import alice.utils.CMDLineParser;
import alice.utils.Config;
import alice.structures.MultiGraph;
import alice.structures.SparseMatrix;
import alice.utils.Transformer;
import alice.utils.Timer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
public class ConvergenceSeq {

    public static void main(String[] args) throws IOException {

        CMDLineParser.parse(args);

        final SeqSampler[] samplers = new SeqSampler[]{new AliceCSampler(), new GmmtSeqSampler()};

        System.out.println("Executing convergence experiment for dataset at " + Config.datasetPath);

        System.out.println("Mining frequent itemsets");
        final SequentialPatterns freqItemsetToSup = FreqSequenceMiner.mine(Config.datasetPath, Config.minFreq);

        final int numFreqItemsets = freqItemsetToSup.sequenceCount;
        System.out.println(JsonKeys.numFreqItemsets + ": " + numFreqItemsets);

        final Transformer transformer = new Transformer();
        final MultiGraph realMatrix = transformer.createMultiGraph(Config.datasetPath);
        System.out.println("Matrix created from dataset");
        
        final int numOnes = realMatrix.getNumEdges();
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
        
                
        for (SeqSampler sampler : samplers) {
            final String samplerName = sampler.getClass().getName();
            System.out.println(JsonKeys.sampler + ": " + samplerName);

            final JSONArray samplerConvergenceStats = new JSONArray();

            MultiGraph startMatrix = realMatrix;

            for (int i = 0; i < numSwapsFactors.size(); i++) {
                final double numSwapsFactor = numSwapsFactors.get(i);
                System.out.println("\t" + JsonKeys.numSwapsFactor + ": " + numSwapsFactor);

                final double prevNumSwapsFactor = i > 0 ? numSwapsFactors.get(i - 1) : 0;
                final int numSwaps = (int) ((numSwapsFactor - prevNumSwapsFactor) * numOnes);
                System.out.println("\t\t" + JsonKeys.numSwaps + ": " + numSwaps);

                System.out.println("\t\tGetting sample from matrix");
                final Timer timer = new Timer(true);
                final long start = System.currentTimeMillis();
                final MultiGraph sample = sampler.sample(startMatrix,
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
                final String[][] dataset = transformer.createSequenceDataset(sample);
                final SequentialPatterns sampleFreqItemset = FreqSequenceMiner.mine(dataset, Config.minFreq);
                final Object2IntOpenHashMap<String> sampleFreqItemsetToSup
                        = getSampleItemsetToSupMap(freqItemsetToSup, sampleFreqItemset);
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
    private static Object2IntOpenHashMap<String> getSampleItemsetToSupMap(
    		SequentialPatterns freqItemsets, SequentialPatterns sampItemsets) {
    	
        final Object2IntOpenHashMap<String> sampleItemsetToSup = new Object2IntOpenHashMap();
        for (int level = 0; level < freqItemsets.getLevelCount(); level++) {
            for (SequentialPattern freqItemset : freqItemsets.getLevel(level)) {
            	sampleItemsetToSup.put(freqItemset.toSave(), 0);
            }
        }
        for (int level = 0; level < sampItemsets.getLevelCount(); level++) {
            for (SequentialPattern freqItemset : sampItemsets.getLevel(level)) {
            	String name = freqItemset.toSave();
            	if (sampleItemsetToSup.containsKey(name)) {
            		sampleItemsetToSup.replace(name, freqItemset.getAbsoluteSupport());
            	}
            }
        }
        return sampleItemsetToSup;
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
    private static double getAvgRelFreqDiff(
            SequentialPatterns freqItemsetToSup,
            Object2IntOpenHashMap<String> sampleItemsetToSup) {

        double sumRelFreqDiff = 0;
        for (int level = 0; level < freqItemsetToSup.getLevelCount(); level++) {
            List<SequentialPattern> patterns = freqItemsetToSup.getLevel(level);
            for (SequentialPattern freqItemset : patterns) {
                String patternName = freqItemset.toSave();
                double count = 1. * freqItemset.getSequenceIDs().size();
                sumRelFreqDiff += Math.abs(count - sampleItemsetToSup.getInt(patternName)) / count;
            }
        }
        return sumRelFreqDiff / freqItemsetToSup.sequenceCount;
    }
}
