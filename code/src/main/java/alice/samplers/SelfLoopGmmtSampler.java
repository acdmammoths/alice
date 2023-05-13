package alice.samplers;

import alice.helpers.Swappables;
import alice.structures.GmmtMatrix;
import alice.structures.SparseMatrix;
import alice.utils.Timer;
import java.util.Random;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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
public class SelfLoopGmmtSampler implements Sampler {

    /**
     * Samples a matrix from the uniform distribution of matrices with the same
     * row and column margins as the original matrix by using the
     * Metropolis-Hastings method by Gionis et al.
     *
     * @param inMatrix a {@link SparseMatrix} representation of the dataset
     * @param numSwaps the number of swaps to make such that the chain
     * sufficiently mixes
     * @param seed the random seed
     * @param timer a timer
     * @return the sampled matrix
     */
    @Override
    public SparseMatrix sample(SparseMatrix inMatrix, int numSwaps, long seed, Timer timer) {
        final long setupTimeStart = System.currentTimeMillis();

        final GmmtMatrix matrix = new GmmtMatrix(inMatrix);
        final Random rnd = new Random(seed);

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);

        Swappables sne;
        
        for (int i = 0; i < numSwaps; i++) {
            timer.start();
            
            sne = matrix.getRandomSwappables(rnd);
            
            if (matrix.areSwappable(sne)) {
                matrix.transition(sne);
            }
            timer.stop();
        }
        return matrix.getMatrix();
    }
    
    /**
     * Samples a matrix from the uniform distribution of matrices with the same
     * row and column margins as the original matrix by using the
     * Metropolis-Hastings method by Gionis et al.
     *
     * @param inMatrix a {@link SparseMatrix} representation of the dataset
     * @param degree degree of the matrix in the Markov graph
     * @param numSwaps the number of swaps to make such that the chain
     * sufficiently mixes
     * @param seed the random seed
     * @param timer a timer
     * @return the sampled matrix
     */
    @Override
    public SparseMatrix sample(SparseMatrix inMatrix, long degree, int numSwaps, long seed, Timer timer) {
        final long setupTimeStart = System.currentTimeMillis();

        final GmmtMatrix matrix = new GmmtMatrix(inMatrix);

        final Random rnd = new Random(seed);

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);

        Swappables sne;
        for (int i = 0; i < numSwaps; i++) {
            timer.start();
            
            sne = matrix.getRandomSwappables(rnd);
            
            if (matrix.areSwappable(sne)) {
                matrix.transition(sne);
            }
            timer.stop();
        }
        return matrix.getMatrix();
    }

    /**
     * Samples a matrix from the uniform distribution of matrices with the same
     * row and column margins as the original matrix by using the
     * Metropolis-Hastings method by Gionis et al.
     *
     * @param inMatrix a {@link SparseMatrix} representation of the dataset
     * @param numSwaps the number of swaps to make such that the chain
     * sufficiently mixes
     * @param seed the random seed
     * @param timer a timer
     * @param stats stores stats on BJDM 
     * @param catNum stores the number of caterpillars
     * @return the sampled matrix
     */
    @Override
    public SparseMatrix sample(SparseMatrix inMatrix, 
            int numSwaps, 
            long seed, 
            Timer timer, 
            DescriptiveStatistics stats,
            DescriptiveStatistics catNum) {
        
        final long setupTimeStart = System.currentTimeMillis();

        final GmmtMatrix matrix = new GmmtMatrix(inMatrix);

        final Random rnd = new Random(seed);

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);
        
        // starting BJDM vector
        double[] start = matrix.getBJDMVector(true);

        Swappables sne;
        for (int i = 0; i < numSwaps; i++) {
            timer.start();
            
            sne = matrix.getRandomSwappables(rnd);
            
            if (matrix.areSwappable(sne)) {
                matrix.transition(sne);
            }
            timer.stop();
            
            if (i % 100 == 0) {
                double distance = matrix.getDistanceFrom(start, true);
                stats.addValue(distance);
            } 
        }
        catNum.addValue(matrix.getNumZstructs());
        
        return matrix.getMatrix();
    
    }

}
