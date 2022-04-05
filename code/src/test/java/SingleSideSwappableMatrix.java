import alice.structures.Matrix;
import java.util.List;
import java.util.Set;

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
class SingleSideSwappableMatrix {

    final Matrix matrix;
    final boolean rowSwappable;
    final List<Set<Integer>> swappableIndices;

    SingleSideSwappableMatrix(Matrix matrix, boolean rowSwappable, List<Set<Integer>> swappableIndices) {
        this.matrix = matrix;
        this.rowSwappable = rowSwappable;
        this.swappableIndices = swappableIndices;
    }

}
