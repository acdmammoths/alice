package caterpillars.utils;

import caterpillars.config.DatasetNames;

public class Config {
    
    public static String datasetsDir = "datasets";
    public static String datasetPath = "datasets/" + DatasetNames.foodmart;
    public static String resultsDir = "datasets";
    public static String samplerType;
    public static int numSwaps = 1000;
    public static double maxNumSwapsFactor = 5;
    public static int numSamples;
    public static int numEstSamples;
    public static int numWySamples;
    public static double minFreq = 0.0003;
    public static double fwer;
    public static int numThreads = 8;
    public static long seed = 1;
    public static boolean sampleAndMine;
    public static boolean cleanup = true;    
}
