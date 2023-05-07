package alice.fpm;


import alice.structures.SparseMatrix;
import alice.samplers.Sampler;
import alice.utils.Transformer;
import alice.utils.Timer;

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
/**
 * A class that helps sample transactional datasets and mines frequent itemsets
 * from those datasets in parallel.
 */
public class SampleAndMineTask implements Runnable {

    /**
     * The sampler used to sample matrices.
     */
    private final Sampler sampler;

    /**
     * The transformer to create the dataset from the matrix.
     */
    private final Transformer transformer;

    /**
     * The matrix of the observed dataset.
     */
    private final SparseMatrix matrix;
    
    /**
     * The degree of the matrix.
     */
    private final long degree;

    /**
     * The number of swaps to perform.
     */
    private final int numSwaps;

    /**
     * The random seed to use for replication.
     */
    private final long seed;

    /**
     * The minimum frequency threshold used to mine the set of frequent
     * itemsets.
     */
    private final double minFreq;

    /**
     * The path to save the sampled transactional dataset.
     */
    private final String samplePath;

    /**
     * The path to save the set of frequent itemsets.
     */
    private final String freqItemsetsPath;

    public SampleAndMineTask(
            Sampler sampler,
            Transformer transformer,
            SparseMatrix matrix,
            long degree,
            int numSwaps,
            long seed,
            double minFreq,
            String samplePath,
            String freqItemsetsPath) {
        this.sampler = sampler;
        this.transformer = transformer;
        this.matrix = matrix;
        this.degree = degree;
        this.numSwaps = numSwaps;
        this.seed = seed;
        this.minFreq = minFreq;
        this.samplePath = samplePath;
        this.freqItemsetsPath = freqItemsetsPath;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        final SparseMatrix sample
                = this.sampler.sample(this.matrix, this.degree, this.numSwaps, this.seed, new Timer(false));
        this.transformer.createDataset(this.samplePath, sample);
        System.out.println("Sample created: " + this.samplePath + " in " + (System.currentTimeMillis() - start));

        FreqItemsetMiner.mine(this.samplePath, this.minFreq, this.freqItemsetsPath);
        System.out.println("Frequent itemsets mined: " + this.freqItemsetsPath);
    }
}
