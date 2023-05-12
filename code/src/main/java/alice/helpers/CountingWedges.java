package alice.helpers;

import alice.structures.RawFastIntCollectionFixedSize;
import alice.structures.Vector;
import com.google.common.primitives.Ints;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

public class CountingWedges {

    protected static long computeHash(int id_a, int id_b, int max_id_plus_one) {
        return id_a > id_b ? ((long) id_b * max_id_plus_one) + id_a : ((long) id_a * max_id_plus_one) + id_b;
    }

    protected static void computeReverseHash(long k, int max_id_plus_one, long[] result__id_a__id_b) {
        result__id_a__id_b[1] = k % max_id_plus_one;
        result__id_a__id_b[0] = (k - result__id_a__id_b[1]) / max_id_plus_one;
    }
    
    public static long countWedges(Vector[] elems, int max_id) {
        int max_id_plus_one = max_id + 1;
        Long2LongOpenHashMap map__id_id__num_occ = new Long2LongOpenHashMap(16 * max_id);
        map__id_id__num_occ.defaultReturnValue(0);
        long key;
        final long one = 1;
        int[] ngh;
        int id_0, id_1;
        for (Vector el : elems) {
            ngh = Ints.toArray(el.getNonzeroIndices());
            for (int i = 0; i < ngh.length; i++) {
                id_0 = ngh[i];
                for (int j = i + 1; j < ngh.length; j++) {
                    id_1 = ngh[j];
                    key = CountingWedges.computeHash(id_0, id_1, max_id_plus_one);
                    map__id_id__num_occ.addTo(key, one);
                }
            }
        }
        long v;
        long numButts = 0;
        for (Long2LongMap.Entry k_v : map__id_id__num_occ.long2LongEntrySet()) {
            v = k_v.getLongValue();
            numButts += (v * (v - 1)) / 2;
        }
        return numButts;
    }
    
    public static long countWedgesFast(RawFastIntCollectionFixedSize[] elems, int max_id) {

        int max_id_plus_one = max_id + 1;
        Long2LongOpenHashMap map__id_id__num_occ = new Long2LongOpenHashMap(16 * max_id);
        map__id_id__num_occ.defaultReturnValue(0);
        long key;
        final long one = 1;
        int[] ngh;
        int id_0, id_1;
        for (RawFastIntCollectionFixedSize el : elems) {
            ngh = el.values;
            for (int i = 0; i < ngh.length; i++) {
                id_0 = ngh[i];
                for (int j = i + 1; j < ngh.length; j++) {
                    id_1 = ngh[j];
                    key = CountingWedges.computeHash(id_0, id_1, max_id_plus_one);
                    map__id_id__num_occ.addTo(key, one);
                }
            }
        }
        long v;
        long numButts = 0;
        for (Long2LongMap.Entry k_v : map__id_id__num_occ.long2LongEntrySet()) {
            v = k_v.getLongValue();
            numButts += (v * (v - 1)) / 2;
        }
        return numButts;
    }

}
