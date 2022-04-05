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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConvergenceExperimentTest {
  private static final String confsDir =
      Paths.concat(Paths.testDir, Paths.experConfsConvergencePath);
  private static final String resultsDir =
      Paths.concat(Paths.testDir, Paths.experResultsConvergencePath);
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
    conf.put(JsonKeys.maxNumSwapsFactor, 5);
    conf.put(JsonKeys.minFreq, 0.5);
    conf.put(JsonKeys.seed, 0);
    conf.put(JsonKeys.resultsDir, resultsDir);

    JsonFile.write(conf, confPath);
  }

  @AfterClass
  public static void after() {
    Paths.deleteDir(Paths.testDir);
  }

  @Test
  public void getSampleItemsetToSupMap() {
    final Transformer transformer = new Transformer();
    final SparseMatrix sample = transformer.createMatrix(paths.datasetPath);
    final Set<Set<Integer>> freqItemsets = new HashSet<>();
    freqItemsets.add(new HashSet<Integer>(Arrays.asList(1, 4)));
    freqItemsets.add(new HashSet<Integer>(Arrays.asList(2, 4)));
    freqItemsets.add(new HashSet<Integer>(Arrays.asList(2, 4, 5)));
    freqItemsets.add(new HashSet<Integer>(Arrays.asList(3, 5)));
    freqItemsets.add(new HashSet<Integer>(Arrays.asList(1, 3, 5)));

    final Map<Set<Integer>, Integer> expectedSampleItemsetToSup = new HashMap<>();
    expectedSampleItemsetToSup.put(new HashSet<Integer>(Arrays.asList(1, 4)), 1);
    expectedSampleItemsetToSup.put(new HashSet<Integer>(Arrays.asList(2, 4)), 2);
    expectedSampleItemsetToSup.put(new HashSet<Integer>(Arrays.asList(2, 4, 5)), 2);

    final Map<Set<Integer>, Integer> actualSampleItemsetToSup =
        ConvergenceExperiment.getSampleItemsetToSupMap(sample, transformer, freqItemsets);

    Assert.assertEquals(expectedSampleItemsetToSup, actualSampleItemsetToSup);
  }

  @Test
  public void getAvgRelFreqDiff() {
    final Map<Set<Integer>, Integer> itemsetToSup = new HashMap<>();
    itemsetToSup.put(new HashSet<Integer>(Arrays.asList(1)), 5);
    itemsetToSup.put(new HashSet<Integer>(Arrays.asList(2)), 2);
    itemsetToSup.put(new HashSet<Integer>(Arrays.asList(4, 5)), 2);
    itemsetToSup.put(new HashSet<Integer>(Arrays.asList(2, 3, 4)), 1);

    final Map<Set<Integer>, Integer> sampleItemsetToSup = new HashMap<>();
    sampleItemsetToSup.put(new HashSet<Integer>(Arrays.asList(1)), 2);
    sampleItemsetToSup.put(new HashSet<Integer>(Arrays.asList(2)), 3);
    sampleItemsetToSup.put(new HashSet<Integer>(Arrays.asList(2, 3, 4)), 3);

    // 1:       |5 - 2| / 5 = 3/5
    // 2:       |2 - 3| / 2 = 1/2
    // 4, 5:    |2 - 0| / 2 = 1
    // 2, 3, 4: |1 - 3| / 1 = 2
    //
    // (3/5 + 1/2 + 1 + 2) / 4
    // = (6/10 + 5/10 + 10/10 + 20/10) / 4
    // = 41/40
    final double expectedAvgRelFreqDiff = (double) 41 / 40;

    final double actualAvgRelFreqDiff =
        ConvergenceExperiment.getAvgRelFreqDiff(itemsetToSup, sampleItemsetToSup);

    Assert.assertEquals(expectedAvgRelFreqDiff, actualAvgRelFreqDiff, 0);
  }

  @Test
  public void convergenceExperiment() {
    final String[] args = {confPath};
    ConvergenceExperiment.main(args);
  }
}
