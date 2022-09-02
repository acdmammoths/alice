package alice.utils;

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
/**
 * Variables to store the hyperparameters of ALICE and the experimental settings.
 */
public class Config {
    // directory where the datasets are placed
    public static String datasetsDir = "datasets";
    // path to the dataset
    public static String datasetPath = "datasets/foodmart.txt";
    // directory where the output is saved
    public static String resultsDir = "datasets/";
    // number of iterations to perform before returning the sample
    public static int numSwaps = 27478;
    public static double maxNumSwapsFactor = 5;
    // number of random sample to generate
    public static int numSamples = 2048;
    // number of random samples to generate to estimate the p-values
    public static int numEstSamples = 500;
    // number of random samples to generate to compute the adjusted critical values
    public static int numWySamples = 128;
    // minimum frequency for an itemset to be frequent
    public static double minFreq = 0.0003;
    // family wise error rate
    public static double fwer = 0.05;
    // number of threads
    public static int numThreads = 12;
    // seed for reproducibility
    public static long seed = 0;
    // whether to sample random datasets and mine their frequent itemsets,
    // in the NumFreqItemsets experiment
    public static boolean sampleAndMine = true;
    // whether to cleanup the sampled matrices and frequent itemsets
    public static boolean cleanup = true;  
    
}
