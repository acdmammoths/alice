package alice.spm;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.text.DecimalFormat;
import java.util.List;

/**
 * This class represents a sequential pattern. A sequential pattern is a list of
 * itemsets.
 *
 * Copyright (c) 2008-2012 Philippe Fournier-Viger
 *
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */
public class SequentialPattern implements Comparable<SequentialPattern> {

    // the list of itemsets
    private final List<Itemset> itemsets;

    // IDs of sequences containing this pattern
    private IntArrayList sequencesIds;

    // whether the sequence was found (used in ProSecCo)
    private boolean isFound = false;

    // additional support count for the sequential pattern
    private int additionalSupport = 0;

    /**
     * Set the set of IDs of sequence containing this prefix
     *
     * @param sequencesIds a set of integer containing sequence IDs
     */
    public void setSequenceIDs(IntArrayList sequencesIds) {
        this.sequencesIds = sequencesIds;
    }

    /**
     * Defaults constructor
     */
    public SequentialPattern() {
        itemsets = Lists.newArrayList();
    }

    /**
     * Get the relative support of this pattern (a percentage)
     *
     * @param sequencecount the number of sequences in the original database
     * @return the support as a string
     */
    public String getRelativeSupportFormated(int sequencecount) {
        double relSupport = ((double) sequencesIds.size()) / ((double) sequencecount);
        // pretty formating :
        DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(5);
        return format.format(relSupport);
    }

    /**
     * Get the absolute support of this pattern.
     *
     * @return the support (an integer >= 1)
     */
    public int getAbsoluteSupport() {
        return sequencesIds.size();
    }

    /**
     * Add an itemset to this sequential pattern
     *
     * @param itemset the itemset to be added
     */
    public void addItemset(Itemset itemset) {
//		itemCount += itemset.size();
        itemsets.add(itemset);
    }

    /**
     * Copy the sequential pattern
     *
     * @return copy of the sequence pattern
     */
    public SequentialPattern copy() {
        SequentialPattern clone = new SequentialPattern();
        for (Itemset it : itemsets) {
            clone.addItemset(it.cloneItemSet());
        }
        clone.additionalSupport = this.additionalSupport;
        clone.sequencesIds = new IntArrayList(this.sequencesIds);
        return clone;
    }

    /**
     * Print this sequential pattern to System.out
     */
    public void print() {
        System.out.print(toString());
    }

    /**
     * Get a string representation of this sequential pattern, containing the
     * sequence IDs of sequence containing this pattern.
     */
    @Override
    public String toString() {
        StringBuilder r = new StringBuilder("");
        // For each itemset in this sequential pattern
        for (Itemset itemset : itemsets) {
            r.append('('); // begining of an itemset
            // For each item in the current itemset
            for (Integer item : itemset.getItems()) {
                String string = item.toString();
                r.append(string); // append the item
                r.append(' ');
            }
            r.append(')');// end of an itemset
        }
//		//  add the list of sequence IDs that contains this pattern.
//		if(getSequencesID() != null){
//			r.append("  Sequence ID: ");
//			for(Integer id : getSequencesID()){
//				r.append(id);
//				r.append(' ');
//			}
//		}
        return r.append("    ").toString();
    }

    public String toSave() {
        StringBuilder r = new StringBuilder("");
        // For each itemset in this sequential pattern
        for (Itemset itemset : itemsets) {
            // For each item in the current itemset
            for (Integer item : itemset.getItems()) {
                String string = item.toString();
                r.append(string); // append the item
                r.append(' ');
            }
            r.append("-1 ");
        }
        return r.substring(0, r.length()-4);
    }

    /**
     * Get a string representation of this sequential pattern.
     * @return a string representation of this sequential pattern
     */
    public String itemsetsToString() {
        StringBuilder r = new StringBuilder("");
        for (Itemset itemset : itemsets) {
            r.append('{');
            for (Integer item : itemset.getItems()) {
                String string = item.toString();
                r.append(string);
                r.append(' ');
            }
            r.append('}');
        }
        return r.append("    ").toString();
    }

    /**
     * Get the itemsets in this sequential pattern
     *
     * @return a list of itemsets.
     */
    public List<Itemset> getItemsets() {
        return itemsets;
    }

    /**
     * Get an itemset at a given position.
     *
     * @param index the position
     * @return the itemset
     */
    public Itemset get(int index) {
        return itemsets.get(index);
    }

    /**
     * Get the number of itemsets in this sequential pattern.
     *
     * @return the number of itemsets.
     */
    public int size() {
        return itemsets.size();
    }

    public IntArrayList getSequenceIDs() {
        return sequencesIds;
    }

    @Override
    public int compareTo(SequentialPattern o) {
        if (o == this) {
            return 0;
        }
        int compare = this.getAbsoluteSupport() - o.getAbsoluteSupport();
        if (compare != 0) {
            return compare;
        }

        return this.hashCode() - o.hashCode();
    }

    public boolean isFound() {
        return isFound;
    }

    public void addAdditionalSupport(int additionalSup) {
        this.additionalSupport += additionalSup;
    }

}
