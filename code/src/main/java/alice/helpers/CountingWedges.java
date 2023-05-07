package alice.helpers;

import alice.structures.Vector;
import com.google.common.primitives.Ints;
import java.io.*;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.List;
import java.util.Set;
import org.javatuples.Pair;
//import samosa.structures.collections.RawFastIntCollectionFixedSizeSorted;

public class CountingWedges {

    protected static long computeHash(int id_a, int id_b, int max_id_plus_one) {
        return id_a > id_b ? ((long) id_b * max_id_plus_one) + id_a : ((long) id_a * max_id_plus_one) + id_b;
    }

    protected static void computeReverseHash(long k, int max_id_plus_one, long[] result__id_a__id_b) {
        result__id_a__id_b[1] = k % max_id_plus_one;
        result__id_a__id_b[0] = (k - result__id_a__id_b[1]) / max_id_plus_one;
    }
    
    public static long countWedges(List<Pair<Set<Integer>, Set<Integer>>> elems, int direction, int max_id) {
        int max_id_plus_one = max_id + 1;
        Long2LongOpenHashMap map__id_id__num_occ = new Long2LongOpenHashMap(16 * max_id);
        map__id_id__num_occ.defaultReturnValue(0);
        long key;
        final long one = 1;
        int[] ngh;
        //
        int id_0, id_1;
        for (Pair<Set<Integer>, Set<Integer>> el : elems) {
            ngh = Ints.toArray(el.getValue1());
            if (direction == -1) {
                ngh = Ints.toArray(el.getValue0());
            }
            for (int i = 0; i < ngh.length; i++) {
                id_0 = ngh[i];
                for (int j = i + 1; j < ngh.length; j++) {
                    id_1 = ngh[j];
                    key = CountingWedges.computeHash(id_0, id_1, max_id_plus_one);
                    map__id_id__num_occ.addTo(key, one);
                }
            }
        }
        long k, v;
//        long[] result__id_a__id_b = {-1, -1};
//        long id_a, id_b;
        long numButts = 0;
        for (Long2LongMap.Entry k_v : map__id_id__num_occ.long2LongEntrySet()) {
            k = k_v.getLongKey();
            v = k_v.getLongValue();
//            CountingWedges.computeReverseHash(k, max_id_plus_one, result__id_a__id_b);
//            id_a = result__id_a__id_b[0];
//            id_b = result__id_a__id_b[1];
            numButts += (v * (v - 1)) / 2;
//            System.out.println("(" + id_a + ", " + id_b + ") : " + v);
        }
        return numButts;
    }
    
    public static long countWedges(List<Vector> elems, int max_id) {
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
    
//    public static long countWedgesFast(List<Pair<RawFastIntCollectionFixedSizeSorted, RawFastIntCollectionFixedSizeSorted>> elems, 
//            int direction, 
//            int max_id) {
//
//        int max_id_plus_one = max_id + 1;
//        Long2LongOpenHashMap map__id_id__num_occ = new Long2LongOpenHashMap(16 * max_id);
//        map__id_id__num_occ.defaultReturnValue(0);
//        long key;
//        final long one = 1;
//        int[] ngh;
//        int id_0, id_1;
//        for (Pair<RawFastIntCollectionFixedSizeSorted, RawFastIntCollectionFixedSizeSorted> el : elems) {
//            ngh = el.getValue1().values;
//            if (direction == -1) {
//                ngh = el.getValue0().values;
//            }
//            for (int i = 0; i < ngh.length; i++) {
//                id_0 = ngh[i];
//                for (int j = i + 1; j < ngh.length; j++) {
//                    id_1 = ngh[j];
//                    key = CountingWedges.computeHash(id_0, id_1, max_id_plus_one);
//                    map__id_id__num_occ.addTo(key, one);
//                }
//            }
//        }
//        long v;
//        long numButts = 0;
//        for (Long2LongMap.Entry k_v : map__id_id__num_occ.long2LongEntrySet()) {
//            v = k_v.getLongValue();
//            numButts += (v * (v - 1)) / 2;
//        }
//        return numButts;
//    }

    public static void main(String[] args) throws Exception {

        String in_file_name = "/Users/ikki/Library/CloudStorage/Dropbox/Giulia/rand_1000_100000_40.tsv";

        BufferedReader br = new BufferedReader(new FileReader(in_file_name));
        String line;
        int max_id = 0;
        int c_id = 0;
        while ((line = br.readLine()) != null) {
            String[] s_0__s_1 = line.split("\t");
            String[] s_0 = s_0__s_1[0].split(",");
//			String[] s_1 = s_0__s_1[1].split(",");
            for (String id_as_string : s_0) {
                c_id = Integer.parseInt(id_as_string);
                max_id = (c_id > max_id ? c_id : max_id);
            }
        }
        br.close();
        System.out.println("max_id= " + max_id);
        int max_id_plus_one = max_id + 1;
        Long2LongOpenHashMap map__id_id__num_occ = new Long2LongOpenHashMap(16 * max_id);
        map__id_id__num_occ.defaultReturnValue(0);
        long key;
        final long one = 1;
        //
        int id_0, id_1;
        br = new BufferedReader(new FileReader(in_file_name));
        while ((line = br.readLine()) != null) {
            String[] s_0__s_1 = line.split("\t");
            String[] s_0 = s_0__s_1[0].split(",");
//			String[] s_1 = s_0__s_1[1].split(",");
//			String[] s = s_0;
//			System.out.println();
//			System.out.println(Arrays.toString(s_0__s_1));
//			System.out.println(Arrays.toString(s_0));
//			System.out.println(Arrays.toString(s_1));
            for (int i = 0; i < s_0.length; i++) {
                id_0 = Integer.parseInt(s_0[i]);
                for (int j = i + 1; j < s_0.length; j++) {
                    id_1 = Integer.parseInt(s_0[j]);
                    key = CountingWedges.computeHash(id_0, id_1, max_id_plus_one);
                    map__id_id__num_occ.addTo(key, one);
                }
            }
        }
        br.close();
        long k, v;
        long[] result__id_a__id_b = {-1, -1};
        long id_a, id_b;
        for (Long2LongMap.Entry k_v : map__id_id__num_occ.long2LongEntrySet()) {
            k = k_v.getLongKey();
            v = k_v.getLongValue();
            CountingWedges.computeReverseHash(k, max_id_plus_one, result__id_a__id_b);
            id_a = result__id_a__id_b[0];
            id_b = result__id_a__id_b[1];
            System.out.println("(" + id_a + ", " + id_b + ") : " + v);
        }
        System.out.println("map__id_id__num_occ.size()= " + map__id_id__num_occ.size());
    }

}
