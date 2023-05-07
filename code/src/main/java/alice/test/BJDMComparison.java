package alice.test;

/*
 * Copyright (C) 2022 Giulia
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
import alice.samplers.GmmtSampler;
import alice.config.Paths;
import alice.config.JsonKeys;
import alice.utils.JsonFile;
import alice.config.Delimiters;
import alice.samplers.BJDMSampler;
import alice.samplers.SampleTask;
import alice.structures.SparseMatrix;
import alice.utils.CMDLineParser;
import alice.utils.Config;
import alice.utils.Timer;
import alice.utils.Transformer;
import com.google.common.collect.Maps;
import alice.samplers.Sampler;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONObject;

/**
 * Computes the distances between consecutive BJDM matrices in the Markov chain
 */
public class BJDMComparison {

    public static void main(String[] args) {
        
        CMDLineParser.parse(args);

        System.out.println("Executing BJDM distance experiment");
        // samplers
        final Sampler[] samplers = {
            new BJDMSampler(), 
            new GmmtSampler()
        };
         // create object for runtime stats
        final JSONObject runtimeStats = new JSONObject();
        // file to store the JSON
        final String datasetBaseName = new Paths(Config.datasetPath, "").datasetBaseName;
        final String resultsBaseName
                = String.join(
                        Delimiters.dash,
                        datasetBaseName,
                        String.valueOf(Config.numSwaps),
                        String.valueOf(Config.numSamples),
                        String.valueOf(Config.seed));
        final String resultPath = Paths.getJsonFilePath(Config.resultsDir, resultsBaseName);
        
        final SparseMatrix matrix = new Transformer().createMatrix(Config.datasetPath);
        System.out.println("Matrix created from dataset");
        
        final Random rnd = new Random(Config.seed);
        
        for (Sampler sampler : samplers) {
            
            final String samplerName = sampler.getClass().getName();
            final Map<Integer, DescriptiveStatistics> allStats = Maps.newConcurrentMap();
            final Map<Integer, DescriptiveStatistics> allCatNum = Maps.newConcurrentMap();
            final Map<Integer, Timer> allTimers = Maps.newConcurrentMap();
            
            System.out.println(samplerName);
            
            // sample datasets in parallel
            final ExecutorService pool = Executors.newFixedThreadPool(Config.numThreads);
            for (int i = 0; i < Config.numSamples; i++) {
                final long seed = rnd.nextLong();
                final SampleTask sampleTask = new SampleTask(sampler,
                        matrix,
                        Config.numSwaps,
                        seed,
                        i,
                        new Timer(true),
                        new DescriptiveStatistics(),
                        new DescriptiveStatistics());
                pool.execute(sampleTask);
                allStats.put(i, sampleTask.getStats());
                allCatNum.put(i, sampleTask.getNumCat());
                allTimers.put(i, sampleTask.getTimes());
            }
            pool.shutdown();
            try {
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Error executing sample and mine tasks");
                e.printStackTrace();
                System.exit(1);
            }
            // get time statistics
            final long[] setupTime = new long[Config.numSamples];
            final double[] minStepTime = new double[Config.numSamples];
            final double[] c10StepTime = new double[Config.numSamples];
            final double[] q1StepTime = new double[Config.numSamples];
            final double[] medianStepTime = new double[Config.numSamples];
            final double[] q3StepTime = new double[Config.numSamples];
            final double[] c90StepTime = new double[Config.numSamples];
            final double[] maxStepTime = new double[Config.numSamples];

            for (Entry<Integer, Timer> e : allTimers.entrySet()) {
                Timer timer = e.getValue();
                setupTime[e.getKey()] = timer.getSavedTime();
                minStepTime[e.getKey()] = timer.getMin();
                c10StepTime[e.getKey()] = timer.getPercentile(10);
                q1StepTime[e.getKey()] = timer.getPercentile(25);
                medianStepTime[e.getKey()] = timer.getPercentile(50);
                q3StepTime[e.getKey()] = timer.getPercentile(75);
                c90StepTime[e.getKey()] = timer.getPercentile(90);
                maxStepTime[e.getKey()] = timer.getMax();
            }
            
            // get BJDM stats
            final double[] min = new double[Config.numSamples];
            final double[] c10 = new double[Config.numSamples];
            final double[] q1 = new double[Config.numSamples];
            final double[] median = new double[Config.numSamples];
            final double[] q3 = new double[Config.numSamples];
            final double[] c90 = new double[Config.numSamples];
            final double[] max = new double[Config.numSamples];
            
            for (Entry<Integer, DescriptiveStatistics> e : allStats.entrySet()) {
                DescriptiveStatistics st = e.getValue();
                min[e.getKey()] = st.getMin();
                c10[e.getKey()] = st.getPercentile(10);
                q1[e.getKey()] = st.getPercentile(25);
                median[e.getKey()] = st.getPercentile(50);
                q3[e.getKey()] = st.getPercentile(75);
                c90[e.getKey()] = st.getPercentile(90);
                max[e.getKey()] = st.getMax();
            }
            
            // get Num Caterpillars
            final double[] numC = new double[Config.numSamples];
            
            for (Entry<Integer, DescriptiveStatistics> e : allCatNum.entrySet()) {
                DescriptiveStatistics st = e.getValue();
                numC[e.getKey()] = st.getMax();
            }
            
            // create object for stats
            final JSONObject samplerRuntimeStats = new JSONObject();
            samplerRuntimeStats.put(JsonKeys.setupTime, setupTime);
            samplerRuntimeStats.put(JsonKeys.minStepTime, minStepTime);
            samplerRuntimeStats.put(JsonKeys.c10StepTime, c10StepTime);
            samplerRuntimeStats.put(JsonKeys.q1StepTime, q1StepTime);
            samplerRuntimeStats.put(JsonKeys.medianStepTime, medianStepTime);
            samplerRuntimeStats.put(JsonKeys.q3StepTime, q3StepTime);
            samplerRuntimeStats.put(JsonKeys.c90StepTime, c90StepTime);
            samplerRuntimeStats.put(JsonKeys.maxStepTime, maxStepTime);
            
            samplerRuntimeStats.put("minDist", min);
            samplerRuntimeStats.put("c10Dist", c10);
            samplerRuntimeStats.put("q1Dist", q1);
            samplerRuntimeStats.put("meanDist", median);
            samplerRuntimeStats.put("q3Dist", q3);
            samplerRuntimeStats.put("c90Dist", c90);
            samplerRuntimeStats.put("maxDist", max);
            samplerRuntimeStats.put("numCater", numC);
            
            runtimeStats.put(samplerName, samplerRuntimeStats);
        }
        System.out.println("Writing results...");
        // create base object
        final JSONObject results = new JSONObject();
        results.put(JsonKeys.runtimeStats, runtimeStats);
        // save JSON
        JsonFile.write(results, resultPath);
        System.out.println("Result written to " + resultPath);
    }
    
}
