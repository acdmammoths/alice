package alice.utils;


/**
 *
 * @author giulia
 */
public class Utils {
    
    /**
     * 
     * @param numbers array of doubles
     * @return cumulative sum of numbers
     */
    public static double[] cumSum(double[] numbers) {
        double[] cumsum = new double[numbers.length];
        double sum = 0.;
        for (int i = 0; i < numbers.length; i++) {
            sum += numbers[i];
            cumsum[i] = sum;
        }
        return cumsum;
    }
}
