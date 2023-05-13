package alice.samplers;

import alice.helpers.Swappables;
import alice.structures.BJDMMatrix;
import alice.structures.Edge;
import alice.structures.SparseMatrix;
import alice.structures.Vector;
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
public class SelfLoopBJDMSampler implements Sampler {

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

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);

        Swappables sne;
        
        for (int i = 0; i < numSwaps; i++) {
            timer.start();
            
            sne = matrix.getRandomSwappables(rnd);
            
            final Edge swappableEdge1 = sne.swappableEdge1;
            final Edge swappableEdge2 = sne.swappableEdge2;
            
            if (matrix.areSwappable(sne)) {
                final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
                final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
                final Edge newEdge1 = new Edge(swappableEdge1.row, swappableEdge2.col);
                final Edge newEdge2 = new Edge(swappableEdge2.row, swappableEdge1.col);
                final Vector[] newRows = matrix.getNewRows(newEdge1, newEdge2);
                final Vector newRow1 = newRows[0];
                final Vector newRow2 = newRows[1];
                matrix.transition(sne,
                        swappableRow1, swappableRow2,
                        newRow1, newRow2);
            }
            timer.stop();
        }
        return matrix.getMatrix();
    }
    
    /**
     * 
     * @param inMatrix a {@link SparseMatrix} representation of the dataset
     * @param degree degree of the matrix in the Markov graph
     * @param numSwaps the number of swaps to make such that the chain
     * sufficiently mixes
     * @param seed the random seed
     * @param timer a timer
     * @return the matrix representation of the sampled dataset
     */
    @Override
    public SparseMatrix sample(SparseMatrix inMatrix, long degree, int numSwaps, long seed, Timer timer) {
        final long setupTimeStart = System.currentTimeMillis();

        final BJDMMatrix matrix = new BJDMMatrix(inMatrix);

        final Random rnd = new Random(seed);

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);

        Swappables sne;
        for (int i = 0; i < numSwaps; i++) {
            timer.start();
            
            sne = matrix.getRandomSwappables(rnd);
            
            final Edge swappableEdge1 = sne.swappableEdge1;
            final Edge swappableEdge2 = sne.swappableEdge2;
            
            if (matrix.areSwappable(sne)) {
                final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
                final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
                final Edge newEdge1 = new Edge(swappableEdge1.row, swappableEdge2.col);
                final Edge newEdge2 = new Edge(swappableEdge2.row, swappableEdge1.col);
                final Vector[] newRows = matrix.getNewRows(newEdge1, newEdge2);
                final Vector newRow1 = newRows[0];
                final Vector newRow2 = newRows[1];
                matrix.transition(sne,
                        swappableRow1, swappableRow2,
                        newRow1, newRow2);
            }
            timer.stop();
        }
        return matrix.getMatrix();
    }

    /**
     * @param inMatrix a {@link SparseMatrix} representation of the dataset
     * @param numSwaps the number of swaps to make such that the chain
     * sufficiently mixes
     * @param seed the random seed
     * @param timer a timer
     * @param stats stores the distances between consecutive BJDMs.
     * @param catNum stores the number of caterpillars
     * @return the matrix representation of the sampled dataset
     */
    @Override
    public SparseMatrix sample(SparseMatrix inMatrix, 
            int numSwaps, 
            long seed, 
            Timer timer, 
            DescriptiveStatistics stats,
            DescriptiveStatistics catNum) {
        
        final long setupTimeStart = System.currentTimeMillis();

        final BJDMMatrix matrix = new BJDMMatrix(inMatrix);

        final Random rnd = new Random(seed);

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);
        
        // starting BJDM vector
        double[] start = matrix.getBJDMVector(true);

        Swappables sne;
        for (int i = 0; i < numSwaps; i++) {
            timer.start();
            
            sne = matrix.getRandomSwappables(rnd);
            
            final Edge swappableEdge1 = sne.swappableEdge1;
            final Edge swappableEdge2 = sne.swappableEdge2;
            
            if (matrix.areSwappable(sne)) {
                final Vector swappableRow1 = matrix.getRowInstance(swappableEdge1.row);
                final Vector swappableRow2 = matrix.getRowInstance(swappableEdge2.row);
                final Edge newEdge1 = new Edge(swappableEdge1.row, swappableEdge2.col);
                final Edge newEdge2 = new Edge(swappableEdge2.row, swappableEdge1.col);
                final Vector[] newRows = matrix.getNewRows(newEdge1, newEdge2);
                final Vector newRow1 = newRows[0];
                final Vector newRow2 = newRows[1];
                matrix.transition(sne,
                        swappableRow1, swappableRow2,
                        newRow1, newRow2);
            }
            timer.stop();
            
            if (i % 100 == 0) {
                double distance = matrix.getDistanceFrom(start, true);
                stats.addValue(distance);
            } 
        }
        catNum.addValue(matrix.getNumCaterpillars());
        
        return matrix.getMatrix();
    
    }

}
