package alice.utils;


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
public class Utils {
    
    /**
     * 
     * @param numbers array of doubles
     * @return cumulative sum of numbers
     */
    public static double[] cumSum(double[] numbers) {
        double[] cumsum = new double[numbers.length];
        double sum = 0.;
        for (int i = 0; i < numbers.length; i++) {
            sum += numbers[i];
            cumsum[i] = sum;
        }
        return cumsum;
    }
}
