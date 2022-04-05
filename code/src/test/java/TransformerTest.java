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
import java.util.Random;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/** A class to test {@link Transformer}. */
public class TransformerTest {
  private final Transformer transformer = new Transformer();
  private final Sampler sampler = new NaiveSampler();
  private final Random rnd = new Random();
  private static final String datasetPath =
      Paths.concat(Paths.datasetsDir, DatasetNames.test);
  private static final Paths paths = new Paths(datasetPath, Paths.testDir);

  @BeforeClass
  public static void before() {
    Paths.makeDir(new Paths("", Paths.testDir).samplesPath);
  }

  @AfterClass
  public static void after() {
    Paths.deleteDir(Paths.testDir);
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

    final SparseMatrix actualMatrix = this.transformer.createMatrix(paths.datasetPath);

    Assert.assertEquals(expectedMatrix, actualMatrix);
  }

  @Test
  public void createDataset() {
    final String inDatasetPath = paths.datasetPath;
    final String outDatasetPath = paths.getSamplePath("", 0);
    final SparseMatrix matrix = this.transformer.createMatrix(inDatasetPath);
    final SparseMatrix expectedMatrix =
        this.sampler.sample(matrix, 100, this.rnd.nextLong(), new Timer(false));

    this.transformer.createDataset(outDatasetPath, expectedMatrix);
    final SparseMatrix actualMatrix = this.transformer.createMatrix(outDatasetPath);

    Assert.assertEquals(expectedMatrix, actualMatrix);
  }
}
