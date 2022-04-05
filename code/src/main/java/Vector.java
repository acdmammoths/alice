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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An implementation of a sparse 0-1 vector using a hash set. Any nonzero value that is inserted
 * into the vector is considered a 1.
 */
class Vector {
  /** The set of indices in the vector. We only store the indices since this is a 0-1 vector. */
  private final Set<Integer> indices;

  /** Initializes an empty vector. */
  Vector() {
    this.indices = new HashSet<>();
  }

  /** Initializes a vector from an array. */
  Vector(int[] array) {
    this();
    for (int i = 0; i < array.length; i++) {
      this.set(i, array[i]);
    }
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
      Vector otherVector = (Vector) o;
      return Objects.equals(this.indices, otherVector.indices);
    }
  }

  @Override
  public int hashCode() {
    return this.indices.hashCode();
  }

  @Override
  public String toString() {
    return this.indices.toString();
  }

  Vector copy() {
    final Vector copy = new Vector();
    for (int index : this.indices) {
      copy.set(index, 1);
    }
    return copy;
  }

  int get(int index) {
    return this.indices.contains(index) ? 1 : 0;
  }

  void set(int index, int value) {
    if (value == 0) {
      this.indices.remove(index);
    } else {
      this.indices.add(index);
    }
  }

  Set<Integer> getNonzeroIndices() {
    return this.indices;
  }

  int getNumNonzeroIndices() {
    return this.indices.size();
  }
}
