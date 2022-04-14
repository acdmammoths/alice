package caterpillars.helpers;


import java.util.List;

public class SwappableLists {

    public final int swappable1;
    public final int swappable2;
    public final List<Integer> new1;
    public final List<Integer> new2;
    public boolean rowBased;

    /**
     */
    public SwappableLists(int swappable1, int swappable2, List<Integer> new1, List<Integer> new2, boolean rowBased) {
        this.swappable1 = swappable1;
        this.swappable2 = swappable2;
        this.new1 = new1;
        this.new2 = new2;
        this.rowBased = rowBased;
    }
}
