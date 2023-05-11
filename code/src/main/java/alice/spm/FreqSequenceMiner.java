package alice.spm;

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
import java.io.IOException;

/**
 * A wrapper class for mining frequent itemsets.
 */
public class FreqSequenceMiner {

    /**
     * Mines frequent itemsets and saves them to disk.
     *
     * @param datasetPath the path of the dataset
     * @param minFreq the minimum frequency threshold
     * @param freqItemsetsPath the path to save the frequent itemsets
     */
    public static void mine(String datasetPath, double minFreq, String freqItemsetsPath) {
        final AlgoPrefixSpan algo = new AlgoPrefixSpan();
        try {
            algo.runAlgorithm(datasetPath, minFreq, freqItemsetsPath);
        } catch (IOException e) {
            System.err.println("Error running mining algorithm");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Mines frequent itemsets and returns them as a map where each key is a
     * frequent itemset and the value is the frequent itemset's support.
     *
     * @param datasetPath the path of the dataset
     * @param minFreq the minimum frequency threshold
     * @return a map where each key is a frequent itemset and the value is the
     * frequent itemset's support
     */
    public static SequentialPatterns mine(String datasetPath, double minFreq) {
        final AlgoPrefixSpan algo = new AlgoPrefixSpan();
        try {
            return algo.runAlgorithm(datasetPath, minFreq, null);
        } catch (IOException e) {
            System.err.println("Error running mining algorithm");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
    
    public static SequentialPatterns mine(String[][] dataset, double minFreq) {
        final AlgoPrefixSpan algo = new AlgoPrefixSpan();
        try {
            return algo.runAlgorithm(dataset, minFreq);
        } catch (IOException e) {
            System.err.println("Error running mining algorithm");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
