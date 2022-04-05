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
import java.util.Objects;

/** A class to represent an edge in the graph representation of a dataset. */
class Edge {
  final int row;
  final int col;

  Edge(int row, int col) {
    this.row = row;
    this.col = col;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o == null) {
      return false;
    } else if (this.getClass() != o.getClass()) {
      return false;
    } else {
      final Edge otherEdge = (Edge) o;
      return this.row == otherEdge.row && this.col == otherEdge.col;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.row, this.col);
  }

  @Override
  public String toString() {
    return "(" + this.row + ", " + this.col + ")";
  }
}
