package caterpillars.utils;

import caterpillars.config.DatasetNames;

public class Config {
    
    public static String datasetsDir = "datasets";
    public static String datasetPath = "datasets/" + "retail.txt";
    public static String resultsDir = "datasets";
    public static String samplerType;
    public static int numSwaps = 193568 /*retail=193568 pumsb=3629404*/;
    public static double maxNumSwapsFactor = 5;
    public static int numSamples = 1568;
    public static int numEstSamples = 1568;
    public static int numWySamples = 128;
    public static double minFreq = 0.002 /* retail=0.002, pumsb=0.9*/;
    public static double fwer = 0.05;
    public static int numThreads = 8;
    public static long seed = 0;
    public static boolean sampleAndMine = true;
    public static boolean cleanup = true;    
}
