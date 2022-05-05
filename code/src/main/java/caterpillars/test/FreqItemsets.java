package caterpillars.test;

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

import caterpillars.config.SamplerNames;
import diffusr.fpm.SampleAndMiner;
import caterpillars.config.JsonKeys;
import caterpillars.samplers.CurveballBJDMSampler;
import caterpillars.samplers.NaiveBJDMSampler;
import caterpillars.utils.CMDLineParser;
import caterpillars.utils.Config;
import diffusr.samplers.GmmtSampler;
import diffusr.samplers.Sampler;

/**
 * Driver for sampling datasets and mining patterns from them.
 */
public class FreqItemsets {

    /**
     * Runs the driver.
     * @param args
     */
    public static void main(String[] args) {
        // parse args
        CMDLineParser.parse(args);

        Sampler sampler = null;
        if (Config.samplerType.equals(SamplerNames.caterNaiveSampler)) {
            sampler = new NaiveBJDMSampler();
        } else if (Config.samplerType.equals(SamplerNames.caterCurveballSampler)) {
            sampler = new CurveballBJDMSampler();
        } else if (Config.samplerType.equals(SamplerNames.gmmtSampler)) {
            sampler = new GmmtSampler();
        } else {
            System.err.println("Unknown sample type " + Config.samplerType);
            System.exit(1);
        }

        System.out.println("Running driver with arguments:");
        System.out.println("\t" + JsonKeys.datasetPath + ": " + Config.datasetPath);
        System.out.println("\t" + JsonKeys.sampler + ": " + Config.samplerType);
        System.out.println("\t" + JsonKeys.numSwaps + ": " + Config.numSwaps);
        System.out.println("\t" + JsonKeys.numSamples + ": " + Config.numSamples);
        System.out.println("\t" + JsonKeys.minFreq + ": " + Config.minFreq);
        System.out.println("\t" + JsonKeys.numThreads + ": " + Config.numThreads);
        System.out.println("\t" + JsonKeys.seed + ": " + Config.seed);
        System.out.println("\t" + JsonKeys.resultsDir + ": " + Config.resultsDir);

        SampleAndMiner.sampleAndMine(
                Config.datasetPath, 
                sampler, 
                Config.numSwaps, 
                Config.numSamples, 
                Config.minFreq, 
                Config.numThreads, 
                Config.seed, 
                Config.resultsDir);
    }
}
