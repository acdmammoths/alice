package alice.structures;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.Collection;


public class RawFastIntCollectionFixedSizeWithOrder extends RawFastIntCollectionFixedSize {
    
    /**
     * 
     * @param rficfs_a another RawFastIntCollectionFixedSizeWithOrder
     */
    public RawFastIntCollectionFixedSizeWithOrder(RawFastIntCollectionFixedSizeWithOrder rficfs_a) {
        super(rficfs_a);
    }

    public RawFastIntCollectionFixedSizeWithOrder(final int[] values) {
        super(values);
    }

    public RawFastIntCollectionFixedSizeWithOrder(final Integer[] values) {
        super(values);
    }

    public RawFastIntCollectionFixedSizeWithOrder(final Collection<Integer> values) {
        super(values);
    }
    
    /**
     * 
     * @param values a collection of integers
     * @param mapper map storing the id of each element in values
     */
    public RawFastIntCollectionFixedSizeWithOrder(final Collection<Integer> values, final Int2IntOpenHashMap mapper) {
        super(values, mapper);
    }

    /**
     * 
     * @param o
     * @return true if this and o store the same values in the same positions
     */
    @Override
    public boolean equals(Object o) {
        RawFastIntCollectionFixedSizeWithOrder ot = (RawFastIntCollectionFixedSizeWithOrder) o;
        if (ot.values.length != this.values.length) {
            return false;
        }
        for (int i = 0; i < this.values.length; i++) {
            if (ot.values[i] != this.values[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
