package alice.structures;

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
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An implementation of a sparse 0-1 vector using a hash set. Any nonzero value
 * that is inserted into the vector is considered a 1.
 */
public class Vector {

    /**
     * The set of indices in the vector. We only store the indices since this is
     * a 0-1 vector.
     */
    private final Set<Integer> indices;
    
    /**
     * Initializes an empty vector.
     */
    public Vector() {
        this.indices = new HashSet<>();
    }
    
    /**
     * 
     * @param indices positions where the vector is 1
     */
    public Vector(Collection<Integer> indices) {
        this.indices = Sets.newHashSet(indices);
    }

    /**
     * Initializes a vector from an array.
     * @param array 1-0 positions of the vector
     */
    public Vector(int[] array) {
        this();
        for (int i = 0; i < array.length; i++) {
            this.set(i, array[i]);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        Vector otherVector = (Vector) o;
        return Objects.equals(this.indices, otherVector.indices);
    }

    @Override
    public int hashCode() {
        return this.indices.hashCode();
    }

    @Override
    public String toString() {
        return this.indices.toString();
    }

    public Vector copy() {
        return new Vector(this.indices);
    }

    /**
     * 
     * @param index element id
     * @return 1 if the element is set in the vector; 0 otherwise
     */
    public int get(int index) {
        return this.indices.contains(index) ? 1 : 0;
    }

    /**
     * If value is 0, the index is removed from the vector; otherwise it is inserted.
     * 
     * @param index element id
     * @param value new value of the element
     */
    public void set(int index, int value) {
        if (value == 0) {
            this.indices.remove(index);
        } else {
            this.indices.add(index);
        }
    }

    /**
     * 
     * @return all the indices set in the vector
     */
    public Set<Integer> getNonzeroIndices() {
        return this.indices;
    }

    /**
     * 
     * @return number of elements set in the vector
     */
    public int getNumNonzeroIndices() {
        return this.indices.size();
    }
    
    /**
     * 
     * @param other a vector
     * @return number of elements in the intersection between the two vectors
     */
    public long interSize(Vector other) {
        return this.indices.stream()
                .filter(i -> other.indices.contains(i))
                .count();
    }
}
