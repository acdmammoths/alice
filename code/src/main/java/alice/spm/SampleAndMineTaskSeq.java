package alice.spm;


import alice.samplers.SeqSampler;
import alice.structures.MultiGraph;
import alice.utils.Transformer;
import alice.utils.Timer;

/*
 * Copyright (C) 2023 Giulia Preti
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
 * A class that helps sample sequence datasets and mines frequent sequence
 * patterns from those datasets in parallel.
 */
public class SampleAndMineTaskSeq implements Runnable {

    /**
     * The sampler used to sample multigraphs.
     */
    private final SeqSampler sampler;

    /**
     * The transformer to create the dataset from the multigraph.
     */
    private final Transformer transformer;

    /**
     * The multigraph of the observed dataset.
     */
    private final MultiGraph matrix;
    
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
     * patterns.
     */
    private final double minFreq;

    /**
     * The path to save the sampled sequence dataset.
     */
    private final String samplePath;

    /**
     * The path to save the set of frequent sequence patterns.
     */
    private final String freqItemsetsPath;

    public SampleAndMineTaskSeq(
            SeqSampler sampler,
            Transformer transformer,
            MultiGraph matrix,
            int numSwaps,
            long seed,
            double minFreq,
            String samplePath,
            String freqItemsetsPath) {
        
        this.sampler = sampler;
        this.transformer = transformer;
        this.matrix = matrix;
        this.numSwaps = numSwaps;
        this.seed = seed;
        this.minFreq = minFreq;
        this.samplePath = samplePath;
        this.freqItemsetsPath = freqItemsetsPath;
    }

    @Override
    public void run() {
        final MultiGraph sample
                = this.sampler.sample(this.matrix, this.numSwaps, this.seed, new Timer(false));
        this.transformer.createSequenceDataset(this.samplePath, sample);
        System.out.println("Sample created: " + this.samplePath);

        FreqSequenceMiner.mine(this.samplePath, this.minFreq, this.freqItemsetsPath);
        System.out.println("Frequent sequences mined: " + this.freqItemsetsPath);
    }
}
