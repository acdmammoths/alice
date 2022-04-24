package caterpillars.test;

import diffusr.samplers.GmmtSampler;
import caterpillars.config.Paths;
import caterpillars.samplers.CurveballBJDMSampler;
import caterpillars.samplers.NaiveBJDMSampler;
import caterpillars.utils.CMDLineParser;
import caterpillars.utils.Config;
import diffusr.fpm.SigFreqItemsetMiner;
import diffusr.samplers.Sampler;

/**
 * This class runs the significant frequent itemsets experiment.
 */
public class SigFreqItemsets {

    public static void main(String[] args) {

        CMDLineParser.parse(args);
        
        final Sampler[] samplers = {new NaiveBJDMSampler(), /*new CurveballBJDMSampler(),*/ new GmmtSampler()};

        for (Sampler sampler : samplers) {
            SigFreqItemsetMiner sigFreqItemsetMiner
                    = new SigFreqItemsetMiner(
                            Config.datasetPath,
                            sampler,
                            Config.numSwaps,
                            Config.numEstSamples,
                            Config.numWySamples,
                            Config.minFreq,
                            Config.fwer,
                            Config.numThreads,
                            Config.seed,
                            Paths.concat(Config.resultsDir, sampler.getClass().getName()),
                            Config.cleanup);
            sigFreqItemsetMiner.mine();
        }
    }
}
