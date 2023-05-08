package alice.structures;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.Random;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RawFastIntCollectionFixedSize {

    public final int[] values;

    public RawFastIntCollectionFixedSize(RawFastIntCollectionFixedSize rficfs_a) {
        //
        this.values = new int[rficfs_a.values.length];
        System.arraycopy(rficfs_a.values, 0, this.values, 0, rficfs_a.values.length);
    }

    public RawFastIntCollectionFixedSize(final int[] values) {
        //
        this.values = new int[values.length];
        System.arraycopy(values, 0, this.values, 0, values.length);
    }

    public RawFastIntCollectionFixedSize(final Integer[] values) {
        //
        this.values = new int[values.length];
        System.arraycopy(values, 0, this.values, 0, values.length);
    }

    public RawFastIntCollectionFixedSize(final Collection<Integer> values) {
        this.values = new int[values.size()];
        int pos = 0;
        for (int v : values) {
            this.values[pos] = v;
            pos++;
        }
    }
    
    public RawFastIntCollectionFixedSize(final Collection<Integer> values, final Int2IntOpenHashMap mapper) {
        this.values = new int[values.size()];
        int pos = 0;
        for (int v : values) {
            this.values[pos] = mapper.get(v);
            pos++;
        }
    }

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

    public boolean isEmpty() {
        return this.values.length == 0;
    }

    public int size() {
        return this.values.length;
    }

    public boolean contains(final int v) {
        return this.findKey(v) != -1;
    }

    public void fastReplaceWithoutChecks(final int a, final int b) {
        this.values[this.findKey(a)] = b;
    }
    
    public int getRandomElement(Random r) {
        if (this.values.length == 0) {
            return -1;
        }
        return this.values[r.nextInt(this.values.length)];
    }
    
    public int getRandomElementExcept(Random r, RawFastIntCollectionFixedSize ot) {
        int element;
        do {
            element = this.values[r.nextInt(this.values.length)];
        } while (ot.contains(element));
        return element;
    }
    
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
