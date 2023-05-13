package alice.test;

/*
 * Copyright (C) 2023 Giulia Preti
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
import alice.utils.CMDLineParser;
import alice.utils.Config;
import alice.spm.SampleAndMinerSeq;
import alice.samplers.AliceSSampler;
import alice.samplers.GmmtSeqSampler;
import alice.samplers.SeqSampler;
import alice.spm.FreqSequenceMiner;
import alice.spm.SequentialPatterns;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class runs the number of frequent sequence patterns experiment by measuring the
 * quartiles for the number of frequent patterns across the sampled distribution
 * of sets of frequent patterns as well as measure the p-value for the number of
 * frequent patterns in the observed dataset.
 */
public class NumFreqItemsetsSeq {

    public static void main(String[] args) throws IOException {
        
        CMDLineParser.parse(args);

        System.out.println("Executing number of frequent itemsets experiment");
        final SequentialPatterns observedFreqItemsets
                = FreqSequenceMiner.mine(Config.datasetPath, Config.minFreq);

        final int observedNumFreqItemsets = observedFreqItemsets.getSequenceCount();
        System.out.println(JsonKeys.numFreqItemsets + ": " + observedNumFreqItemsets);

        final Int2IntOpenHashMap observedFreqItemsetLenToCount
                = getFreqItemsetLenToCountMap(observedFreqItemsets);
        System.out.println(JsonKeys.freqItemsetLenToCount + ": " + observedFreqItemsetLenToCount);
        
        final SeqSampler[] samplers = new SeqSampler[]{new AliceSSampler(), new GmmtSeqSampler()};

        // create object for numFreqItemsetsStats
        final JSONArray numFreqItemsetsStats = new JSONArray();

        for (SeqSampler sampler : samplers) {
            final String samplerName = sampler.getClass().getName();
            System.out.println(JsonKeys.sampler + ": " + samplerName);

            final String samplerResultsDir = Paths.concat(Config.resultsDir, samplerName);
            final Paths paths = new Paths(Config.datasetPath, samplerResultsDir);
            final String freqItemsetsSamplesDir = paths.freqItemsetsDirPath;

            if (Config.sampleAndMine) {
                System.out.println("Sampling and mining");
                int numSwaps = Config.numSwaps;
                if (samplerName.equals(GmmtSeqSampler.class.getName())) {
                    numSwaps *= 5;
                    System.out.println(numSwaps);
                }
                SampleAndMinerSeq.sampleAndMine(
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

            final IntArrayList numFreqItemsetsDist = new IntArrayList();
            final Int2ObjectMap<IntArrayList> freqItemsetLenToCountDist = new Int2ObjectOpenHashMap();
            final File[] freqItemsetsSamples = new File(freqItemsetsSamplesDir).listFiles();

            for (File freqItemsetsSample : freqItemsetsSamples) {
                final Int2IntOpenHashMap freqItemsetLenToCount
                        = getFreqItemsetLenToCountMap(freqItemsetsSample);
                final int numFreqItemsets = getNumFreqItemsets(freqItemsetLenToCount);
                numFreqItemsetsDist.add(numFreqItemsets);
                updateFreqItemsetLenToCountDist(freqItemsetLenToCountDist, freqItemsetLenToCount);
            }

            System.out.println(JsonKeys.numSamples + ": " + numFreqItemsetsDist.size());

            final Int2ObjectMap<int[]> freqItemsetLenToCountQuartiles
                    = getFreqItemsetLenToCountQuartiles(freqItemsetLenToCountDist);
            System.out.println(JsonKeys.freqItemsetLenToCountQuartiles + ": ");
            freqItemsetLenToCountQuartiles.int2ObjectEntrySet().forEach(entry -> {
                System.out.println(entry.getIntKey() + " => " + Arrays.toString(entry.getValue()));
            });

            final int[] numFreqItemsetsQuartiles = getQuartiles(numFreqItemsetsDist);
            System.out.println(JsonKeys.numFreqItemsetsQuartiles + ": " + Arrays.toString(numFreqItemsetsQuartiles));

            final double pvalue = getPvalue(observedNumFreqItemsets, numFreqItemsetsDist);
            System.out.println(JsonKeys.pvalue + ": " + pvalue);

            // create object for samplerNumFreqItemsetsStats
            final JSONObject samplerNumFreqItemsetsStats = new JSONObject();
            samplerNumFreqItemsetsStats.put(JsonKeys.sampler, samplerName);
            samplerNumFreqItemsetsStats.put(JsonKeys.numSamples, numFreqItemsetsDist.size());
            samplerNumFreqItemsetsStats.put(JsonKeys.numFreqItemsetsQuartiles, numFreqItemsetsQuartiles);
            samplerNumFreqItemsetsStats.put(JsonKeys.pvalue, pvalue);
            samplerNumFreqItemsetsStats.put(JsonKeys.freqItemsetLenToCountQuartiles, freqItemsetLenToCountQuartiles);

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
     * Gets a map where each key is a frequent pattern length and the value is
     * the total number of frequent patterns with that length in the dataset.
     *
     * @param freqItemsets frequent sequence patterns
     * @return a map where each key is a frequent pattern length and the value is
     * the total number of frequent patterns with that length in the dataset
     */
    public static Int2IntOpenHashMap getFreqItemsetLenToCountMap(SequentialPatterns freqItemsets) {
        final Int2IntOpenHashMap freqItemsetLenToCount = new Int2IntOpenHashMap();
        for (int level = 0; level < freqItemsets.getLevelCount(); level ++) {
            freqItemsetLenToCount.put(level, freqItemsets.getLevel(level).size());
        }
        return freqItemsetLenToCount;
    }

    /**
     * Gets a map where each key is a frequent pattern length and the value is
     * the total number of frequent patterns with that length in the dataset.
     *
     * @param freqItemsetsFile the file where the frequent patterns are saved
     * @return a map where each key is a frequent pattern length and the value
     * is the total number of frequent patterns with that length in the dataset
     */
    public static Int2IntOpenHashMap getFreqItemsetLenToCountMap(File freqItemsetsFile) {
        
        // pattern -1 #SUP: support #SID: sid1 sid2 ... sidn
        final Int2IntOpenHashMap freqItemsetLenToCount = new Int2IntOpenHashMap();
        freqItemsetLenToCount.defaultReturnValue(0);
        try {
            final BufferedReader br = new BufferedReader(new FileReader(freqItemsetsFile.getPath()));
            String line = br.readLine();
            while (line != null) {
                String[] tmp = line.trim().split(" #SUP: ");
                String[] freqItemsetString = tmp[0].trim().split("-1");
                freqItemsetLenToCount.addTo(freqItemsetString.length, 1);
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
     * Gets the number of frequent patterns in the dataset.
     *
     * @param freqItemsetLenToCount a map where each key is a frequent pattern
     * length and the value is the total number of frequent patterns with that
     * length in the dataset
     * @return the number of frequent patterns in the sequence dataset
     */
    public static int getNumFreqItemsets(Int2IntOpenHashMap freqItemsetLenToCount) {
        return freqItemsetLenToCount.values().intStream().sum();
    }

    /**
     * Updates the freqItemsetLenToCountDist with freqItemsetLenToCount.
     *
     * @param freqItemsetLenToCountDist a map where each key is a frequent
     * pattern length and the value is the distribution of the total number of
     * frequent patterns with that length across the sampled datasets
     * @param freqItemsetLenToCount a map where each key is a frequent pattern
     * length and the value is the total number of frequent patterns with that
     * length in the dataset
     */
    public static void updateFreqItemsetLenToCountDist(
            Int2ObjectMap<IntArrayList> freqItemsetLenToCountDist,
            Int2IntOpenHashMap freqItemsetLenToCount) {
        for (int freqItemsetLen : freqItemsetLenToCount.keySet()) {
            final int count = freqItemsetLenToCount.get(freqItemsetLen);
            final IntArrayList countDist = freqItemsetLenToCountDist.getOrDefault(freqItemsetLen, new IntArrayList());
            countDist.add(count);
            freqItemsetLenToCountDist.put(freqItemsetLen, countDist);
        }
    }

    /**
     * Gets a map where each key is a frequent pattern length and the value is
     * the quartiles for the distribution of the total number of frequent
     * patterns with that length across the sampled datasets.
     *
     * @param freqItemsetLenToCountDist a map where each key is a frequent
     * pattern length and the value is the distribution of the total number of
     * frequent patterns with that length across the sampled datasets
     * @return a map where each key is a frequent pattern length and the value
     * is the quartiles for the distribution of the total number of frequent
     * patterns with that length across the sampled datasets.
     */
    public static Int2ObjectMap<int[]> getFreqItemsetLenToCountQuartiles(
            Int2ObjectMap<IntArrayList> freqItemsetLenToCountDist) {
        
        final Int2ObjectMap<int[]> freqItemsetLenToCountQuartiles = new Int2ObjectOpenHashMap();
        for (int freqItemsetLen : freqItemsetLenToCountDist.keySet()) {
            final IntArrayList countDist = freqItemsetLenToCountDist.get(freqItemsetLen);
            final int[] countQuartiles = getQuartiles(countDist);
            freqItemsetLenToCountQuartiles.put(freqItemsetLen, countQuartiles);
        }
        return freqItemsetLenToCountQuartiles;
    }

    /**
     * Gets the p-value of the number of frequent patterns in the observed
     * dataset with respect to the distribution of the number of frequent
     * patterns across the sampled datasets.
     *
     * @param observedNumFreqItemsets the number of frequent patterns in the
     * observed dataset
     * @param numFreqItemsetsDist the distribution of the number of frequent
     * patterns across the sampled datasets
     * @return the p-value of the number of frequent patterns in the observed
     * dataset
     */
    public static double getPvalue(int observedNumFreqItemsets, IntArrayList numFreqItemsetsDist) {
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
    public static int[] getQuartiles(IntArrayList dist) {
        final DescriptiveStatistics stats = new DescriptiveStatistics();
        for (int value : dist) {
            stats.addValue(value);
        }
        final int min = (int) stats.getMin();
        final int q1 = (int) stats.getPercentile(25);
        final int median = (int) stats.getPercentile(50);
        final int q3 = (int) stats.getPercentile(75);
        final int max = (int) stats.getMax();
        return new int[]{min, q1, median, q3, max};
    }
}
