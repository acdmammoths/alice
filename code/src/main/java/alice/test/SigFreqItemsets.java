package alice.test;

/*
 * Copyright (C) 2022 Alexander Lee, Giulia Preti, and Matteo Riondato
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
import alice.config.Paths;
import alice.samplers.BJDMSampler;
import alice.utils.CMDLineParser;
import alice.utils.Config;
import alice.fpm.SigFreqItemsetMiner;
import alice.samplers.CurveballBJDMSampler;
import alice.samplers.GmmtSampler;
import alice.samplers.Sampler;

/**
 * This class runs the significant frequent itemsets experiment.
 */
public class SigFreqItemsets {

    public static void main(String[] args) {

        CMDLineParser.parse(args);
        
        final Sampler[] samplers = {
            new BJDMSampler(), 
            new CurveballBJDMSampler(), 
            new GmmtSampler()
        };

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
