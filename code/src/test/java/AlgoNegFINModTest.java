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
import diffusr.fpm.AlgoNegFINMod;
import diffusr.fpm.AlgoNegFIN;
import alice.config.Paths;
import diffusr.fpm.Itemsets;
import alice.config.DatasetNames;
import alice.utils.Config;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AlgoNegFINModTest {

    private static final String datasetPath = Paths.concat(Config.datasetsDir, DatasetNames.foodmart);
    private static final Paths paths = new Paths(datasetPath, "test");

    @BeforeClass
    public static void before() {
        Paths.makeDir("test");
        Paths.makeDir(paths.freqItemsetsDirPath);
        Config.datasetPath = Paths.concat(Config.datasetsDir, DatasetNames.foodmart);
        Config.minFreq = 0.0003;
    }

    @AfterClass
    public static void after() {
        Paths.deleteDir("test");
    }

    @Test
    public void same() throws IOException {
        final String outPath = Paths.concat(paths.freqItemsetsDirPath, paths.datasetBaseName);
        final AlgoNegFIN negFin = new AlgoNegFIN();
        negFin.runAlgorithm(Config.datasetPath, Config.minFreq, outPath);
        final Map<Set<Integer>, Integer> expectedFreqItemsetToSup
                = Itemsets.getFreqItemsetToSupMap(outPath);

        final AlgoNegFINMod negFinMod = new AlgoNegFINMod();
        final Map<Set<Integer>, Integer> actualFreqItemsetToSup
                = negFinMod.runAlgorithm(Config.datasetPath, Config.minFreq);

        Assert.assertEquals(expectedFreqItemsetToSup, actualFreqItemsetToSup);
        // check to see if we are just comparing empty maps
        Assert.assertTrue(!actualFreqItemsetToSup.isEmpty());
    }
}
