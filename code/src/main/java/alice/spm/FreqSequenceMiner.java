package alice.spm;

/*
 * Copyright (C) 2022 Giulia Preti
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
 * A wrapper class for mining frequent sequence patterns.
 */
public class FreqSequenceMiner {

    /**
     * Mines frequent sequence patterns and saves them to disk.
     *
     * @param datasetPath the path of the dataset
     * @param minFreq the minimum frequency threshold
     * @param freqItemsetsPath the path to save the frequent patterns
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
     * Mines frequent sequence patterns and returns them as a map where each key is a
     * frequent pattern and the value is the frequent pattern support.
     *
     * @param datasetPath the path of the sequence dataset
     * @param minFreq the minimum frequency threshold
     * @return a list of frequent sequence itemsets
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
    
    /**
     * Mines frequent sequence patterns and returns them as a map where each key is a
     * frequent pattern and the value is the frequent pattern support.
     *
     * @param dataset the sequence dataset
     * @param minFreq the minimum frequency threshold
     * @return a list of frequent sequence itemsets
     */
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
