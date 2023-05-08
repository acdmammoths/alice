package alice.helpers;


import alice.structures.Edge;

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
/**
 * A class to hold the pair of swappable edges and the pair of new edges that
 * transition to the adjacent matrix.
 */
public class SwappableAndNewEdges {

    public final Edge swappableEdge1;
    public final int e1Index;
    public final Edge swappableEdge2;
    public final int e2Index;

    /**
     * Instantiates an instance of {@link SwappableAndNewEdges}.
     *
     * @param swappableEdge1 the first swappable edge that transitions to the
     * adjacent matrix
     * @param swappableEdge2 the second swappable edge that transitions to the
     * adjacent matrix
     * @param index1 first index
     * @param index2 second index
     * @param rowSwap if the indexes refer to positions in rows or cols
     */
    public SwappableAndNewEdges(Edge swappableEdge1, Edge swappableEdge2, 
            int index1, int index2) {
        this.swappableEdge1 = swappableEdge1;
        this.swappableEdge2 = swappableEdge2;
        this.e1Index = index1;
        this.e2Index = index2;
    }
    
    @Override
    public String toString() {
        return swappableEdge1.toString() + "," + swappableEdge2.toString() 
                + "-" + e1Index + "," + e2Index;
    }
}
