package alice.fpm;

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
import alice.config.Paths;
import alice.config.Delimiters;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * A helper class to mine significant frequent itemsets.
 */
public class Itemsets {

    /**
     * Gets the minimum p-value for the set of frequent itemsets specified by
     * freqItemsetsPath.
     *
     * @param paths an object to get necessary paths
     * @param freqItemsetsPath the path for the set of frequent itemsets
     * @param numEstSamples the number of samples used to estimate p-values
     * @return the minimum p-value for the set of frequent itemsets
     */
    public static double getMinPvalue(Paths paths, String freqItemsetsPath, int numEstSamples) {
        final Object2IntOpenHashMap<IntOpenHashSet> freqItemsetToSum
                = getFreqItemsetToSumMap(paths, freqItemsetsPath, numEstSamples);
        final Collection<Integer> sums = freqItemsetToSum.values();
        int minSum = 0;
        if (!sums.isEmpty()) {
            minSum = Collections.min(sums);
        }
        return getPvalue(minSum, numEstSamples);
    }

    /**
     * Gets a map where each key is a frequent itemset and the value is the
     * p-value for the frequent itemset.
     *
     * @param paths an object to get necessary paths
     * @param freqItemsetToSup a map where each key is a frequent itemset and
     * the value is the support for the frequent itemset
     * @param numEstSamples the number of samples used to estimate p-values
     * @return a map where each key is a frequent itemset and the value is the
     * p-value for the frequent itemset.
     */
    public static Object2DoubleOpenHashMap<IntOpenHashSet> getFreqItemsetToPvalueMap(
            Paths paths, Object2IntOpenHashMap<IntOpenHashSet> freqItemsetToSup, int numEstSamples) {
        final Object2DoubleOpenHashMap<IntOpenHashSet> freqItemsetToPvalue = new Object2DoubleOpenHashMap();
        final Object2IntOpenHashMap<IntOpenHashSet> freqItemsetToSum
                = getFreqItemsetToSumMap(paths, freqItemsetToSup, numEstSamples);
        for (IntOpenHashSet freqItemset : freqItemsetToSum.keySet()) {
            final int sum = freqItemsetToSum.getInt(freqItemset);
            final double pvalue = getPvalue(sum, numEstSamples);
            freqItemsetToPvalue.put(freqItemset, pvalue);
        }
        return freqItemsetToPvalue;
    }

    /**
     * Gets a map where each key is a frequent itemset in the input dataset and
     * the value is the number of estimate (sampled) datasets where the itemset
     * has a support no less than its support in the input dataset.
     *
     * @param paths an object to get necessary paths
     * @param freqItemsetToSup a map where each key is a frequent itemset and
     * the value is the support for the frequent itemset
     * @param numEstSamples the number of samples used to estimate p-values
     * @return a map where each key is a frequent itemset in the input dataset
     * and the value is the number of estimate (sampled) datasets where the
     * itemset has a support no less than its support in the input dataset
     */
    public static Object2IntOpenHashMap<IntOpenHashSet> getFreqItemsetToSumMap(
            Paths paths, Object2IntOpenHashMap<IntOpenHashSet> freqItemsetToSup, int numEstSamples) {
        final Object2IntOpenHashMap<IntOpenHashSet> freqItemsetToSum = new Object2IntOpenHashMap();

        for (int i = 0; i < numEstSamples; i++) {
            final String estFreqItemsetsPath = paths.getFreqItemsetsPath(Paths.estTag, i);

            try {
                final BufferedReader br = new BufferedReader(new FileReader(estFreqItemsetsPath));
                String line = br.readLine();
                while (line != null) {
                    final String[] freqItemsetAndSup = line.split(Delimiters.sup);
                    final IntOpenHashSet freqItemset = getFreqItemset(freqItemsetAndSup);
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
     * Gets a map where each key is a frequent itemset in the input dataset and
     * the value is the number of estimate (sampled) datasets where the itemset
     * has a support no less than its support in the input dataset.
     *
     * @param paths an object to get necessary paths
     * @param freqItemsetsPath the path to the set of frequent itemsets
     * @param numEstSamples the number of samples used to estimate p-values
     * @return a map where each key is a frequent itemset in the input dataset
     * and the value is the number of estimate (sampled) datasets where the
     * itemset has a support no less than its support in the input dataset
     */
    public static Object2IntOpenHashMap<IntOpenHashSet> getFreqItemsetToSumMap(
            Paths paths, String freqItemsetsPath, int numEstSamples) {
        final Object2IntOpenHashMap<IntOpenHashSet> freqItemsetToSup = getFreqItemsetToSupMap(freqItemsetsPath);
        return getFreqItemsetToSumMap(paths, freqItemsetToSup, numEstSamples);
    }

    /**
     * Gets a map where each key is a frequent itemset and the value is the
     * support for the frequent itemset.
     *
     * @param freqItemsetsPath the path for the set of frequent itemsets
     * @return a map where each key is a frequent itemset and the value is the
     * support for the frequent itemset.
     */
    public static Object2IntOpenHashMap<IntOpenHashSet> getFreqItemsetToSupMap(String freqItemsetsPath) {
        final Object2IntOpenHashMap<IntOpenHashSet> freqItemsetToSup = new Object2IntOpenHashMap();

        try {
            final BufferedReader br = new BufferedReader(new FileReader(freqItemsetsPath));

            String line = br.readLine();
            while (line != null) {
                final String[] freqItemsetAndSup = line.split(Delimiters.sup);
                final IntOpenHashSet freqItemset = getFreqItemset(freqItemsetAndSup);
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
     * @return estimated p-value
     */
    public static double getPvalue(int sum, int numEstSamples) {
        return (double) (1 + sum) / (numEstSamples + 1);
    }

    public static IntOpenHashSet getFreqItemset(String[] freqItemsetAndSup) {
        final String freqItemsetString = freqItemsetAndSup[0];
        final IntOpenHashSet freqItemset = new IntOpenHashSet();
        for (String itemString : freqItemsetString.split(Delimiters.space)) {
            final int itemInt = Integer.parseInt(itemString);
            freqItemset.add(itemInt);
        }
        return freqItemset;
    }

    public static int getSup(String[] itemsetAndSup) {
        return Integer.parseInt(itemsetAndSup[1]);
    }

    public static String toString(IntOpenHashSet itemset) {
        final StringBuilder buffer = new StringBuilder();
        for (int item : itemset) {
            buffer.append(item);
            buffer.append(Delimiters.space);
        }
        buffer.deleteCharAt(buffer.length() - 1);
        return buffer.toString();
    }
}
