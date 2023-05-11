import alice.structures.SparseMatrix;
import alice.config.Paths;
import alice.config.DatasetNames;
import alice.test.Convergence;
import alice.utils.Config;
import alice.utils.Transformer;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Arrays;
import java.util.Map;
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
public class ConvergenceExperimentTest {

    @BeforeClass
    public static void before() {
        Paths.makeDir("test");

        Config.datasetPath = Paths.concat(Config.datasetsDir, DatasetNames.test);
        Config.maxNumSwapsFactor = 5;
        Config.minFreq = 0.5;
        Config.seed = 0;
        Config.resultsDir = "test";
    }

    @AfterClass
    public static void after() {
        Paths.deleteDir("test");
    }

    @Test
    public void getSampleItemsetToSupMap() {
        final Transformer transformer = new Transformer();
        final SparseMatrix sample = transformer.createMatrix(Config.datasetPath);
        final ObjectSet<IntOpenHashSet> freqItemsets = new ObjectOpenHashSet();
        freqItemsets.add(new IntOpenHashSet(Arrays.asList(1, 4)));
        freqItemsets.add(new IntOpenHashSet(Arrays.asList(2, 4)));
        freqItemsets.add(new IntOpenHashSet(Arrays.asList(2, 4, 5)));
        freqItemsets.add(new IntOpenHashSet(Arrays.asList(3, 5)));
        freqItemsets.add(new IntOpenHashSet(Arrays.asList(1, 3, 5)));

        final Map<IntOpenHashSet, Integer> expectedSampleItemsetToSup = Maps.newHashMap();
        expectedSampleItemsetToSup.put(new IntOpenHashSet(Arrays.asList(1, 4)), 1);
        expectedSampleItemsetToSup.put(new IntOpenHashSet(Arrays.asList(2, 4)), 2);
        expectedSampleItemsetToSup.put(new IntOpenHashSet(Arrays.asList(2, 4, 5)), 2);

        final Map<IntOpenHashSet,Integer> actualSampleItemsetToSup
                = Convergence.getSampleItemsetToSupMap(sample, transformer, freqItemsets);
        Assert.assertEquals(expectedSampleItemsetToSup, actualSampleItemsetToSup);
    }

    @Test
    public void getAvgRelFreqDiff() {
        final Object2IntOpenHashMap<IntOpenHashSet> itemsetToSup = new Object2IntOpenHashMap();
        itemsetToSup.put(new IntOpenHashSet(Arrays.asList(1)), 5);
        itemsetToSup.put(new IntOpenHashSet(Arrays.asList(2)), 2);
        itemsetToSup.put(new IntOpenHashSet(Arrays.asList(4, 5)), 2);
        itemsetToSup.put(new IntOpenHashSet(Arrays.asList(2, 3, 4)), 1);

        final Map<IntOpenHashSet, Integer> sampleItemsetToSup = Maps.newHashMap();
        sampleItemsetToSup.put(new IntOpenHashSet(Arrays.asList(1)), 2);
        sampleItemsetToSup.put(new IntOpenHashSet(Arrays.asList(2)), 3);
        sampleItemsetToSup.put(new IntOpenHashSet(Arrays.asList(2, 3, 4)), 3);

        // 1:       |5 - 2| / 5 = 3/5
        // 2:       |2 - 3| / 2 = 1/2
        // 4, 5:    |2 - 0| / 2 = 1
        // 2, 3, 4: |1 - 3| / 1 = 2
        //
        // (3/5 + 1/2 + 1 + 2) / 4
        // = (6/10 + 5/10 + 10/10 + 20/10) / 4
        // = 41/40
        final double expectedAvgRelFreqDiff = (double) 41 / 40;
        final double actualAvgRelFreqDiff = Convergence.getAvgRelFreqDiff(itemsetToSup, sampleItemsetToSup);
        Assert.assertEquals(expectedAvgRelFreqDiff, actualAvgRelFreqDiff, 0);
    }

    @Test
    public void convergenceExperiment() {
        final String[] args = {};
        Convergence.main(args);
    }
}
