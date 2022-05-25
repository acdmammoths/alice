package caterpillars.samplers;


import caterpillars.structures.SparseMatrix;
import diffusr.samplers.Sampler;
import caterpillars.utils.Timer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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
 * A class that helps sample transactional datasets in parallel.
 */
public class SampleTask implements Runnable {

    /**
     * The sampler used to sample matrices.
     */
    private final Sampler sampler;

    /**
     * The matrix of the observed dataset.
     */
    private final SparseMatrix matrix;

    /**
     * The number of swaps to perform.
     */
    private final int numSwaps;

    /**
     * The random seed to use for replication.
     */
    private final long seed;
    
    /**
     * Identifier run.
     */
    private final int id;
    
    /**
     * The timer to store the step times.
     */
    private final Timer timer;
    
    /**
     * The object to store BJDM distances.
     */
    private final DescriptiveStatistics stats;
    

    public SampleTask(
            Sampler sampler,
            SparseMatrix matrix,
            int numSwaps,
            long seed,
            int id,
            Timer timer,
            DescriptiveStatistics stats) {
        this.sampler = sampler;
        this.matrix = matrix;
        this.numSwaps = numSwaps;
        this.seed = seed;
        this.id = id;
        this.timer = timer;
        this.stats = stats;
    }

    @Override
    public void run() {
        this.sampler.sample(this.matrix, this.numSwaps, this.seed, this.timer, this.stats);
        System.out.println(this.sampler.getClass().toString() + ": sample " + this.id + " created.");
    }
    
    public DescriptiveStatistics getStats() {
        return stats;
    }
    
    public Timer getTimes() {
        return timer;
    }
}
