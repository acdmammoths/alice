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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SigFreqItemsetsExperimentTest {
  private static final String confsDir =
      Paths.concat(Paths.testDir, Paths.experConfsDiffusrPath);
  private static final String resultsDir =
      Paths.concat(Paths.testDir, Paths.experResultsDiffusrPath);
  private static final String datasetPath =
      Paths.concat(Paths.datasetsDir, DatasetNames.test);
  private static final Paths paths = new Paths(datasetPath, "");
  private static final String confPath =
      Paths.getJsonFilePath(confsDir, paths.datasetBaseName);

  @BeforeClass
  public static void before() {
    Paths.makeDir(confsDir);
    Paths.makeDir(resultsDir);

    final JSONObject conf = new JSONObject();
    conf.put(JsonKeys.datasetPath, datasetPath);
    conf.put(JsonKeys.numSwaps, 100);
    conf.put(JsonKeys.numEstSamples, 16);
    conf.put(JsonKeys.numWySamples, 8);
    conf.put(JsonKeys.minFreq, 0.5);
    conf.put(JsonKeys.fwer, 0.05);
    conf.put(JsonKeys.numThreads, 8);
    conf.put(JsonKeys.seed, 0);
    conf.put(JsonKeys.resultsDir, resultsDir);
    conf.put(JsonKeys.cleanup, false);

    JsonFile.write(conf, confPath);
  }

  @AfterClass
  public static void after() {
    Paths.deleteDir(Paths.testDir);
  }

  @Test
  public void sigFreqItemsetExperiment() {
    final String[] args = {confPath};
    SigFreqItemsetsExperiment.main(args);
  }
}
