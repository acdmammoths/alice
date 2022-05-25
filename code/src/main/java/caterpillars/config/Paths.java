package caterpillars.config;

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

import diffusr.samplers.Sampler;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * A helper class to construct file and directory paths.
 */
public class Paths {

    private static final String textExt = ".txt";
    private static final String jsonExt = ".json";
    public static final String estTag = "-est";
    public static final String wyTag = "-wy";

    public final String datasetBaseName;
    public final String samplesPath;
    public final String freqItemsetsDirPath;
    public final String sigFreqItemsetsDirPath;

    public Paths(String datasetPath, String resultsDir) {
        this.datasetBaseName = getBaseName(datasetPath);
        this.samplesPath = resultsDir + "samples/";
        this.freqItemsetsDirPath = resultsDir + "freqItemsets/";
        this.sigFreqItemsetsDirPath = resultsDir + "sigFreqItemsets/";
    }

    public String getSamplePath(String tag, int id) {
        return getTextFilePath(this.samplesPath, getSampleBaseFileName(tag, id));
    }

    public String getFreqItemsetsPath(String tag, int id) {
        return getTextFilePath(this.freqItemsetsDirPath, getSampleBaseFileName(tag, id));
    }

    public String getSigFreqItemsetsPath(
            Sampler sampler,
            int numSwaps,
            int numEstSamples,
            int numWySamples,
            double minFreq,
            double fwer,
            int numThreads,
            long seed) {
        final String sigFreqItemsetsBaseName
                = String.join(
                        Delimiters.dash,
                        this.datasetBaseName,
                        sampler.getClass().getName(),
                        String.valueOf(numSwaps),
                        String.valueOf(numEstSamples),
                        String.valueOf(numWySamples),
                        String.valueOf(minFreq),
                        String.valueOf(fwer),
                        String.valueOf(numThreads),
                        String.valueOf(seed));
        return getJsonFilePath(this.sigFreqItemsetsDirPath, sigFreqItemsetsBaseName);
    }

    public String getSampleBaseFileName(String tag, int id) {
        final String prefix = this.datasetBaseName + tag;
        return appendId(prefix, id);
    }

    public static String appendId(String prefix, int id) {
        return String.join(Delimiters.dash, prefix, String.valueOf(id));
    }

    public static String getBaseName(String fileName) {
        return FilenameUtils.getBaseName(fileName);
    }

    public static String concat(String basePath, String fullFileNameToAdd) {
        String path = FilenameUtils.concat(basePath, fullFileNameToAdd);
        if (path == null) {
            return basePath + fullFileNameToAdd + "/";
        }
        return path;
    }
    
    public static String getTextFilePath(String dir, String baseFileName) {
        String path = concat(dir, baseFileName + textExt);
        if (path == null) {
            return dir + baseFileName + textExt;
        }
        return path;
    }

    public static String getJsonFilePath(String dir, String baseFileName) {
        String path = concat(dir, baseFileName + jsonExt);
        if (path == null) {
            return dir + baseFileName + jsonExt;
        }
        return path;
    }

    public static void makeDir(String dirPath) {
        try {
            FileUtils.forceMkdir(new File(dirPath));
        } catch (IOException e) {
            System.err.println("Error in making directory " + dirPath);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void deleteDir(String dirPath) {
        try {
            final File dirToDelete = new File(dirPath);
            FileUtils.deleteDirectory(dirToDelete);
        } catch (IOException e) {
            System.err.println("Error in deleting directory " + dirPath);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
