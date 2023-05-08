package alice.structures;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;

/**
 *
 * @author giulia
 */
public class MultiGraph {
    
    int[][] vertexToNeighbors; 
    Int2ObjectOpenHashMap<int[]> rowSumToVertices;
    
    public MultiGraph(int[][] vertexToNeighbors, 
            Int2ObjectOpenHashMap<int[]> rowSumToVertices) {

        this.vertexToNeighbors = vertexToNeighbors;
        this.rowSumToVertices = rowSumToVertices;
    }
    
    @Override
    public String toString() {
        String s = "[";
        for (int[] vertexToNeighbor : vertexToNeighbors) {
            s += Arrays.toString(vertexToNeighbor);
            s += "\n";
        }
        s += "]";
        return s;
    }
    
}
