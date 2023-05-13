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
import alice.samplers.CurveballBJDMSampler;
import alice.samplers.BJDMSampler;
import alice.utils.CMDLineParser;
import alice.utils.Config;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import alice.fpm.FreqItemsetMiner;
import alice.fpm.SampleAndMiner;
import alice.samplers.GmmtSampler;
import alice.samplers.Sampler;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class runs the number of frequent itemsets experiment by measuring the
 * quartiles for the number of frequent itemsets across the sampled distribution
 * of sets of frequent itemsets as well as measure the p-value for the number of
 * frequent itemsets in the observed dataset.
 */
public class NumFreqItemsets {

    public static void main(String[] args) {
        
        CMDLineParser.parse(args);

        System.out.println("Executing number of frequent itemsets experiment");
        final ObjectSet<IntOpenHashSet> observedFreqItemsets
                = FreqItemsetMiner.mine(Config.datasetPath, Config.minFreq).keySet();

        final int observedNumFreqItemsets = observedFreqItemsets.size();
        System.out.println(JsonKeys.numFreqItemsets + ": " + observedNumFreqItemsets);

        final Int2IntOpenHashMap observedFreqItemsetLenToCount
                = getFreqItemsetLenToCountMap(observedFreqItemsets);
        System.out.println(JsonKeys.freqItemsetLenToCount + ": " + observedFreqItemsetLenToCount);

        final Sampler[] samplers = {
            new GmmtSampler(),
            new BJDMSampler(), 
            new CurveballBJDMSampler() 
        };

        // create object for numFreqItemsetsStats
        final JSONArray numFreqItemsetsStats = new JSONArray();

        for (Sampler sampler : samplers) {
            final String samplerName = sampler.getClass().getName();
            System.out.println(JsonKeys.sampler + ": " + samplerName);

            final String samplerResultsDir = Paths.concat(Config.resultsDir, samplerName);
            final Paths paths = new Paths(Config.datasetPath, samplerResultsDir);
            final String freqItemsetsSamplesDir = paths.freqItemsetsDirPath;

            if (Config.sampleAndMine) {
                System.out.println("Sampling and mining");
                int numSwaps = Config.numSwaps;
                if (samplerName.contains("SelfLoop")) {
                    numSwaps *= 50;
                    System.out.println(numSwaps);
                }
                SampleAndMiner.sampleAndMine(
                        Config.datasetPath,
                        sampler,
                        numSwaps,
                        Config.numSamples,
                        Config.minFreq,
                        Config.numThreads,
                        Config.seed,
                        samplerResultsDir);
            } else {
                System.out.println("Skipping sampling and mining");
            }

            final List<Integer> numFreqItemsetsDist = Lists.newArrayList();
            final Map<Integer, List<Integer>> freqItemsetLenToCountDist = Maps.newHashMap();
            final File[] freqItemsetsSamples = new File(freqItemsetsSamplesDir).listFiles();

            for (File freqItemsetsSample : freqItemsetsSamples) {
                final Int2IntOpenHashMap freqItemsetLenToCount
                        = getFreqItemsetLenToCountMap(freqItemsetsSample);
                final int numFreqItemsets = getNumFreqItemsets(freqItemsetLenToCount);
                numFreqItemsetsDist.add(numFreqItemsets);
                updateFreqItemsetLenToCountDist(freqItemsetLenToCountDist, freqItemsetLenToCount);
            }

            System.out.println(JsonKeys.numSamples + ": " + numFreqItemsetsDist.size());

            final Map<Integer, List<Integer>> freqItemsetLenToCountQuartiles
                    = getFreqItemsetLenToCountQuartiles(freqItemsetLenToCountDist);
            System.out.println(
                    JsonKeys.freqItemsetLenToCountQuartiles + ": " + freqItemsetLenToCountQuartiles);

            final List<Integer> numFreqItemsetsQuartiles = getQuartiles(numFreqItemsetsDist);
            System.out.println(JsonKeys.numFreqItemsetsQuartiles + ": " + numFreqItemsetsQuartiles);

            final double pvalue = getPvalue(observedNumFreqItemsets, numFreqItemsetsDist);
            System.out.println(JsonKeys.pvalue + ": " + pvalue);

            // create object for samplerNumFreqItemsetsStats
            final JSONObject samplerNumFreqItemsetsStats = new JSONObject();
            samplerNumFreqItemsetsStats.put(JsonKeys.sampler, samplerName);
            samplerNumFreqItemsetsStats.put(JsonKeys.numSamples, numFreqItemsetsDist.size());
            samplerNumFreqItemsetsStats.put(JsonKeys.numFreqItemsetsQuartiles, numFreqItemsetsQuartiles);
            samplerNumFreqItemsetsStats.put(JsonKeys.pvalue, pvalue);
            samplerNumFreqItemsetsStats.put(
                    JsonKeys.freqItemsetLenToCountQuartiles, freqItemsetLenToCountQuartiles);

            numFreqItemsetsStats.put(samplerNumFreqItemsetsStats);
        }

        // create object for runInfo
        final JSONObject runInfo = new JSONObject();
        runInfo.put(JsonKeys.numFreqItemsets, observedNumFreqItemsets);
        runInfo.put(JsonKeys.freqItemsetLenToCount, observedFreqItemsetLenToCount);
        runInfo.put(JsonKeys.timestamp, LocalDateTime.now());

        // create base object
        final JSONObject results = new JSONObject();
        results.put(JsonKeys.runInfo, runInfo);
        results.put(JsonKeys.numFreqItemsetsStats, numFreqItemsetsStats);

        // save JSON
        final String datasetBaseName = new Paths(Config.datasetPath, "").datasetBaseName;
        final String resultsBaseName
                = String.join(
                        Delimiters.dash,
                        datasetBaseName,
                        String.valueOf(Config.numSwaps),
                        String.valueOf(Config.numSamples),
                        String.valueOf(Config.minFreq),
                        String.valueOf(Config.numThreads),
                        String.valueOf(Config.seed));
        final String resultPath = Paths.getJsonFilePath(Config.resultsDir, resultsBaseName);
        JsonFile.write(results, resultPath);

        System.out.println("Result written to " + resultPath);
    }

    /**
     * Gets a map where each key is a frequent itemset length and the value is
     * the total number of frequent itemsets with that length in the dataset.
     *
     * @param freqItemsets a set of frequent itemsets
     * @return a map where each key is a frequent itemset length and the value
     * is the total number of frequent itemsets with that length in the dataset
     */
    public static Int2IntOpenHashMap getFreqItemsetLenToCountMap(ObjectSet<IntOpenHashSet> freqItemsets) {
        final Int2IntOpenHashMap freqItemsetLenToCount = new Int2IntOpenHashMap();
        for (IntOpenHashSet freqItemset : freqItemsets) {
            final int freqItemsetLen = freqItemset.size();
            freqItemsetLenToCount.merge(freqItemsetLen, 1, Integer::sum);
        }
        return freqItemsetLenToCount;
    }

    /**
     * Gets a map where each key is a frequent itemset length and the value is
     * the total number of frequent itemsets with that length in the dataset.
     *
     * @param freqItemsetsFile the file where the frequent itemsets are saved
     * @return a map where each key is a frequent itemset length and the value
     * is the total number of frequent itemsets with that length in the dataset
     */
    public static Int2IntOpenHashMap getFreqItemsetLenToCountMap(File freqItemsetsFile) {
        final Int2IntOpenHashMap freqItemsetLenToCount = new Int2IntOpenHashMap();
        try {
            final BufferedReader br = new BufferedReader(new FileReader(freqItemsetsFile.getPath()));
            String line = br.readLine();
            while (line != null) {
                final String freqItemsetString = line.split(Delimiters.sup)[0];
                final int freqItemsetLen = freqItemsetString.split(Delimiters.space).length;
                freqItemsetLenToCount.merge(freqItemsetLen, 1, Integer::sum);
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Error getting frequent itemset length to count map");
            e.printStackTrace();
            System.exit(1);
        }
        return freqItemsetLenToCount;
    }

    /**
     * Gets the number of frequent itemsets in the dataset.
     *
     * @param freqItemsetLenToCount a map where each key is a frequent itemset
     * length and the value is the total number of frequent itemsets with that
     * length in the dataset
     * @return the number of frequent itemsets in the dataset
     */
    public static int getNumFreqItemsets(Int2IntOpenHashMap freqItemsetLenToCount) {
        int numFreqItemsets = 0;
        for (int count : freqItemsetLenToCount.values()) {
            numFreqItemsets += count;
        }
        return numFreqItemsets;
    }

    /**
     * Updates the freqItemsetLenToCountDist with freqItemsetLenToCount.
     *
     * @param freqItemsetLenToCountDist a map where each key is a frequent
     * itemset length and the value is the distribution of the total number of
     * frequent itemsets with that length across the sampled datasets
     * @param freqItemsetLenToCount a map where each key is a frequent itemset
     * length and the value is the total number of frequent itemsets with that
     * length in the dataset
     */
    public static void updateFreqItemsetLenToCountDist(
            Map<Integer, List<Integer>> freqItemsetLenToCountDist,
            Int2IntOpenHashMap freqItemsetLenToCount) {
        for (int freqItemsetLen : freqItemsetLenToCount.keySet()) {
            final int count = freqItemsetLenToCount.get(freqItemsetLen);
            final List<Integer> countDist
                    = freqItemsetLenToCountDist.getOrDefault(freqItemsetLen, Lists.newArrayList());
            countDist.add(count);
            freqItemsetLenToCountDist.put(freqItemsetLen, countDist);
        }
    }

    /**
     * Gets a map where each key is a frequent itemset length and the value is
     * the quartiles for the distribution of the total number of frequent
     * itemsets with that length across the sampled datasets.
     *
     * @param freqItemsetLenToCountDist a map where each key is a frequent
     * itemset length and the value is the distribution of the total number of
     * frequent itemsets with that length across the sampled datasets
     * @return a map where each key is a frequent itemset length and the value
     * is the quartiles for the distribution of the total number of frequent
     * itemsets with that length across the sampled datasets.
     */
    public static Map<Integer, List<Integer>> getFreqItemsetLenToCountQuartiles(
            Map<Integer, List<Integer>> freqItemsetLenToCountDist) {
        final Map<Integer, List<Integer>> freqItemsetLenToCountQuartiles = new HashMap<>();
        for (Entry<Integer, List<Integer>> entry : freqItemsetLenToCountDist.entrySet()) {
            final int freqItemsetLen = entry.getKey();
            final List<Integer> countDist = entry.getValue();
            final List<Integer> countQuartiles = getQuartiles(countDist);
            freqItemsetLenToCountQuartiles.put(freqItemsetLen, countQuartiles);
        }
        return freqItemsetLenToCountQuartiles;
    }

    /**
     * Gets the p-value of the number of frequent itemsets in the observed
     * dataset with respect to the distribution of the number of frequent
     * itemsets across the sampled datasets.
     *
     * @param observedNumFreqItemsets the number of frequent itemsets in the
     * observed dataset
     * @param numFreqItemsetsDist the distribution of the number of frequent
     * itemsets across the sampled datasets
     * @return the p-value of the number of frequent itemsets in the observed
     * dataset
     */
    public static double getPvalue(int observedNumFreqItemsets, List<Integer> numFreqItemsetsDist) {
        int count = 1;
        for (int numFreqItemsets : numFreqItemsetsDist) {
            if (numFreqItemsets >= observedNumFreqItemsets) {
                count++;
            }
        }
        return (double) count / (numFreqItemsetsDist.size() + 1);
    }

    /**
     * Gets the quartiles for the given distribution.
     *
     * @param dist the input distribution
     * @return the quartiles of the distribution
     */
    public static List<Integer> getQuartiles(List<Integer> dist) {
        final DescriptiveStatistics stats = new DescriptiveStatistics();
        for (int value : dist) {
            stats.addValue(value);
        }
        final int min = (int) stats.getMin();
        final int q1 = (int) stats.getPercentile(25);
        final int median = (int) stats.getPercentile(50);
        final int q3 = (int) stats.getPercentile(75);
        final int max = (int) stats.getMax();
        return Arrays.asList(min, q1, median, q3, max);
    }
}
