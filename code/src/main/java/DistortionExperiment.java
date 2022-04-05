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
import java.time.LocalDateTime;
import org.json.JSONObject;

/**
 * This class runs the distortion experiment by measuring the quartiles of the log of the number of
 * equivalent matrices across the null space of matrices.
 */
public class DistortionExperiment {
  public static void main(String[] args) {
    final String confPath = args[0];
    run(confPath);
  }

  private static void run(String confPath) {
    System.out.println("Reading configuration file at " + confPath);

    final JSONObject conf = JsonFile.read(confPath);
    final String datasetPath = conf.getString(JsonKeys.datasetPath);
    final int numSwaps = conf.getInt(JsonKeys.numSwaps);
    final long seed = conf.getLong(JsonKeys.seed);
    final String resultsDir = conf.getString(JsonKeys.resultsDir);

    Paths.makeDir(resultsDir);

    System.out.println("Executing distortion experiment for dataset at " + datasetPath);

    final SparseMatrix matrix = new Transformer().createMatrix(datasetPath);

    final LogNumEquivMatricesTracker logNumEquivMatricesTracker = new LogNumEquivMatricesTracker();
    NaiveSampler.sample(matrix, numSwaps, seed, logNumEquivMatricesTracker);

    logNumEquivMatricesTracker.initStats();
    final int saveCount = logNumEquivMatricesTracker.saveCount;
    final long numSamples = logNumEquivMatricesTracker.getNumSamples();
    final double minLogNumEquivMatrices = logNumEquivMatricesTracker.getMin();
    final double q1LogNumEquivMatrices = logNumEquivMatricesTracker.getPercentile(25);
    final double medianLogNumEquivMatrices = logNumEquivMatricesTracker.getPercentile(50);
    final double q3LogNumEquivMatrices = logNumEquivMatricesTracker.getPercentile(75);
    final double maxLogNumEquivMatrices = logNumEquivMatricesTracker.getMax();

    System.out.println("\t" + JsonKeys.logNumEquivMatricesStats + ":");
    System.out.println("\t\t" + JsonKeys.saveCount + ": " + saveCount);
    System.out.println("\t\t" + JsonKeys.numSamples + ": " + numSamples);
    System.out.println("\t\t" + JsonKeys.minLogNumEquivMatrices + ": " + minLogNumEquivMatrices);
    System.out.println("\t\t" + JsonKeys.q1LogNumEquivMatrices + ": " + q1LogNumEquivMatrices);
    System.out.println(
        "\t\t" + JsonKeys.medianLogNumEquivMatrices + ": " + medianLogNumEquivMatrices);
    System.out.println("\t\t" + JsonKeys.q3LogNumEquivMatrices + ": " + q3LogNumEquivMatrices);
    System.out.println("\t\t" + JsonKeys.maxLogNumEquivMatrices + ": " + maxLogNumEquivMatrices);

    // create object for logNumEquivMatricesStats
    final JSONObject logNumEquivMatricesStats = new JSONObject();
    logNumEquivMatricesStats.put(JsonKeys.numSamples, numSamples);
    logNumEquivMatricesStats.put(JsonKeys.minLogNumEquivMatrices, minLogNumEquivMatrices);
    logNumEquivMatricesStats.put(JsonKeys.q1LogNumEquivMatrices, q1LogNumEquivMatrices);
    logNumEquivMatricesStats.put(JsonKeys.medianLogNumEquivMatrices, medianLogNumEquivMatrices);
    logNumEquivMatricesStats.put(JsonKeys.q3LogNumEquivMatrices, q3LogNumEquivMatrices);
    logNumEquivMatricesStats.put(JsonKeys.maxLogNumEquivMatrices, maxLogNumEquivMatrices);

    // create object for runInfo
    final JSONObject runInfo = new JSONObject();
    runInfo.put(JsonKeys.args, conf);
    runInfo.put(JsonKeys.saveCount, saveCount);
    runInfo.put(JsonKeys.timestamp, LocalDateTime.now());

    // create base object
    final JSONObject results = new JSONObject();
    results.put(JsonKeys.runInfo, runInfo);
    results.put(JsonKeys.logNumEquivMatricesStats, logNumEquivMatricesStats);

    // save JSON
    final String datasetBaseName = new Paths(datasetPath, "").datasetBaseName;
    final String resultsBaseName =
        String.join(
            Delimiters.dash, datasetBaseName, String.valueOf(numSwaps), String.valueOf(seed));
    final String resultsPath = Paths.getJsonFilePath(resultsDir, resultsBaseName);
    JsonFile.write(results, resultsPath);

    System.out.println("Result written to " + resultsPath);
  }
}
