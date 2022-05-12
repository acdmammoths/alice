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
import caterpillars.structures.SparseMatrix;
import caterpillars.config.Paths;
import caterpillars.utils.JsonFile;
import caterpillars.config.Delimiters;
import caterpillars.config.JsonKeys;
import caterpillars.samplers.CurveballBJDMSampler;
import caterpillars.samplers.NaiveBJDMSampler;
import caterpillars.utils.CMDLineParser;
import caterpillars.utils.Config;
import diffusr.samplers.Sampler;
import caterpillars.utils.Transformer;
import caterpillars.utils.Timer;
import diffusr.samplers.GmmtSampler;
import java.time.LocalDateTime;
import java.util.Random;
import org.json.JSONObject;

/**
 * This class runs the runtime experiment, which measures the step times of the
 * samplers. This class is also used for the scalability experiment.
 */
public class Scalability {

    public static void main(String[] args) {
        
        CMDLineParser.parse(args);

        final Sampler[] samplers = {new NaiveBJDMSampler(), new CurveballBJDMSampler(), new GmmtSampler()};

        System.out.println("Executing runtime experiment for dataset at " + Config.datasetPath);

        final SparseMatrix matrix = new Transformer().createMatrix(Config.datasetPath);

        final Random rnd = new Random(Config.seed);

        // create object for runtimeStats
        final JSONObject runtimeStats = new JSONObject();
        for (Sampler sampler : samplers) {
            
            final String samplerName = sampler.getClass().getName();
            final Timer timer = new Timer(true);
            sampler.sample(matrix, Config.numSwaps, rnd.nextLong(), timer);

            final long setupTime = timer.getSavedTime();
            final double minStepTime = timer.getMin();
            final double q1StepTime = timer.getPercentile(25);
            final double medianStepTime = timer.getPercentile(50);
            final double q3StepTime = timer.getPercentile(75);
            final double maxStepTime = timer.getMax();

            System.out.println("\t" + JsonKeys.runtimeStats + ":");
            System.out.println("\t\t" + JsonKeys.setupTime + ": " + setupTime);
            System.out.println("\t\t" + JsonKeys.minStepTime + ": " + minStepTime);
            System.out.println("\t\t" + JsonKeys.q1StepTime + ": " + q1StepTime);
            System.out.println("\t\t" + JsonKeys.medianStepTime + ": " + medianStepTime);
            System.out.println("\t\t" + JsonKeys.q3StepTime + ": " + q3StepTime);
            System.out.println("\t\t" + JsonKeys.maxStepTime + ": " + maxStepTime);

            // create object for samplerRuntimeStats
            final JSONObject samplerRuntimeStats = new JSONObject();
            samplerRuntimeStats.put(JsonKeys.setupTime, setupTime);
            samplerRuntimeStats.put(JsonKeys.minStepTime, minStepTime);
            samplerRuntimeStats.put(JsonKeys.q1StepTime, q1StepTime);
            samplerRuntimeStats.put(JsonKeys.medianStepTime, medianStepTime);
            samplerRuntimeStats.put(JsonKeys.q3StepTime, q3StepTime);
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
