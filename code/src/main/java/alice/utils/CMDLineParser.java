package alice.utils;

/*
 * Copyright (C) 2022 Giulia Preti
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
/**
 * Methods to parse and store the arguments passed at runtime.
 */
public class CMDLineParser {
    
    public static void parse(String[] args) {
        if (args != null && args.length > 0) {
            parseArgs(args);
        }
    }

    private static void parseArgs(String[] args) {
        for (String arg : args) {
            String[] parts = arg.split("=");
            parseArg(parts[0], parts[1]);
        }
    }

    private static void parseArg(String key, String value) {
        if (key.compareToIgnoreCase("datasetPath") == 0) {
            Config.datasetPath = value;
        } else if (key.compareToIgnoreCase("datasetsDir") == 0) {
            Config.datasetsDir = value;
        } else if (key.compareToIgnoreCase("resultsDir") == 0) {
            Config.resultsDir = value;
        } else if (key.compareToIgnoreCase("numSwaps") == 0) {
            Config.numSwaps = Integer.parseInt(value);
        } else if (key.compareToIgnoreCase("maxNumSwapsFactor") == 0) {
            Config.maxNumSwapsFactor = Double.parseDouble(value);
        } else if (key.compareToIgnoreCase("numSamples") == 0) {
            Config.numSamples = Integer.parseInt(value);
        } else if (key.compareToIgnoreCase("numEstSamples") == 0) {
            Config.numEstSamples = Integer.parseInt(value);
        } else if (key.compareToIgnoreCase("numWySamples") == 0) {
            Config.numWySamples = Integer.parseInt(value);
        } else if (key.compareToIgnoreCase("numThreads") == 0) {
            Config.numThreads = Integer.parseInt(value);
        } else if (key.compareToIgnoreCase("minFreq") == 0) {
            Config.minFreq = Double.parseDouble(value);
        } else if (key.compareToIgnoreCase("fwer") == 0) {
            Config.fwer = Double.parseDouble(value);
        } else if (key.compareToIgnoreCase("seed") == 0) {
            Config.seed = Long.parseLong(value);
        } else if (key.compareToIgnoreCase("sampleAndMine") == 0) {
            Config.sampleAndMine = Boolean.valueOf(value);
        } else if (key.compareToIgnoreCase("cleanup") == 0) {
            Config.cleanup = Boolean.valueOf(value);
        }
    }
    
}
