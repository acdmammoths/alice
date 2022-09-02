package alice.helpers;


import java.util.List;

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
 * A class to hold the list of rows/cols to swap.
 */
public class SwappableLists {

    public final int swappable1;
    public final int swappable2;
    public final List<Integer> new1;
    public final List<Integer> new2;
    public final boolean rowBased;

    /**
     * Instantiates an instance of {@link SwappableLists}.
     * @param swappable1 id of the first vector
     * @param swappable2 id of the second vector
     * @param new1 new elements of the first vector
     * @param new2 new elements of the second vector
     * @param rowBased whether the ids refer to rows or columns
     */
    public SwappableLists(int swappable1, int swappable2, List<Integer> new1, List<Integer> new2, boolean rowBased) {
        this.swappable1 = swappable1;
        this.swappable2 = swappable2;
        this.new1 = new1;
        this.new2 = new2;
        this.rowBased = rowBased;
    }
    
    @Override
    public String toString() {
        return this.rowBased + "\t" + 
                this.swappable1 + " -> " + this.new1.toString() + "\n" + 
                this.swappable2 + " -> " + this.new2.toString();
    }
}
