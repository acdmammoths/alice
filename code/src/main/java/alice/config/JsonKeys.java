package alice.config;

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

/** A class to store all the JSON keys we use to avoid typos. */
public class JsonKeys {

    public static final String args = "args";
    public static final String datasetPath = "datasetPath";
    public static final String sampler = "sampler";
    public static final String numSwaps = "numSwaps";
    public static final String numEstSamples = "numEstSamples";
    public static final String numWySamples = "numWySamples";
    public static final String minFreq = "minFreq";
    public static final String fwer = "fwer";
    public static final String numThreads = "numThreads";
    public static final String seed = "seed";
    public static final String resultsDir = "resultsDir";
    public static final String cleanup = "cleanup";
    public static final String runtimes = "runtimes";
    public static final String runtimeStats = "runtimeStats";
    public static final String setupTime = "setupTime";
    public static final String minStepTime = "minStepTime";
    public static final String c10StepTime = "c10StepTime";
    public static final String q1StepTime = "q1StepTime";
    public static final String medianStepTime = "medianStepTime";
    public static final String q3StepTime = "q3StepTime";
    public static final String c90StepTime = "c90StepTime";
    public static final String maxStepTime = "maxStepTime";
    public static final String totalTime = "totalTime";
    public static final String logNumEquivMatricesStats = "logNumEquivMatricesStats";
    public static final String saveCount = "saveCount";
    public static final String numSamples = "numSamples";
    public static final String minLogNumEquivMatrices = "minLogNumEquivMatrices";
    public static final String q1LogNumEquivMatrices = "q1LogNumEquivMatrices";
    public static final String medianLogNumEquivMatrices = "medianLogNumEquivMatrices";
    public static final String q3LogNumEquivMatrices = "q3LogNumEquivMatrices";
    public static final String maxLogNumEquivMatrices = "maxLogNumEquivMatrices";
    public static final String maxNumSwapsFactor = "maxNumSwapsFactor";
    public static final String convergenceStats = "convergenceStats";
    public static final String numSwapsFactor = "numSwapsFactor";
    public static final String numOnes = "numOnes";
    public static final String avgRelFreqDiff = "avgRelFreqDiff";
    public static final String runInfo = "runInfo";
    public static final String itemsets = "itemsets";
    public static final String timestamp = "timestamp";
    public static final String freqItemsets = "freqItemsets";
    public static final String sigFreqItemsets = "sigFreqItemsets";
    public static final String sup = "sup";
    public static final String pvalue = "pvalue";
    public static final String numFreqItemsets = "numFreqItemsets";
    public static final String numSigFreqItemsets = "numSigFreqItemsets";
    public static final String adjustedCriticalValue = "adjustedCriticalValue";
    public static final String minPvalues = "minPvalues";
    public static final String totalRuntime = "totalRuntime";
    public static final String createMatrixTime = "createMatrixTime";
    public static final String estSampleAndMinetime = "estSampleAndMineTime";
    public static final String wySampleAndMineTime = "wySampleAndMineTime";
    public static final String getMinPvaluesTime = "getMinPvaluesTime";
    public static final String setAdjustedCriticalValueTime = "setAdjustedCriticalValueTime";
    public static final String mineSigFreqItemsetsTime = "mineSigFreqItemsetsTime";
    public static final String numFreqItemsetsStats = "numFreqItemsetsStats";
    public static final String numFreqItemsetsQuartiles = "numFreqItemsetsQuartiles";
    public static final String freqItemsetLenToCount = "freqItemsetLenToCount";
    public static final String freqItemsetLenToCountQuartiles = "freqItemsetLenToCountQuartiles";
    public static final String sampleAndMine = "sampleAndMine";
}
