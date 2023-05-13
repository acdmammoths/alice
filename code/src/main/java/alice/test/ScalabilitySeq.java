package alice.test;

/*
 * Copyright (C) 2023 Giulia Preti
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
import alice.utils.JsonFile;
import alice.config.Delimiters;
import alice.config.JsonKeys;
import alice.samplers.AliceSSampler;
import alice.utils.CMDLineParser;
import alice.utils.Config;
import alice.utils.Timer;
import alice.samplers.GmmtSeqSampler;
import alice.samplers.SeqSampler;
import alice.structures.MultiGraph;
import alice.utils.Transformer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;
import org.json.JSONObject;

/**
 * This class runs the scalability experiment, which measures the step times of the
 * samplers for sequence datasets.
 */
public class ScalabilitySeq {

    public static void main(String[] args) throws IOException {
        
        CMDLineParser.parse(args);
        
        final SeqSampler[] samplers = new SeqSampler[]{new AliceSSampler(), new GmmtSeqSampler()};

        System.out.println("Executing runtime experiment for dataset at " + Config.datasetPath);

        final Transformer transformer = new Transformer();
        final MultiGraph matrix = transformer.createMultiGraph(Config.datasetPath);
        final Random rnd = new Random(Config.seed);

        // create object for runtimeStats
        final JSONObject runtimeStats = new JSONObject();
        for (SeqSampler sampler : samplers) {
            
            final String samplerName = sampler.getClass().getName();
            final Timer timer = new Timer(true);
            sampler.sample(matrix, Config.numSwaps, rnd.nextLong(), timer);

            final long setupTime = timer.getSavedTime();
            final double minStepTime = timer.getMin();
            final double c10StepTime = timer.getPercentile(10);
            final double q1StepTime = timer.getPercentile(25);
            final double medianStepTime = timer.getPercentile(50);
            final double q3StepTime = timer.getPercentile(75);
            final double c90StepTime = timer.getPercentile(90);
            final double maxStepTime = timer.getMax();

            System.out.println("\t" + JsonKeys.runtimeStats + ":");
            System.out.println("\t\t" + JsonKeys.setupTime + ": " + setupTime);
            System.out.println("\t\t" + JsonKeys.minStepTime + ": " + minStepTime);
            System.out.println("\t\t" + JsonKeys.c10StepTime + ": " + c10StepTime);
            System.out.println("\t\t" + JsonKeys.q1StepTime + ": " + q1StepTime);
            System.out.println("\t\t" + JsonKeys.medianStepTime + ": " + medianStepTime);
            System.out.println("\t\t" + JsonKeys.q3StepTime + ": " + q3StepTime);
            System.out.println("\t\t" + JsonKeys.c90StepTime + ": " + c90StepTime);
            System.out.println("\t\t" + JsonKeys.maxStepTime + ": " + maxStepTime);

            // create object for samplerRuntimeStats
            final JSONObject samplerRuntimeStats = new JSONObject();
            samplerRuntimeStats.put(JsonKeys.setupTime, setupTime);
            samplerRuntimeStats.put(JsonKeys.minStepTime, minStepTime);
            samplerRuntimeStats.put(JsonKeys.c10StepTime, c10StepTime);
            samplerRuntimeStats.put(JsonKeys.q1StepTime, q1StepTime);
            samplerRuntimeStats.put(JsonKeys.medianStepTime, medianStepTime);
            samplerRuntimeStats.put(JsonKeys.q3StepTime, q3StepTime);
            samplerRuntimeStats.put(JsonKeys.c90StepTime, c90StepTime);
            samplerRuntimeStats.put(JsonKeys.maxStepTime, maxStepTime);

            runtimeStats.put(samplerName, samplerRuntimeStats);
        }
        // create object for runInfo
        final JSONObject runInfo = new JSONObject();
        runInfo.put(JsonKeys.timestamp, LocalDateTime.now());
        // create base object
        final JSONObject results = new JSONObject();
        results.put(JsonKeys.runInfo, runInfo);
        results.put(JsonKeys.runtimeStats, runtimeStats);
        // save JSON
        final String datasetBaseName = new Paths(Config.datasetPath, "").datasetBaseName;
        final String resultsBaseName  = String.join(Delimiters.dash, 
                datasetBaseName, 
                String.valueOf(Config.numSwaps), 
                String.valueOf(Config.seed));
        final String resultsPath = Paths.getJsonFilePath(Config.resultsDir, resultsBaseName);
        JsonFile.write(results, resultsPath);
        System.out.println("Result written to " + resultsPath);
    }
}
