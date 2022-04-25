import caterpillars.config.Paths;
import caterpillars.config.DatasetNames;
import caterpillars.test.Scalability;
import caterpillars.utils.Config;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ScalabilityTest {

    @BeforeClass
    public static void before() {
        Paths.makeDir("test");

        Config.datasetPath = Paths.concat(Config.datasetsDir, DatasetNames.test);
        Config.numSwaps = 100;
        Config.seed = 0;
        Config.resultsDir = "test";
    }

    @AfterClass
    public static void after() {
        Paths.deleteDir("test");
    }

    @Test
    public void runtimeExperiment() {
        final String[] args = {};
        Scalability.main(args);
    }
}
