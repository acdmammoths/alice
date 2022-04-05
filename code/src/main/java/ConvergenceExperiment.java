/*
 * Copyright (C) 2022 Alexander Lee and Matteo Riondato
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class runs the convergence experiment by measuring the average relative support/frequency
 * difference for different values of the swap number multiplier/factor.
 */
public class ConvergenceExperiment {
  public static void main(String[] args) {
    final String confPath = args[0];
    run(confPath);
  }

  private static void run(String confPath) {
    System.out.println("Reading configuration file at " + confPath);

    final JSONObject conf = JsonFile.read(confPath);
    final String datasetPath = conf.getString(JsonKeys.datasetPath);
    final int maxNumSwapsFactor = conf.getInt(JsonKeys.maxNumSwapsFactor);
    final double minFreq = conf.getDouble(JsonKeys.minFreq);
    final long seed = conf.getLong(JsonKeys.seed);
    final String resultsDir = conf.getString(JsonKeys.resultsDir);

    Paths.makeDir(resultsDir);

    final Sampler[] samplers = {new NaiveSampler(), new RefinedSampler(), new GmmtSampler()};

    System.out.println("Executing convergence experiment for dataset at " + datasetPath);

    System.out.println("Mining frequent itemsets");
    final Map<Set<Integer>, Integer> freqItemsetToSup = FreqItemsetMiner.mine(datasetPath, minFreq);

    final int numFreqItemsets = freqItemsetToSup.size();
    System.out.println(JsonKeys.numFreqItemsets + ": " + freqItemsetToSup.size());

    final Transformer transformer = new Transformer();
    final SparseMatrix realMatrix = transformer.createMatrix(datasetPath);

    System.out.println("Matrix created from dataset");

    final int numOnes = new Matrix(realMatrix).getSum();

    System.out.println(JsonKeys.numOnes + ": " + numOnes);

    final Random rnd = new Random(seed);

    final List<Double> numSwapsFactors = new ArrayList<>();
    // use more granular factors at first
    for (double i = 0; i < 2; i += 0.25) {
      numSwapsFactors.add(i);
    }
    for (double i = 2; i <= maxNumSwapsFactor; i++) {
      numSwapsFactors.add(i);
    }

    // create object for convergenceStats
    final JSONObject convergenceStats = new JSONObject();

    for (Sampler sampler : samplers) {
      final String samplerName = sampler.getClass().getName();
      System.out.println(JsonKeys.sampler + ": " + samplerName);

      final JSONArray samplerConvergenceStats = new JSONArray();

      // initialize start state as the real dataset
      SparseMatrix startMatrix = realMatrix;

      for (int i = 0; i < numSwapsFactors.size(); i++) {
        final double numSwapsFactor = numSwapsFactors.get(i);
        System.out.println("\t" + JsonKeys.numSwapsFactor + ": " + numSwapsFactor);

        final double prevNumSwapsFactor = i > 0 ? numSwapsFactors.get(i - 1) : 0;
        final int numSwaps = (int) ((numSwapsFactor - prevNumSwapsFactor) * numOnes);
        System.out.println("\t\t" + JsonKeys.numSwaps + ": " + numSwaps);

        System.out.println("\t\tGetting sample from matrix");
        final SparseMatrix sample =
            sampler.sample(startMatrix, numSwaps, rnd.nextLong(), new Timer(false));

        System.out.println("\t\tGetting sample itemset to support map");
        final Map<Set<Integer>, Integer> sampleFreqItemsetToSup =
            getSampleItemsetToSupMap(sample, transformer, freqItemsetToSup.keySet());

        // compute convergence statistics
        final double avgRelFreqDiff = getAvgRelFreqDiff(freqItemsetToSup, sampleFreqItemsetToSup);
        System.out.println("\t\t" + JsonKeys.avgRelFreqDiff + ": " + avgRelFreqDiff);

        // create object for factorConvergenceStats
        final JSONObject factorConvergenceStats = new JSONObject();
        factorConvergenceStats.put(JsonKeys.numSwapsFactor, numSwapsFactor);
        factorConvergenceStats.put(JsonKeys.avgRelFreqDiff, avgRelFreqDiff);

        samplerConvergenceStats.put(factorConvergenceStats);

        // run the chain again starting from the last sample
        startMatrix = sample;
      }

      convergenceStats.put(samplerName, samplerConvergenceStats);
    }

    // create object for runInfo
    final JSONObject runInfo = new JSONObject();
    runInfo.put(JsonKeys.args, conf);
    runInfo.put(JsonKeys.timestamp, LocalDateTime.now());
    runInfo.put(JsonKeys.numOnes, numOnes);
    runInfo.put(JsonKeys.numFreqItemsets, numFreqItemsets);

    // create base object
    final JSONObject results = new JSONObject();
    results.put(JsonKeys.runInfo, runInfo);
    results.put(JsonKeys.convergenceStats, convergenceStats);

    // save JSON
    final String datasetBaseName = new Paths(datasetPath, "").datasetBaseName;
    final String resultsBaseName =
        String.join(
            Delimiters.dash,
            datasetBaseName,
            String.valueOf(maxNumSwapsFactor),
            String.valueOf(minFreq),
            String.valueOf(seed));
    final String resultPath = Paths.getJsonFilePath(resultsDir, resultsBaseName);
    JsonFile.write(results, resultPath);

    System.out.println("Result written to " + resultPath);
  }

  /**
   * Gets the sample's itemset to support map such that the map only contains itemsets that are
   * frequent itemsets of the observed dataset.
   *
   * @param sample the matrix representation of the sampled dataset
   * @param transformer the transformer used to create the matrix of the sample
   * @param freqItemsets the set of frequent itemsets in the observed dataset
   * @return a map where each key is an itemset and the value is the itemset's support in the
   *     sampled dataset such that the map only contains itemsets that are frequent itemsets of the
   *     observed dataset
   */
  static Map<Set<Integer>, Integer> getSampleItemsetToSupMap(
      SparseMatrix sample, Transformer transformer, Set<Set<Integer>> freqItemsets) {
    final Map<Set<Integer>, Integer> sampleItemsetToSup = new HashMap<>();

    for (int r = 0; r < sample.getNumRows(); r++) {
      for (Set<Integer> freqItemset : freqItemsets) {
        if (isItemsetInSampleTransaction(freqItemset, sample, r, transformer.itemToColIndex)) {
          final int sup = sampleItemsetToSup.getOrDefault(freqItemset, 0);
          sampleItemsetToSup.put(freqItemset, sup + 1);
        }
      }
    }

    return sampleItemsetToSup;
  }

  /**
   * Determines whether the itemset is in the transaction of the sample specified by rowIndex.
   *
   * @param itemset the itemset
   * @param sample the sample
   * @param rowIndex the row index that corresponds to the transaction
   * @param itemToColIndex a map where each key is an item and the value is the item's column index
   *     in the sample matrix
   * @return whether the itemset is in the transaction of the sample
   */
  private static boolean isItemsetInSampleTransaction(
      Set<Integer> itemset,
      SparseMatrix sample,
      int rowIndex,
      Map<Integer, Integer> itemToColIndex) {
    for (int item : itemset) {
      final int colIndex = itemToColIndex.get(item);
      if (sample.get(rowIndex, colIndex) == 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the average relative frequency/support difference.
   *
   * @param freqItemsetToSup a map where each key is a frequent itemset in the observed dataset and
   *     the value is the frequent itemset's support
   * @param sampleItemsetToSup a map where each key is an itemset and the value is the itemset's
   *     support in the sampled dataset such that the map only contains itemsets that are frequent
   *     itemsets of the observed dataset
   * @return the average relative frequency/support difference
   */
  static double getAvgRelFreqDiff(
      Map<Set<Integer>, Integer> freqItemsetToSup, Map<Set<Integer>, Integer> sampleItemsetToSup) {
    double sumRelFreqDiff = 0;

    for (Entry<Set<Integer>, Integer> entry : freqItemsetToSup.entrySet()) {
      final Set<Integer> itemset = entry.getKey();
      final int sup = entry.getValue();
      final int sampleSup = sampleItemsetToSup.getOrDefault(itemset, 0);

      final double relFreqDiff = (double) Math.abs(sup - sampleSup) / sup;
      sumRelFreqDiff += relFreqDiff;
    }

    return sumRelFreqDiff / freqItemsetToSup.size();
  }
}
