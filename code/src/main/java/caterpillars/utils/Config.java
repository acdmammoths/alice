package caterpillars.utils;

public class Config {
    
    public static String datasetsDir = "datasets";
    public static String datasetPath = "datasets/foodmart.txt";
    public static String resultsDir = "datasets/";
    public static String samplerType;
    public static int numSwaps = 27478;
    public static double maxNumSwapsFactor = 5;
    public static int numSamples = 2048;
    public static int numEstSamples = 500;
    public static int numWySamples = 128;
    public static double minFreq = 0.0003;
    public static double fwer = 0.05;
    public static int numThreads = 12;
    public static long seed = 0;
    public static boolean sampleAndMine = true;
    public static boolean cleanup = true;  
    
}
