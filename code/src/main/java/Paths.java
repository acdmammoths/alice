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
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/** A helper class to construct file and directory paths. */
class Paths {
  private static final String textExt = ".txt";
  private static final String jsonExt = ".json";
  static final String estTag = "-est";
  static final String wyTag = "-wy";

  private static final String experimentsDir = "experiments";
  private static final String runtimeDir = "runtime";
  private static final String convergenceDir = "convergence";
  private static final String distortionDir = "distortion";
  private static final String numFreqItemsetsDir = "numFreqItemsets";
  private static final String diffusrDir = "diffusr";

  private static final String experConfsPath = concat(experimentsDir, "confs");
  static final String experConfsRuntimePath = concat(experConfsPath, runtimeDir);
  static final String experConfsConvergencePath = concat(experConfsPath, convergenceDir);
  static final String experConfsDistortionPath = concat(experConfsPath, distortionDir);
  static final String experConfsNumFreqItemsetsPath = concat(experConfsPath, numFreqItemsetsDir);
  static final String experConfsDiffusrPath = concat(experConfsPath, diffusrDir);

  private static final String experResultsPath = concat(experimentsDir, "results");
  static final String experResultsRuntimePath = concat(experResultsPath, runtimeDir);
  static final String experResultsConvergencePath = concat(experResultsPath, convergenceDir);
  static final String experResultsDistortionPath = concat(experResultsPath, distortionDir);
  static final String experResultsNumFreqItemsetsPath =
      concat(experResultsPath, numFreqItemsetsDir);
  static final String experResultsDiffusrPath = concat(experResultsPath, diffusrDir);

  static final String datasetsDir = "datasets";
  static final String testDir = "test";

  final String datasetPath;
  final String datasetBaseName;
  final String resultsDir;
  final String samplesPath;
  final String freqItemsetsDirPath;
  final String sigFreqItemsetsDirPath;

  Paths(String datasetPath, String resultsDir) {
    this.datasetPath = datasetPath;
    this.datasetBaseName = getBaseName(datasetPath);
    this.resultsDir = resultsDir;
    this.samplesPath = concat(resultsDir, "samples");
    this.freqItemsetsDirPath = concat(resultsDir, "freqItemsets");
    this.sigFreqItemsetsDirPath = concat(resultsDir, "sigFreqItemsets");
  }

  String getSamplePath(String tag, int id) {
    return getTextFilePath(this.samplesPath, this.getSampleBaseFileName(tag, id));
  }

  String getFreqItemsetsPath(String tag, int id) {
    return getTextFilePath(this.freqItemsetsDirPath, this.getSampleBaseFileName(tag, id));
  }

  String getSigFreqItemsetsPath(
      Sampler sampler,
      int numSwaps,
      int numEstSamples,
      int numWySamples,
      double minFreq,
      double fwer,
      int numThreads,
      long seed) {
    final String sigFreqItemsetsBaseName =
        String.join(
            Delimiters.dash,
            this.datasetBaseName,
            sampler.getClass().getName(),
            String.valueOf(numSwaps),
            String.valueOf(numEstSamples),
            String.valueOf(numWySamples),
            String.valueOf(minFreq),
            String.valueOf(fwer),
            String.valueOf(numThreads),
            String.valueOf(seed));
    return getJsonFilePath(this.sigFreqItemsetsDirPath, sigFreqItemsetsBaseName);
  }

  private String getSampleBaseFileName(String tag, int id) {
    final String prefix = this.datasetBaseName + tag;
    return appendId(prefix, id);
  }

  private static String appendId(String prefix, int id) {
    return String.join(Delimiters.dash, prefix, String.valueOf(id));
  }

  private static String getBaseName(String fileName) {
    return FilenameUtils.getBaseName(fileName);
  }

  static String concat(String basePath, String fullFileNameToAdd) {
    return FilenameUtils.concat(basePath, fullFileNameToAdd);
  }

  private static String getTextFilePath(String dir, String baseFileName) {
    return concat(dir, baseFileName + textExt);
  }

  static String getJsonFilePath(String dir, String baseFileName) {
    return concat(dir, baseFileName + jsonExt);
  }

  static void makeDir(String dirPath) {
    try {
      FileUtils.forceMkdir(new File(dirPath));
    } catch (IOException e) {
      System.err.println("Error in making directory " + dirPath);
      e.printStackTrace();
      System.exit(1);
    }
  }

  static void deleteDir(String dirPath) {
    try {
      final File dirToDelete = new File(dirPath);
      FileUtils.deleteDirectory(dirToDelete);
    } catch (IOException e) {
      System.err.println("Error in deleting directory " + dirPath);
      e.printStackTrace();
      System.exit(1);
    }
  }
}
