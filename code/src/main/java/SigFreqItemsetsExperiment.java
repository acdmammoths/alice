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
import org.json.JSONObject;

/** This class runs the significant frequent itemsets experiment. */
public class SigFreqItemsetsExperiment {
  public static void main(String[] args) {
    final String confPath = args[0];
    run(confPath);
  }

  private static void run(String confPath) {
    System.out.println("Reading configuration file at " + confPath);

    final JSONObject conf = JsonFile.read(confPath);
    final String datasetPath = conf.getString(JsonKeys.datasetPath);
    final int numSwaps = conf.getInt(JsonKeys.numSwaps);
    final int numEstSamples = conf.getInt(JsonKeys.numEstSamples);
    final int numWySamples = conf.getInt(JsonKeys.numWySamples);
    final double minFreq = conf.getDouble(JsonKeys.minFreq);
    final double fwer = conf.getDouble(JsonKeys.fwer);
    final int numThreads = conf.getInt(JsonKeys.numThreads);
    final long seed = conf.getLong(JsonKeys.seed);
    final String resultsDir = conf.getString(JsonKeys.resultsDir);
    final boolean cleanup = conf.getBoolean(JsonKeys.cleanup);

    final Sampler[] samplers = {new NaiveSampler(), new RefinedSampler(), new GmmtSampler()};

    for (Sampler sampler : samplers) {
      SigFreqItemsetMiner sigFreqItemsetMiner =
          new SigFreqItemsetMiner(
              datasetPath,
              sampler,
              numSwaps,
              numEstSamples,
              numWySamples,
              minFreq,
              fwer,
              numThreads,
              seed,
              Paths.concat(resultsDir, sampler.getClass().getName()),
              cleanup);
      sigFreqItemsetMiner.mine();
    }
  }
}
