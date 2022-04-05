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

/** A class that helps compute the minimum p-values of the WY samples in parallel. */
class MinPvaluesTask implements Runnable {
  /** An object to access necessary paths easily. */
  private final Paths paths;

  /** The path to the file of frequent itemsets for which we want to find the minimum p-value. */
  private final String freqItemsetsPath;

  /** The number of estimate samples. */
  private final int numEstSamples;

  /** An array of minimum p-values to populate. */
  private final double[] minPvalues;

  /** The ID for this task, which is used to write to minPvalues. */
  private final int id;

  MinPvaluesTask(
      Paths paths, String freqItemsetsPath, int numEstSamples, double[] minPvalues, int id) {
    this.paths = paths;
    this.freqItemsetsPath = freqItemsetsPath;
    this.numEstSamples = numEstSamples;
    this.minPvalues = minPvalues;
    this.id = id;
  }

  @Override
  public void run() {
    final double minPvalue =
        Itemsets.getMinPvalue(this.paths, this.freqItemsetsPath, this.numEstSamples);
    this.minPvalues[this.id] = minPvalue;
    System.out.println("Minimum p-value for " + this.freqItemsetsPath + ": " + minPvalue);
  }
}
