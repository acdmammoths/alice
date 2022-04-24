import caterpillars.config.SamplerNames;
import caterpillars.config.Paths;
import caterpillars.config.DatasetNames;
import caterpillars.test.FreqItemsets;
import caterpillars.utils.Config;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DriverTest {

    @BeforeClass
    public static void before() {
        Paths.makeDir("test");

        Config.datasetPath = Paths.concat(Config.datasetsDir, DatasetNames.test);
        Config.numSwaps = 10;
        Config.numSamples = 20;
        Config.minFreq = 0.5;
        Config.numThreads = 8;
        Config.seed = 0;
        Config.resultsDir = "test";
    }

    @AfterClass
    public static void after() {
        Paths.deleteDir("test");
    }

    @Test
    public void driver() {
        final String[] samplerTypes = {SamplerNames.gmmtSampler, 
            SamplerNames.caterNaiveSampler /*,  SamplerNames.caterCurveballSampler */};
        for (String samplerType : samplerTypes) {
            Config.samplerType = samplerType;
            final String[] args = {};
            FreqItemsets.main(args);
        }
    }
}
