
import caterpillars.structures.Matrix;
import java.util.List;
import java.util.Set;

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
