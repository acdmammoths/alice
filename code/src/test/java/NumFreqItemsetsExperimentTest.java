import alice.config.Paths;
import alice.config.DatasetNames;
import alice.config.Delimiters;
import alice.test.NumFreqItemsets;
import alice.utils.Config;
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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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
public class NumFreqItemsetsExperimentTest {

    private static final String datasetPath =
      Paths.concat(Config.datasetsDir, DatasetNames.test);
  private static final Paths paths = new Paths(datasetPath, "test");
  private static final String freqItemsetsFilePath = paths.getFreqItemsetsPath("", 0);

  @BeforeClass
  public static void before() throws IOException {
    Paths.makeDir("test");
    Paths.makeDir(paths.freqItemsetsDirPath);

    Config.datasetPath = datasetPath;
    Config.numSwaps = 100;
    Config.numSamples = 20;
    Config.minFreq = 0.5;
    Config.numThreads = 8;
    Config.seed = 0;
    Config.resultsDir = "test";
    Config.sampleAndMine = true;

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
    Paths.deleteDir("test");
  }

  @Test
  public void numFreqItemsetsExperiment() {
    final String[] args = {};
    NumFreqItemsets.main(args);
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
        NumFreqItemsets.getFreqItemsetLenToCountMap(freqItemsets);
    Assert.assertEquals(expectedFreqItemsetLenToCount, actualFreqItemsetLenTocount);

    actualFreqItemsetLenTocount =
        NumFreqItemsets.getFreqItemsetLenToCountMap(new File(freqItemsetsFilePath));
    Assert.assertEquals(expectedFreqItemsetLenToCount, actualFreqItemsetLenTocount);
  }

  @Test
  public void getNumFreqItemsets() {
    final Set<Set<Integer>> freqItemsets = new HashSet<>();
    freqItemsets.add(new HashSet<>(Arrays.asList(1, 2, 3)));
    freqItemsets.add(new HashSet<>(Arrays.asList(2, 3, 4)));
    freqItemsets.add(new HashSet<>(Arrays.asList(1)));

    final Map<Integer, Integer> freqItemsetLenToCount =
        NumFreqItemsets.getFreqItemsetLenToCountMap(freqItemsets);

    final int expectedNumFreqItemsets = 3;

    final int actualNumFreqItemsets =
        NumFreqItemsets.getNumFreqItemsets(freqItemsetLenToCount);

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

    NumFreqItemsets.updateFreqItemsetLenToCountDist(
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
        NumFreqItemsets.getFreqItemsetLenToCountQuartiles(freqItemsetLenCountDist);

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
    double actualPvalue = NumFreqItemsets.getPvalue(95, numFreqItemsetsDist);

    Assert.assertEquals(expectedPvalue, actualPvalue, 0);
  }
}
