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
class JsonKeys {
  static final String args = "args";
  static final String datasetPath = "datasetPath";
  static final String sampler = "sampler";
  static final String numSwaps = "numSwaps";
  static final String numEstSamples = "numEstSamples";
  static final String numWySamples = "numWySamples";
  static final String minFreq = "minFreq";
  static final String fwer = "fwer";
  static final String numThreads = "numThreads";
  static final String seed = "seed";
  static final String resultsDir = "resultsDir";
  static final String cleanup = "cleanup";
  static final String runtimes = "runtimes";
  static final String runtimeStats = "runtimeStats";
  static final String setupTime = "setupTime";
  static final String minStepTime = "minStepTime";
  static final String q1StepTime = "q1StepTime";
  static final String medianStepTime = "medianStepTime";
  static final String q3StepTime = "q3StepTime";
  static final String maxStepTime = "maxStepTime";
  static final String logNumEquivMatricesStats = "logNumEquivMatricesStats";
  static final String saveCount = "saveCount";
  static final String numSamples = "numSamples";
  static final String minLogNumEquivMatrices = "minLogNumEquivMatrices";
  static final String q1LogNumEquivMatrices = "q1LogNumEquivMatrices";
  static final String medianLogNumEquivMatrices = "medianLogNumEquivMatrices";
  static final String q3LogNumEquivMatrices = "q3LogNumEquivMatrices";
  static final String maxLogNumEquivMatrices = "maxLogNumEquivMatrices";
  static final String maxNumSwapsFactor = "maxNumSwapsFactor";
  static final String convergenceStats = "convergenceStats";
  static final String numSwapsFactor = "numSwapsFactor";
  static final String numOnes = "numOnes";
  static final String avgRelFreqDiff = "avgRelFreqDiff";
  static final String runInfo = "runInfo";
  static final String itemsets = "itemsets";
  static final String timestamp = "timestamp";
  static final String freqItemsets = "freqItemsets";
  static final String sigFreqItemsets = "sigFreqItemsets";
  static final String sup = "sup";
  static final String pvalue = "pvalue";
  static final String numFreqItemsets = "numFreqItemsets";
  static final String numSigFreqItemsets = "numSigFreqItemsets";
  static final String adjustedCriticalValue = "adjustedCriticalValue";
  static final String minPvalues = "minPvalues";
  static final String totalRuntime = "totalRuntime";
  static final String createMatrixTime = "createMatrixTime";
  static final String estSampleAndMinetime = "estSampleAndMineTime";
  static final String wySampleAndMineTime = "wySampleAndMineTime";
  static final String getMinPvaluesTime = "getMinPvaluesTime";
  static final String setAdjustedCriticalValueTime = "setAdjustedCriticalValueTime";
  static final String mineSigFreqItemsetsTime = "mineSigFreqItemsetsTime";
  static final String numFreqItemsetsStats = "numFreqItemsetsStats";
  static final String numFreqItemsetsQuartiles = "numFreqItemsetsQuartiles";
  static final String freqItemsetLenToCount = "freqItemsetLenToCount";
  static final String freqItemsetLenToCountQuartiles = "freqItemsetLenToCountQuartiles";
  static final String sampleAndMine = "sampleAndMine";
}
