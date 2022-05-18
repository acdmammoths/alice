package caterpillars.utils;

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
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * A class to log the setup time and step times of samplers.
 */
public class Timer {

    /**
     * Whether or not to log times.
     */
    private final boolean logTime;

    /**
     * An object to store the logged times and compute quartiles from.
     */
    private final DescriptiveStatistics times = new DescriptiveStatistics();

    /**
     * The start time.
     */
    private long start;

    /**
     * A variable to store a single time, such as setup time.
     */
    private long savedTime;

    public Timer(boolean logTime) {
        this.logTime = logTime;
    }

    public void start() {
        if (this.logTime) {
            this.start = System.currentTimeMillis();
        }
    }

    public void stop() {
        if (this.logTime) {
            final long elapsed = System.currentTimeMillis() - this.start;
            this.times.addValue(elapsed);
        }
    }

    public void save(long time) {
        if (this.logTime) {
            this.savedTime = time;
        }
    }

    public long getSavedTime() {
        return this.savedTime;
    }

    public double getMin() {
        double min = this.times.getMin();
        if (Double.compare(min, Double.NaN) != 0) {
            return min;
        }
        return 0.0;
    }

    public double getMax() {
        double max = this.times.getMax();
        if (Double.compare(max, Double.NaN) != 0) {
            return max;
        }
        return 0.0;
    }

    public double getPercentile(double percentile) {
        double time = this.times.getPercentile(percentile);
        if (Double.compare(time, Double.NaN) != 0) {
            return time;
        }
        return 0.0;
    }
}
