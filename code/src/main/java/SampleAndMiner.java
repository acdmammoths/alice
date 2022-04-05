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
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A class to sample transactional datasets using DiFfuSR or GMMT and then mine sampled datasets for
 * frequent itemsets.
 */
class SampleAndMiner {
  /**
   * Samples transactional datasets using the sampler and mines the sampled datasets for frequent
   * itemsets. The sampling and mining are done in parallel.
   *
   * @param datasetPath the path to the dataset
   * @param sampler the sampler to use for sampling datasets
   * @param numSwaps the number of swaps to use for sampling
   * @param numSamples the number of samples to obtain
   * @param minFreq the minimum frequency threshold for mining frequent itemsets
   * @param seed a random seed for replication
   * @param resultsDir the directory to store the resulting sampled datasets and frequent itemsets
   */
  static void sampleAndMine(
      String datasetPath,
      Sampler sampler,
      int numSwaps,
      int numSamples,
      double minFreq,
      int numThreads,
      long seed,
      String resultsDir) {
    final Transformer transformer = new Transformer();
    final SparseMatrix matrix = transformer.createMatrix(datasetPath);
    final Paths paths = new Paths(datasetPath, resultsDir);
    final Random rnd = new Random(seed);
    final ExecutorService pool = Executors.newFixedThreadPool(numThreads);

    Paths.makeDir(paths.samplesPath);
    Paths.makeDir(paths.freqItemsetsDirPath);

    for (int i = 0; i < numSamples; i++) {
      final String samplePath = paths.getSamplePath("-ds", i);
      final String freqItemsetsPath = paths.getFreqItemsetsPath("-fis", i);
      final SampleAndMineTask sampleTask =
          new SampleAndMineTask(
              sampler,
              transformer,
              matrix,
              numSwaps,
              rnd.nextLong(),
              minFreq,
              samplePath,
              freqItemsetsPath);
      pool.execute(sampleTask);
    }

    pool.shutdown();
    try {
      pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      System.err.println("Error executing sample and mine tasks");
      e.printStackTrace();
      System.exit(1);
    }
  }
}
