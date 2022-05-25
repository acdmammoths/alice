package caterpillars.samplers;

import caterpillars.structures.BJDMMatrix;
import caterpillars.helpers.SwappableAndNewEdges;
import caterpillars.structures.SparseMatrix;
import caterpillars.helpers.LogNumEquivMatricesTracker;
import caterpillars.structures.Vector;
import caterpillars.structures.Edge;
import diffusr.samplers.Sampler;
import caterpillars.utils.Timer;
import java.util.Random;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class NaiveBJDMSampler implements Sampler {

    /**
     * @param inMatrix a {@link SparseMatrix} representation of the dataset
     * @param numSwaps the number of swaps to make such that the chain
     * sufficiently mixes
     * @param seed the random seed
     * @param timer a timer
     * @return the matrix representation of the sampled dataset
     */
    @Override
    public SparseMatrix sample(SparseMatrix inMatrix, int numSwaps, long seed, Timer timer) {
        final long setupTimeStart = System.currentTimeMillis();

        final BJDMMatrix matrix = new BJDMMatrix(inMatrix);

        final Random rnd = new Random(seed);

        double logNumEquivMatrices = matrix.getLogNumEquivMatrices();

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);

        for (int i = 0; i < numSwaps; i++) {
            timer.start();

            final SwappableAndNewEdges sne = matrix.getSwappableAndNewEdges(rnd);
            
            if (sne == null) {
                continue;
            }
            
            final Edge swappableEdge1 = sne.swappableEdge1;
            final Edge swappableEdge2 = sne.swappableEdge2;
            final Edge newEdge1 = sne.newEdge1;
            final Edge newEdge2 = sne.newEdge2;
            final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
            final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
            final Vector[] newRows = matrix.getNewRows(newEdge1, newEdge2);
            final Vector newRow1 = newRows[0];
            final Vector newRow2 = newRows[1];

            final double logNumEquivAdjMatrices
                    = matrix.getLogNumEquivAdjMatrices(
                            logNumEquivMatrices, swappableRow1, swappableRow2, newRow1, newRow2);

            final double frac = Math.exp(logNumEquivMatrices - logNumEquivAdjMatrices);
            final double acceptanceProb = Math.min(1, frac);

            if (rnd.nextDouble() <= acceptanceProb) {
                matrix.transition(
                        swappableEdge1, swappableEdge2,
                        newEdge1, newEdge2,
                        swappableRow1, swappableRow2,
                        newRow1, newRow2);

                logNumEquivMatrices = logNumEquivAdjMatrices;
            }
            timer.stop();
        }

        return matrix.getMatrix();
    }

    /**
     * Saves the natural logarithm of the number of equivalent matrices for each
     * matrix the sampler reaches and always transitions to the adjacent matrix.
     * We always transition to the adjacent matrix so that the number of data
     * points we save is equal to the number of swaps (assuming there are no
     * hash collisions when saving the data points). This method is used
     * specifically for {@link DistortionExperiment}.
     *
     * @param inMatrix a {@link SparseMatrix} representation of the dataset
     * @param numSwaps the number of swaps to make such that the chain
     * sufficiently mixes
     * @param seed the random seed
     * @param logNumEquivMatricesTracker a tracker for the log of the number of
     * equivalent matrices
     */
    static void sample(
            SparseMatrix inMatrix,
            int numSwaps,
            long seed,
            LogNumEquivMatricesTracker logNumEquivMatricesTracker) {
        
        final BJDMMatrix matrix = new BJDMMatrix(inMatrix);

        final Random rnd = new Random(seed);

        double logNumEquivMatrices = matrix.getLogNumEquivMatrices();

        for (int i = 0; i < numSwaps; i++) {
            logNumEquivMatricesTracker.save(matrix, logNumEquivMatrices);

            final SwappableAndNewEdges sne = matrix.getSwappableAndNewEdges(rnd);
            
            if (sne == null) {
                continue;
            }
            
            final Edge swappableEdge1 = sne.swappableEdge1;
            final Edge swappableEdge2 = sne.swappableEdge2;
            final Edge newEdge1 = sne.newEdge1;
            final Edge newEdge2 = sne.newEdge2;
            final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
            final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
            final Vector[] newRows = matrix.getNewRows(newEdge1, newEdge2);
            final Vector newRow1 = newRows[0];
            final Vector newRow2 = newRows[1];

            final double logNumEquivAdjMatrices
                    = matrix.getLogNumEquivAdjMatrices(
                            logNumEquivMatrices, swappableRow1, swappableRow2, newRow1, newRow2);

            matrix.transition(
                    swappableEdge1, swappableEdge2,
                    newEdge1, newEdge2,
                    swappableRow1, swappableRow2,
                    newRow1, newRow2);

            logNumEquivMatrices = logNumEquivAdjMatrices;
        }
    }

    @Override
    public SparseMatrix sample(SparseMatrix inMatrix, 
            int numSwaps, 
            long seed, 
            Timer timer, 
            DescriptiveStatistics stats) {
        
        final long setupTimeStart = System.currentTimeMillis();

        final BJDMMatrix matrix = new BJDMMatrix(inMatrix);

        final Random rnd = new Random(seed);

        double logNumEquivMatrices = matrix.getLogNumEquivMatrices();

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);
        
        // starting BJDM vector
        double[] start = matrix.getBJDMVector(true);

        for (int i = 0; i < numSwaps; i++) {
            timer.start();

            final SwappableAndNewEdges sne = matrix.getSwappableAndNewEdges(rnd);
            
            if (sne == null) {
                continue;
            }
            
            final Edge swappableEdge1 = sne.swappableEdge1;
            final Edge swappableEdge2 = sne.swappableEdge2;
            final Edge newEdge1 = sne.newEdge1;
            final Edge newEdge2 = sne.newEdge2;
            final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
            final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
            final Vector[] newRows = matrix.getNewRows(newEdge1, newEdge2);
            final Vector newRow1 = newRows[0];
            final Vector newRow2 = newRows[1];

            final double logNumEquivAdjMatrices
                    = matrix.getLogNumEquivAdjMatrices(
                            logNumEquivMatrices, swappableRow1, swappableRow2, newRow1, newRow2);

            final double frac = Math.exp(logNumEquivMatrices - logNumEquivAdjMatrices);
            final double acceptanceProb = Math.min(1, frac);

            if (rnd.nextDouble() <= acceptanceProb) {
                matrix.transition(
                        swappableEdge1, swappableEdge2,
                        newEdge1, newEdge2,
                        swappableRow1, swappableRow2,
                        newRow1, newRow2);

                logNumEquivMatrices = logNumEquivAdjMatrices;
            }
            timer.stop();
            
            if (i % 100 == 0) {
                double distance = matrix.getDistanceFrom(start, true);
                stats.addValue(distance);
            } 
        }

        return matrix.getMatrix();
    
    }
}