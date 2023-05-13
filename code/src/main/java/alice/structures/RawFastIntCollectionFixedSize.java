package alice.structures;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.Random;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RawFastIntCollectionFixedSize {

    // values stored in this object
    public final int[] values;

    /**
     * Copies the values in rficfs_a in this object
     * @param rficfs_a another RawFastIntCollectionFixedSize
     */
    public RawFastIntCollectionFixedSize(RawFastIntCollectionFixedSize rficfs_a) {
        this.values = new int[rficfs_a.values.length];
        System.arraycopy(rficfs_a.values, 0, this.values, 0, rficfs_a.values.length);
    }

    /**
     * Copies the values in values in this object
     * @param values an array of integers
     */
    public RawFastIntCollectionFixedSize(final int[] values) {
        //
        this.values = new int[values.length];
        System.arraycopy(values, 0, this.values, 0, values.length);
    }

    /**
     * Copies the values in values in this object
     * @param values an array of Integers
     */
    public RawFastIntCollectionFixedSize(final Integer[] values) {
        //
        this.values = new int[values.length];
        System.arraycopy(values, 0, this.values, 0, values.length);
    }

    /**
     * Copies the values in values in this object
     * @param values a collection of Integers
     */
    public RawFastIntCollectionFixedSize(final Collection<Integer> values) {
        this.values = new int[values.size()];
        int pos = 0;
        for (int v : values) {
            this.values[pos] = v;
            pos++;
        }
    }
    
    /**
     * Copies the values in values in this object using the id map
     * @param values a collection of Integers
     * @param mapper ids associated to the elements in values
     */
    public RawFastIntCollectionFixedSize(final Collection<Integer> values, final Int2IntOpenHashMap mapper) {
        this.values = new int[values.size()];
        int pos = 0;
        for (int v : values) {
            this.values[pos] = mapper.get(v);
            pos++;
        }
    }

    /**
     * 
     * @param o int
     * @return the position of o in this object; -1 if it is not present
     */
    protected int findKey(final int o) {
        //
        for (int i = this.values.length; i-- != 0;) {
            if (((this.values[i]) == (o))) {
                return i;
            }
        }
        //
        return -1;
    }

    /**
     * 
     * @return true if this object does not store any value
     */
    public boolean isEmpty() {
        return this.values.length == 0;
    }

    /**
     * 
     * @return number of elements stored in  this object
     */
    public int size() {
        return this.values.length;
    }

    /**
     * 
     * @param v int
     * @return true if v is contained in this object; false otherwise
     */
    public boolean contains(final int v) {
        return this.findKey(v) != -1;
    }

    /**
     * Replace a with b in this object
     * @param a int
     * @param b int
     */
    public void fastReplaceWithoutChecks(final int a, final int b) {
        this.values[this.findKey(a)] = b;
    }
    
    /**
     * 
     * @param r instance of Random
     * @return a random element in this object
     */
    public int getRandomElement(Random r) {
        if (this.values.length == 0) {
            return -1;
        }
        return this.values[r.nextInt(this.values.length)];
    }
    
    /**
     * 
     * @param o another Object
     * @return true if this and o stores the same elements; 0 otherwise
     */
    @Override
    public boolean equals(Object o) {
        RawFastIntCollectionFixedSize ot = (RawFastIntCollectionFixedSize) o;
        if (ot.values.length != this.values.length) {
            return false;
        }
        int common = 0;
        for (int i = 0; i < this.values.length; i++) {
            if (!ot.contains(this.values[i])) {
                return false;
            }
            common++;
        }
        return (common == this.values.length);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int v : this.values) {
            hash += v;
        }
        return hash;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.values);
    }

    /**
     * 
     * @param rficfs_b another RawFastIntCollectionFixedSize
     * @return elements in this object that are not in rficfs_b
     */
    public RawFastIntCollectionFixedSize computeDifference(RawFastIntCollectionFixedSize rficfs_b) {
        List<Integer> diff = Lists.newArrayList();
        int i;
        for (i = 0; i < this.values.length; i++) {
            if (!rficfs_b.contains(this.values[i])) {
                diff.add(this.values[i]);
            }
        }
        int[] a = new int[diff.size()];
        for (i = 0; i < a.length; i++) {
            a[i] = diff.get(i);
        }
        return new RawFastIntCollectionFixedSize(a);
    }

    /**
     * 
     * @param rficfs_b another RawFastIntCollectionFixedSize
     * @return the elements stored both in this object and in rficfs_b
     */
    public RawFastIntCollectionFixedSize computeIntersection(RawFastIntCollectionFixedSize rficfs_b) {
        List<Integer> inter = Lists.newArrayList();
        int i;
        for (i = 0; i < this.values.length; i++) {
            if (rficfs_b.contains(this.values[i])) {
                inter.add(this.values[i]);
            }
        }
        int[] a = new int[inter.size()];
        for (i = 0; i < a.length; i++) {
            a[i] = inter.get(i);
        }
        return new RawFastIntCollectionFixedSize(a);
    }
    
    /**
     * 
     * @param rficfs_b another RawFastIntCollectionFixedSize
     * @return an array where the fist element contains all the ints in this 
     * object but not in rficfs_b, and the second element contains all the ints 
     * in rficfs_b but not in this object
     */
    public RawFastIntCollectionFixedSize[] computeXORMultiSets(RawFastIntCollectionFixedSize rficfs_b) {
        Int2IntOpenHashMap first = new Int2IntOpenHashMap(this.values.length * 2);
        Int2IntOpenHashMap second = new Int2IntOpenHashMap(rficfs_b.values.length * 2);
        first.defaultReturnValue(0);
        second.defaultReturnValue(0);
        for (int v : this.values) {
            first.addTo(v, 1);
        }
        for (int v : rficfs_b.values) {
            if (first.containsKey(v)) {
                first.remove(v);
            } else {
                second.addTo(v, 1);
            }
        }
        RawFastIntCollectionFixedSize firstX = new RawFastIntCollectionFixedSize(first.keySet());
        RawFastIntCollectionFixedSize secondX = new RawFastIntCollectionFixedSize(second.keySet());
        return new RawFastIntCollectionFixedSize[]{firstX, secondX};
    }
    
    /**
     * 
     * @param rficfs_b another RawFastIntCollectionFixedSize
     * @return number of elements both in this object and rficfs_b
     */
    public int computeInterSize(RawFastIntCollectionFixedSize rficfs_b) {
        int inter = 0;
        int i;
        for (i = 0; i < this.values.length; i++) {
            if (rficfs_b.contains(this.values[i])) {
                inter++;
            }
        }
        return inter;
    }

}
