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
 * This class is a driver for sampling datasets using DiFfuSR or GMMT as well as mining the sampled
 * datasets.
 */
public class Driver {
  /** Runs the driver. */
  public static void main(String[] args) {
    final String datasetPath = args[0];
    final String samplerType = args[1];
    final int numSwaps = Integer.parseInt(args[2]);
    final int numSamples = Integer.parseInt(args[3]);
    final double minFreq = Double.parseDouble(args[4]);
    final int numThreads = Integer.parseInt(args[5]);
    final long seed = Long.parseLong(args[6]);
    final String resultsDir = args[7];

    Sampler sampler = null;
    if (samplerType.equals(SamplerNames.gmmtSampler)) {
      sampler = new GmmtSampler();
    } else if (samplerType.equals(SamplerNames.naiveSampler)) {
      sampler = new NaiveSampler();
    } else if (samplerType.equals(SamplerNames.refinedSampler)) {
      sampler = new RefinedSampler();
    } else {
      System.err.println("Unknown sample type " + samplerType);
      System.exit(1);
    }

    System.out.println("Running driver with arguments:");
    System.out.println("\t" + JsonKeys.datasetPath + ": " + datasetPath);
    System.out.println("\t" + JsonKeys.sampler + ": " + samplerType);
    System.out.println("\t" + JsonKeys.numSwaps + ": " + numSwaps);
    System.out.println("\t" + JsonKeys.numSamples + ": " + numSamples);
    System.out.println("\t" + JsonKeys.minFreq + ": " + minFreq);
    System.out.println("\t" + JsonKeys.numThreads + ": " + numThreads);
    System.out.println("\t" + JsonKeys.seed + ": " + seed);
    System.out.println("\t" + JsonKeys.resultsDir + ": " + resultsDir);

    SampleAndMiner.sampleAndMine(
        datasetPath, sampler, numSwaps, numSamples, minFreq, numThreads, seed, resultsDir);
  }
}
