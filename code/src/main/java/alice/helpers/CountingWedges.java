package alice.helpers;

import alice.structures.RawFastIntCollectionFixedSize;
import alice.structures.Vector;
import com.google.common.primitives.Ints;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

/*
 * Copyright (C) 2023 Adriano Fazzone
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
public class CountingWedges {

    /**
     * 
     * @param id_a first node
     * @param id_b second node
     * @param max_id_plus_one max id of a node in the graph
     * @return unique long encoding the pair the node
     */
    protected static long computeHash(int id_a, int id_b, int max_id_plus_one) {
        return id_a > id_b ? ((long) id_b * max_id_plus_one) + id_a : ((long) id_a * max_id_plus_one) + id_b;
    }

    /**
     * 
     * @param k hash value
     * @param max_id_plus_one max id of a node in the graph
     * @param result__id_a__id_b pairs of node ids represented by k
     */
    protected static void computeReverseHash(long k, int max_id_plus_one, long[] result__id_a__id_b) {
        result__id_a__id_b[1] = k % max_id_plus_one;
        result__id_a__id_b[0] = (k - result__id_a__id_b[1]) / max_id_plus_one;
    }
    
    /**
     * 
     * @param elems neighbours of each node in the graph
     * @param max_id max id of a node in the graph
     * @return number of butterflies in the graph
     */
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
    
    /**
     * 
     * @param elems neighbours of each node in the graph
     * @param max_id max id of a node in the graph
     * @return number of butterflies in the graph 
     */
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
