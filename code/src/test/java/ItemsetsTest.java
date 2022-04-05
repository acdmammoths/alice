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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ItemsetsTest {
  private static final String datasetPath =
      Paths.concat(Paths.datasetsDir, DatasetNames.test);
  private static final Paths paths = new Paths(datasetPath, Paths.testDir);
  private static final double minFreq = 0.5;
  private static final String freqItemsetsPath = paths.getFreqItemsetsPath("", 0);
  private static final String estFreqItemsetsPath0 =
      paths.getFreqItemsetsPath(Paths.estTag, 0);
  private static final String estFreqItemsetsPath1 =
      paths.getFreqItemsetsPath(Paths.estTag, 1);

  @BeforeClass
  public static void before() throws IOException {
    Paths.makeDir(new Paths("", Paths.testDir).freqItemsetsDirPath);

    FreqItemsetMiner.mine(datasetPath, minFreq, freqItemsetsPath);

    BufferedWriter bw = new BufferedWriter(new FileWriter(estFreqItemsetsPath0));
    bw.write("5" + Delimiters.sup + "2");
    bw.newLine();
    bw.write("5 1 4" + Delimiters.sup + "2");
    bw.newLine();
    bw.write("2" + Delimiters.sup + "2");
    bw.newLine();
    bw.close();

    bw = new BufferedWriter(new FileWriter(estFreqItemsetsPath1));
    bw.write("5" + Delimiters.sup + "3");
    bw.newLine();
    bw.write("4" + Delimiters.sup + "2");
    bw.newLine();
    bw.write("5 2 4" + Delimiters.sup + "2");
    bw.newLine();
    bw.write("2 1 4" + Delimiters.sup + "2");
    bw.newLine();
    bw.close();
  }

  @AfterClass
  public static void after() {
    Paths.deleteDir(Paths.testDir);
  }

  @Test
  public void getFreqItemsetToSupMap() {
    // minimum itemset size of 1
    final Map<Set<Integer>, Integer> expectedFreqItemsetToSup = new HashMap<>();
    expectedFreqItemsetToSup.put(new HashSet<Integer>(Arrays.asList(5)), 2);
    expectedFreqItemsetToSup.put(new HashSet<Integer>(Arrays.asList(5, 2)), 2);
    expectedFreqItemsetToSup.put(new HashSet<Integer>(Arrays.asList(5, 4)), 2);
    expectedFreqItemsetToSup.put(new HashSet<Integer>(Arrays.asList(5, 2, 4)), 2);
    expectedFreqItemsetToSup.put(new HashSet<Integer>(Arrays.asList(2)), 2);
    expectedFreqItemsetToSup.put(new HashSet<Integer>(Arrays.asList(2, 4)), 2);
    expectedFreqItemsetToSup.put(new HashSet<Integer>(Arrays.asList(1)), 2);
    expectedFreqItemsetToSup.put(new HashSet<Integer>(Arrays.asList(4)), 3);

    final Map<Set<Integer>, Integer> actualFreqItemsetToSup =
        Itemsets.getFreqItemsetToSupMap(freqItemsetsPath);

    Assert.assertEquals(expectedFreqItemsetToSup, actualFreqItemsetToSup);
  }

  @Test
  public void getFreqItemsetToSumMap() {
    final Map<Set<Integer>, Integer> expectedFreqItemsetToSum = new HashMap<>();
    expectedFreqItemsetToSum.put(new HashSet<Integer>(Arrays.asList(5)), 2);
    expectedFreqItemsetToSum.put(new HashSet<Integer>(Arrays.asList(2)), 1);
    expectedFreqItemsetToSum.put(new HashSet<Integer>(Arrays.asList(5, 2, 4)), 1);

    final Map<Set<Integer>, Integer> actualFreqItemsetToSum =
        Itemsets.getFreqItemsetToSumMap(paths, freqItemsetsPath, 2);

    Assert.assertEquals(expectedFreqItemsetToSum, actualFreqItemsetToSum);
  }

  @Test
  public void getFreqItemsetToPvalueMap() {
    final Map<Set<Integer>, Double> expectedFreqItemsetToPvalue = new HashMap<>();
    expectedFreqItemsetToPvalue.put(
        new HashSet<Integer>(Arrays.asList(5)), (double) (1 + 2) / (2 + 1));
    expectedFreqItemsetToPvalue.put(
        new HashSet<Integer>(Arrays.asList(2)), (double) (1 + 1) / (2 + 1));
    expectedFreqItemsetToPvalue.put(
        new HashSet<Integer>(Arrays.asList(5, 2, 4)), (double) (1 + 1) / (2 + 1));

    final Map<Set<Integer>, Integer> freqItemsetToSup =
        Itemsets.getFreqItemsetToSupMap(freqItemsetsPath);
    Map<Set<Integer>, Double> actualFreqItemsetToPvalue =
        Itemsets.getFreqItemsetToPvalueMap(paths, freqItemsetToSup, 2);

    Assert.assertEquals(expectedFreqItemsetToPvalue, actualFreqItemsetToPvalue);
  }

  @Test
  public void getMinPvalue() {
    final double expectedMinPvalue = (double) (1 + 1) / (2 + 1);

    final double actualMinPvalue = Itemsets.getMinPvalue(paths, freqItemsetsPath, 2);

    Assert.assertEquals(expectedMinPvalue, actualMinPvalue, 0);
  }
}
