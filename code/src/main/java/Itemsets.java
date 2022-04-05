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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/** A helper class to mine significant frequent itemsets. */
class Itemsets {
  /**
   * Gets the minimum p-value for the set of frequent itemsets specified by freqItemsetsPath.
   *
   * @param paths an object to get necessary paths
   * @param freqItemsetsPath the path for the set of frequent itemsets
   * @param numEstSamples the number of samples used to estimate p-values
   * @return the minimum p-value for the set of frequent itemsets
   */
  static double getMinPvalue(Paths paths, String freqItemsetsPath, int numEstSamples) {
    final Map<Set<Integer>, Integer> freqItemsetToSum =
        getFreqItemsetToSumMap(paths, freqItemsetsPath, numEstSamples);
    final Collection<Integer> sums = freqItemsetToSum.values();
    int minSum;
    if (sums.size() != 0) {
      minSum = Collections.min(sums);
    } else {
      minSum = 0;
    }
    return getPvalue(minSum, numEstSamples);
  }

  /**
   * Gets a map where each key is a frequent itemset and the value is the p-value for the frequent
   * itemset.
   *
   * @param paths an object to get necessary paths
   * @param freqItemsetToSup a map where each key is a frequent itemset and the value is the support
   *     for the frequent itemset
   * @param numEstSampls the number of samples used to estimate p-values
   * @return a map where each key is a frequent itemset and the value is the p-value for the
   *     frequent itemset.
   */
  static Map<Set<Integer>, Double> getFreqItemsetToPvalueMap(
      Paths paths, Map<Set<Integer>, Integer> freqItemsetToSup, int numEstSamples) {
    final Map<Set<Integer>, Double> freqItemsetToPvalue = new HashMap<>();
    final Map<Set<Integer>, Integer> freqItemsetToSum =
        getFreqItemsetToSumMap(paths, freqItemsetToSup, numEstSamples);
    for (Entry<Set<Integer>, Integer> entry : freqItemsetToSum.entrySet()) {
      final Set<Integer> freqItemset = entry.getKey();
      final int sum = entry.getValue();
      final double pvalue = getPvalue(sum, numEstSamples);
      freqItemsetToPvalue.put(freqItemset, pvalue);
    }
    return freqItemsetToPvalue;
  }

  /**
   * Gets a map where each key is a frequent itemset in the input dataset and the value is the
   * number of estimate (sampled) datasets where the itemset has a support no less than its support
   * in the input dataset.
   *
   * @param paths an object to get necessary paths
   * @param freqItemsetToSup a map where each key is a frequent itemset and the value is the support
   *     for the frequent itemset
   * @param numEstSampls the number of samples used to estimate p-values
   * @return a map where each key is a frequent itemset in the input dataset and the value is the
   *     number of estimate (sampled) datasets where the itemset has a support no less than its
   *     support in the input dataset
   */
  static Map<Set<Integer>, Integer> getFreqItemsetToSumMap(
      Paths paths, Map<Set<Integer>, Integer> freqItemsetToSup, int numEstSamples) {
    final Map<Set<Integer>, Integer> freqItemsetToSum = new HashMap<>();

    for (int i = 0; i < numEstSamples; i++) {
      final String estFreqItemsetsPath = paths.getFreqItemsetsPath(Paths.estTag, i);

      try {
        final BufferedReader br = new BufferedReader(new FileReader(estFreqItemsetsPath));
        String line = br.readLine();
        while (line != null) {
          final String[] freqItemsetAndSup = line.split(Delimiters.sup);
          final Set<Integer> freqItemset = getFreqItemset(freqItemsetAndSup);
          final int sup = getSup(freqItemsetAndSup);
          // (MR) XXX: Breaks if sup == Integer.MAX_VALUE but freqItemsets is
          // not in freqItemsetToSup, which seems unlikely.
          if (sup >= freqItemsetToSup.getOrDefault(freqItemset, Integer.MAX_VALUE)) {
            freqItemsetToSum.merge(freqItemset, 1, Integer::sum);
          }
          line = br.readLine();
        }
        br.close();
      } catch (IOException e) {
        System.err.println("Error getting frequent itemset to sum map");
        e.printStackTrace();
        System.exit(1);
      }
    }

    return freqItemsetToSum;
  }

  /**
   * Gets a map where each key is a frequent itemset in the input dataset and the value is the
   * number of estimate (sampled) datasets where the itemset has a support no less than its support
   * in the input dataset.
   *
   * @param paths an object to get necessary paths
   * @param freqItemsetsPath the path to the set of frequent itemsets
   * @param numEstSampls the number of samples used to estimate p-values
   * @return a map where each key is a frequent itemset in the input dataset and the value is the
   *     number of estimate (sampled) datasets where the itemset has a support no less than its
   *     support in the input dataset
   */
  static Map<Set<Integer>, Integer> getFreqItemsetToSumMap(
      Paths paths, String freqItemsetsPath, int numEstSamples) {
    final Map<Set<Integer>, Integer> freqItemsetToSup = getFreqItemsetToSupMap(freqItemsetsPath);
    return getFreqItemsetToSumMap(paths, freqItemsetToSup, numEstSamples);
  }

  /**
   * Gets a map where each key is a frequent itemset and the value is the support for the frequent
   * itemset.
   *
   * @param freqItemsetPath the path for the set of frequent itemsets
   * @return a map where each key is a frequent itemset and the value is the support for the
   *     frequent itemset.
   */
  static Map<Set<Integer>, Integer> getFreqItemsetToSupMap(String freqItemsetsPath) {
    final Map<Set<Integer>, Integer> freqItemsetToSup = new HashMap<>();

    try {
      final BufferedReader br = new BufferedReader(new FileReader(freqItemsetsPath));

      String line = br.readLine();
      while (line != null) {
        final String[] freqItemsetAndSup = line.split(Delimiters.sup);
        final Set<Integer> freqItemset = getFreqItemset(freqItemsetAndSup);
        final int sup = getSup(freqItemsetAndSup);
        freqItemsetToSup.put(freqItemset, sup);
        line = br.readLine();
      }
      br.close();
    } catch (IOException e) {
      System.err.println("Error getting frequent itemset to support map");
      e.printStackTrace();
      System.exit(1);
    }

    return freqItemsetToSup;
  }

  /**
   * Gets the estimated p-value.
   *
   * @param sum the sum
   * @param numEstSamples the number of samples to estimate the p-value
   */
  static double getPvalue(int sum, int numEstSamples) {
    return (double) (1 + sum) / (numEstSamples + 1);
  }

  private static Set<Integer> getFreqItemset(String[] freqItemsetAndSup) {
    final String freqItemsetString = freqItemsetAndSup[0];
    final Set<Integer> freqItemset = new HashSet<>();
    for (String itemString : freqItemsetString.split(Delimiters.space)) {
      final int itemInt = Integer.parseInt(itemString);
      freqItemset.add(itemInt);
    }
    return freqItemset;
  }

  private static int getSup(String[] itemsetAndSup) {
    return Integer.parseInt(itemsetAndSup[1]);
  }

  static String toString(Set<Integer> itemset) {
    final StringBuilder buffer = new StringBuilder();
    for (int item : itemset) {
      buffer.append(item);
      buffer.append(Delimiters.space);
    }
    buffer.deleteCharAt(buffer.length() - 1);
    return buffer.toString();
  }
}
