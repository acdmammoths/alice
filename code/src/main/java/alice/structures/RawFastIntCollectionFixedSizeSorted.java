package alice.structures;

import java.util.Arrays;
import java.util.Random;

public class RawFastIntCollectionFixedSizeSorted extends RawFastIntCollectionFixedSize {

    public RawFastIntCollectionFixedSizeSorted(int[] values) {
        super(values);
        Arrays.sort(this.values);
        return;
    }

    public RawFastIntCollectionFixedSizeSorted(RawFastIntCollectionFixedSizeSorted rficfss_a) {
        super(rficfss_a);
        return;
    }

    @Override
    protected int findKey(final int o) {
        int midVal;
        int from = 0;
        int to = this.values.length - 1;
        int mid;
        while (from <= to) {
            mid = (from + to) >>> 1;
            midVal = this.values[mid];
            if (midVal < o) {
                from = mid + 1;
            } else if (midVal > o) {
                to = mid - 1;
            } else {
                return mid;
            }
        }
        return -(from + 1);
    }

    @Override
    public void fastReplaceWithoutChecks(final int a, final int b) {
        int index = this.findKey(a);
        this.values[index] = b;
        int tmp;
        while (true) {
            // edge case 1.
            if (index == 0) {
                if (this.values[index] <= this.values[index + 1]) {
                    return;
                }
                // swap right.
                tmp = this.values[index + 1];
                this.values[index + 1] = this.values[index];
                this.values[index] = tmp;
                index++;
                continue;
            }
            // edge case 2.
            if (index == this.values.length - 1) {
                if (this.values[index - 1] <= this.values[index]) {
                    return;
                }
                // swap left.
                tmp = this.values[index - 1];
                this.values[index - 1] = this.values[index];
                this.values[index] = tmp;
                index--;
                continue;
            }
            // central case.
            if (this.values[index - 1] > this.values[index]) {
                // swap left.
                tmp = this.values[index - 1];
                this.values[index - 1] = this.values[index];
                this.values[index] = tmp;
                index--;
                continue;
            }
            if (this.values[index] > this.values[index + 1]) {
                // swap right.
                tmp = this.values[index + 1];
                this.values[index + 1] = this.values[index];
                this.values[index] = tmp;
                index++;
                continue;
            }
            return;
        }
    }

    public static int computeIntersectionSize(RawFastIntCollectionFixedSizeSorted set_a,
            RawFastIntCollectionFixedSizeSorted set_b) {
        int num_common_elements = 0;
        int i = 0;
        int j = 0;
        final int[] values_a = set_a.values;
        final int[] values_b = set_b.values;
        while (true) {
            if (i >= values_a.length) {
                break;
            }
            if (j >= values_b.length) {
                break;
            }
            if (values_a[i] == values_b[j]) {
                num_common_elements++;
                j++;
                i++;
                continue;
            }
            if (values_a[i] > values_b[j]) {
                j++;
                continue;
            }
            i++;
        }
        return num_common_elements;
    }

    public int computeIntersectionSize(RawFastIntCollectionFixedSizeSorted set_b) {
        int num_common_elements = 0;
        int i = 0;
        int j = 0;
        final int[] values_b = set_b.values;
        while (true) {
            if (i >= this.values.length) {
                break;
            }
            if (j >= values_b.length) {
                break;
            }
            if (this.values[i] == values_b[j]) {
                num_common_elements++;
                j++;
                i++;
                continue;
            }
            if (this.values[i] > values_b[j]) {
                j++;
                continue;
            }
            i++;
        }
        return num_common_elements;
    }

    public int[] getTwoRandomElementsInTheXor(RawFastIntCollectionFixedSizeSorted set_b, Random rnd) {
        //
        int int_size = this.computeIntersectionSize(set_b);
        //
        if ((this.values.length == int_size) || (set_b.size() == int_size)) {
            return null;
        }
        int[] result = {-1, -1};
        int random_order_a = rnd.nextInt(this.size() - int_size);
        int random_order_b = rnd.nextInt(set_b.size() - int_size);
        int c_order_a = 0;
        int c_order_b = 0;
        int i = 0;
        int j = 0;
        final int[] values_b = set_b.values;
        while (true) {
            if (i >= this.values.length) {
                break;
            }
            if (j >= values_b.length) {
                break;
            }
//	    System.out.println("i=" + i + " j=" + j + " c_index_a=" + c_order_a + " c_index_b=" + c_order_b + " r_a="
//					+ r_a + " r_b=" + r_b + " this.values[i]=" + this.values[i] + " values_b[j]=" + values_b[j]);
            if (this.values[i] == values_b[j]) {
                j++;
                i++;
                continue;
            }
            //
            if (this.values[i] > values_b[j]) {
                if (c_order_b == random_order_b) {
                    result[1] = values_b[j];
                }
                c_order_b++;
                j++;
                continue;
            }
            //
            if (c_order_a == random_order_a) {
                result[0] = this.values[i];
            }
            c_order_a++;
            i++;
        }
        if (i < this.values.length && result[0] == -1) {
            result[0] = this.values[i + random_order_a - c_order_a];
        }
        if (j < values_b.length && result[1] == -1) {
            result[1] = values_b[j + random_order_b - c_order_b];
        }
        //
        return result;
    }

    public static boolean isIntersectionSizeOverTheThreshold(RawFastIntCollectionFixedSizeSorted set_a,
            RawFastIntCollectionFixedSizeSorted set_b, int threshold) {
        int num_common_elements = 0;
        int i = 0;
        int j = 0;
        final int[] values_a = set_a.values;
        final int[] values_b = set_b.values;
        while (true) {
            if (i >= values_a.length) {
                break;
            }
            if (j >= values_b.length) {
                break;
            }
            if (values_a[i] == values_b[j]) {
                num_common_elements++;
                if (num_common_elements >= threshold) {
                    return true;
                }
                j++;
                i++;
                continue;
            }
            if (values_a[i] > values_b[j]) {
                j++;
                continue;
            }
            i++;
        }
        return false;
    }

    public static void main(String[] args) {
        Random rnd = new Random();
        int[] a_f = {3, 4, 1, 5, 7};
        RawFastIntCollectionFixedSizeSorted first = new RawFastIntCollectionFixedSizeSorted(a_f);
        int[] a_s = {3, 4, 7};
        RawFastIntCollectionFixedSizeSorted second = new RawFastIntCollectionFixedSizeSorted(a_s);
        int[] a_t = {1, 2};
        RawFastIntCollectionFixedSizeSorted third = new RawFastIntCollectionFixedSizeSorted(a_t);
        System.out.println();
        System.out.println("first : " + first);
        System.out.println("second: " + second);
        System.out.println(Arrays.toString(first.getTwoRandomElementsInTheXor(second, rnd)));
        //
        System.out.println();
        System.out.println("first : " + first);
        System.out.println("third : " + third);
        System.out.println(Arrays.toString(first.getTwoRandomElementsInTheXor(third, rnd)));
//        System.out.println(Arrays.toString(third.getTwoRandomElementsInTheXor(first, rnd)));
    }

}
