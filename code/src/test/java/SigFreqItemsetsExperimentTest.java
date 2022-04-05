import alice.config.Paths;
import alice.config.DatasetNames;
import alice.test.SigFreqItemsets;
import alice.utils.Config;
import org.junit.AfterClass;
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
public class SigFreqItemsetsExperimentTest {
    
  @BeforeClass
  public static void before() {
      
    Paths.makeDir("test");

    Config.datasetPath = Paths.concat(Config.datasetsDir, DatasetNames.test);
    Config.numSwaps = 100;
    Config.numEstSamples = 16;
    Config.numWySamples = 8;
    Config.minFreq = 0.5;
    Config.fwer = 0.05;
    Config.numThreads = 8;
    Config.seed = 0;
    Config.resultsDir = "test";
    Config.cleanup = false;

  }

  @AfterClass
  public static void after() {
    Paths.deleteDir("test");
  }

  @Test
  public void sigFreqItemsetExperiment() {
    final String[] args = {};
    SigFreqItemsets.main(args);
  }
}
