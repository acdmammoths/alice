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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DriverTest {
  @BeforeClass
  public static void before() {
    Paths.makeDir(Paths.testDir);
  }

  @AfterClass
  public static void after() {
    Paths.deleteDir(Paths.testDir);
  }

  @Test
  public void driver() {
    final String[] samplerTypes = {
      SamplerNames.gmmtSampler, SamplerNames.naiveSampler, SamplerNames.refinedSampler
    };
    for (String samplerType : samplerTypes) {
      final String[] args = {
        Paths.concat(Paths.datasetsDir, DatasetNames.test),
        samplerType,
        "10",
        "20",
        "0.5",
        "8",
        "0",
        Paths.testDir,
      };
      Driver.main(args);
    }
  }
}
