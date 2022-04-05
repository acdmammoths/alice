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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class NumFreqItemsetsExperimentTest {
  private static final String confsDir =
      Paths.concat(Paths.testDir, Paths.experConfsNumFreqItemsetsPath);
  private static final String resultsDir =
      Paths.concat(Paths.testDir, Paths.experResultsNumFreqItemsetsPath);
  private static final String datasetPath =
      Paths.concat(Paths.datasetsDir, DatasetNames.test);
  private static final Paths paths = new Paths(datasetPath, resultsDir);
  private static final String confPath =
      Paths.getJsonFilePath(confsDir, paths.datasetBaseName);
  private static final String freqItemsetsFilePath = paths.getFreqItemsetsPath("", 0);

  @BeforeClass
  public static void before() throws IOException {
    Paths.makeDir(confsDir);
    Paths.makeDir(paths.freqItemsetsDirPath);

    final JSONObject conf = new JSONObject();
    conf.put(JsonKeys.datasetPath, datasetPath);
    conf.put(JsonKeys.numSwaps, 100);
    conf.put(JsonKeys.numSamples, 20);
    conf.put(JsonKeys.minFreq, 0.5);
    conf.put(JsonKeys.numThreads, 8);
    conf.put(JsonKeys.seed, 0);
    conf.put(JsonKeys.resultsDir, resultsDir);
    conf.put(JsonKeys.sampleAndMine, true);

    JsonFile.write(conf, confPath);

    final BufferedWriter bw = new BufferedWriter(new FileWriter(freqItemsetsFilePath));
    bw.write("1 2 3" + Delimiters.sup + "2");
    bw.newLine();
    bw.write("2 3 4" + Delimiters.sup + "4");
    bw.newLine();
    bw.write("1" + Delimiters.sup + "3");
    bw.newLine();
    bw.close();
  }

  @AfterClass
  public static void after() {
    Paths.deleteDir(Paths.testDir);
  }

  @Test
  public void numFreqItemsetsExperiment() {
    final String[] args = {confPath};
    NumFreqItemsetsExperiment.main(args);
  }

  @Test
  public void getFreqItemsetLenToCountMap() {
    final Set<Set<Integer>> freqItemsets = new HashSet<>();
    freqItemsets.add(new HashSet<>(Arrays.asList(1, 2, 3)));
    freqItemsets.add(new HashSet<>(Arrays.asList(2, 3, 4)));
    freqItemsets.add(new HashSet<>(Arrays.asList(1)));

    final Map<Integer, Integer> expectedFreqItemsetLenToCount = new HashMap<>();
    expectedFreqItemsetLenToCount.put(3, 2);
    expectedFreqItemsetLenToCount.put(1, 1);

    Map<Integer, Integer> actualFreqItemsetLenTocount =
        NumFreqItemsetsExperiment.getFreqItemsetLenToCountMap(freqItemsets);
    Assert.assertEquals(expectedFreqItemsetLenToCount, actualFreqItemsetLenTocount);

    actualFreqItemsetLenTocount =
        NumFreqItemsetsExperiment.getFreqItemsetLenToCountMap(new File(freqItemsetsFilePath));
    Assert.assertEquals(expectedFreqItemsetLenToCount, actualFreqItemsetLenTocount);
  }

  @Test
  public void getNumFreqItemsets() {
    final Set<Set<Integer>> freqItemsets = new HashSet<>();
    freqItemsets.add(new HashSet<>(Arrays.asList(1, 2, 3)));
    freqItemsets.add(new HashSet<>(Arrays.asList(2, 3, 4)));
    freqItemsets.add(new HashSet<>(Arrays.asList(1)));

    final Map<Integer, Integer> freqItemsetLenToCount =
        NumFreqItemsetsExperiment.getFreqItemsetLenToCountMap(freqItemsets);

    final int expectedNumFreqItemsets = 3;

    final int actualNumFreqItemsets =
        NumFreqItemsetsExperiment.getNumFreqItemsets(freqItemsetLenToCount);

    Assert.assertEquals(expectedNumFreqItemsets, actualNumFreqItemsets);
  }

  @Test
  public void updateFreqItemsetLenToCountDist() {
    final Map<Integer, List<Integer>> expectedFreqItemsetLenCountDist = new HashMap<>();
    expectedFreqItemsetLenCountDist.put(1, Arrays.asList(2, 3, 3, 2));
    expectedFreqItemsetLenCountDist.put(2, Arrays.asList(2, 3, 3));
    expectedFreqItemsetLenCountDist.put(3, Arrays.asList(1, 2, 1));
    expectedFreqItemsetLenCountDist.put(4, Arrays.asList(1));

    final Map<Integer, List<Integer>> actualFreqItemsetLenToCountDist = new HashMap<>();
    actualFreqItemsetLenToCountDist.put(1, new ArrayList<>(Arrays.asList(2, 3, 3)));
    actualFreqItemsetLenToCountDist.put(2, new ArrayList<>(Arrays.asList(2, 3)));
    actualFreqItemsetLenToCountDist.put(3, new ArrayList<>(Arrays.asList(1, 2)));

    final Map<Integer, Integer> freqItemsetLenToCount = new HashMap<>();
    freqItemsetLenToCount.put(1, 2);
    freqItemsetLenToCount.put(2, 3);
    freqItemsetLenToCount.put(3, 1);
    freqItemsetLenToCount.put(4, 1);

    NumFreqItemsetsExperiment.updateFreqItemsetLenToCountDist(
        actualFreqItemsetLenToCountDist, freqItemsetLenToCount);

    Assert.assertEquals(expectedFreqItemsetLenCountDist, actualFreqItemsetLenToCountDist);
  }

  @Test
  public void getFreqItemsetLenToCountQuartiles() {
    List<Integer> countDist = new ArrayList<>();
    for (int i = 1; i <= 100; i++) {
      countDist.add(i);
    }
    final Map<Integer, List<Integer>> freqItemsetLenCountDist = new HashMap<>();
    freqItemsetLenCountDist.put(1, countDist);

    Map<Integer, List<Integer>> expectedFreqItemsetLenToCountQuartiles = new HashMap<>();
    expectedFreqItemsetLenToCountQuartiles.put(1, Arrays.asList(1, 25, 50, 75, 100));

    Map<Integer, List<Integer>> actualFreqItemsetLenToCountQuartiles =
        NumFreqItemsetsExperiment.getFreqItemsetLenToCountQuartiles(freqItemsetLenCountDist);

    Assert.assertEquals(
        expectedFreqItemsetLenToCountQuartiles, actualFreqItemsetLenToCountQuartiles);
  }

  @Test
  public void getPvalue() {
    double expectedPvalue = (double) (6 + 1) / (100 + 1);

    List<Integer> numFreqItemsetsDist = new ArrayList<>();
    for (int i = 1; i <= 100; i++) {
      numFreqItemsetsDist.add(i);
    }
    double actualPvalue = NumFreqItemsetsExperiment.getPvalue(95, numFreqItemsetsDist);

    Assert.assertEquals(expectedPvalue, actualPvalue, 0);
  }
}
