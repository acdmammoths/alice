import alice.structures.SparseMatrix;
import alice.config.Paths;
import alice.config.DatasetNames;
import alice.samplers.BJDMSampler;
import alice.utils.Config;
import alice.samplers.Sampler;
import alice.utils.Transformer;
import alice.utils.Timer;
import java.util.Random;
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

/** A class to test {@link Transformer}. */
public class TransformerTest {
  private final Transformer transformer = new Transformer();
  private final Sampler sampler = new BJDMSampler();
  private final Random rnd = new Random();
  private static final String datasetPath =
      Paths.concat(Config.datasetsDir, DatasetNames.test);
  private static final Paths paths = new Paths(datasetPath, "test");

  @BeforeClass
  public static void before() {
    Paths.makeDir(new Paths("", "test").samplesPath);
    Config.datasetPath = datasetPath;
  }

  @AfterClass
  public static void after() {
    Paths.deleteDir("test");
  }

  @Test
  public void createMatrix() {
    final SparseMatrix expectedMatrix =
        new SparseMatrix(
            new int[][] {
              {1, 1, 0, 1, 1},
              {0, 0, 1, 1, 0},
              {0, 1, 0, 1, 1},
              {1, 0, 0, 0, 0},
            });

    final SparseMatrix actualMatrix = this.transformer.createMatrix(Config.datasetPath);
    Assert.assertEquals(expectedMatrix, actualMatrix);
  }

  @Test
  public void createDataset() {
    final String inDatasetPath = Config.datasetPath;
    final String outDatasetPath = paths.getSamplePath("", 0);
    final SparseMatrix matrix = this.transformer.createMatrix(inDatasetPath);
    final SparseMatrix expectedMatrix =
        this.sampler.sample(matrix, 100, this.rnd.nextLong(), new Timer(false));

    this.transformer.createDataset(outDatasetPath, expectedMatrix);
    final SparseMatrix actualMatrix = this.transformer.createMatrix(outDatasetPath);
    Assert.assertEquals(expectedMatrix, actualMatrix);
  }
}
