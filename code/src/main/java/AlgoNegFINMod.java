/*
 * Copyright (C) 2022 Alexander Lee and Matteo Riondato
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
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A variant of SPMF's {@link AlgoNegFIN} that returns the collection of frequent itemsets, instead
 * of writing them to file.
 */
public class AlgoNegFINMod extends AlgoNegFIN {
  private Map<Set<Integer>, Integer> freqItemsetToSup; // frequent itemsets to their support

  /**
   * This method adds an itemset to freqItemsetsToSup + all itemsets that can be made using its node
   * list. We keep the method name even if it does not do what it says in order to reduce duplicate
   * code.
   *
   * @param curNode the current node
   * @param sameCount the same count
   */
  @Override
  void writeItemsetsToFile(SetEnumerationTreeNode curNode, int sameCount) {

    // initialize a frequent itemset
    Set<Integer> freqItemset = new HashSet<>();

    outputCount++;
    // append items from the itemset to the StringBuilder
    for (int i = 0; i < itemsetLen; i++) {
      freqItemset.add(item[itemset[i]].index);
    }

    // add the frequent itemset and its support to the map
    freqItemsetToSup.put(freqItemset, curNode.count);

    // === Add all combination that can be made using the node list of
    // this itemset
    if (sameCount > 0) {
      // generate all subsets of the node list except the empty set
      for (long i = 1, max = 1 << sameCount; i < max; i++) {
        Set<Integer> otherFreqItemset = new HashSet<>();
        for (int k = 0; k < itemsetLen; k++) {
          otherFreqItemset.add(item[itemset[k]].index);
        }

        // we create a new subset
        for (int j = 0; j < sameCount; j++) {
          // check if the j bit is set to 1
          int isSet = (int) i & (1 << j);
          if (isSet > 0) {
            // if yes, add it to the set
            otherFreqItemset.add(item[sameItems[j]].index);
            // newSet.add(item[sameItems[j]].index);
          }
        }
        freqItemsetToSup.put(otherFreqItemset, curNode.count);
        outputCount++;
      }
    }
  }

  /**
   * Run the algorithm.
   *
   * @param filename the input file path
   * @param minsup the minsup threshold
   * @return a map where each key is a frequent itemset and its value is its support
   * @throws IOException if error while reading file
   */
  public Map<Set<Integer>, Integer> runAlgorithm(String filename, double minsup)
      throws IOException {
    freqItemsetToSup = new HashMap<>();

    bmcTreeRoot = new BMCTreeNode();
    nlRoot = new SetEnumerationTreeNode();

    MemoryLogger.getInstance().reset();

    // record the start time
    startTimestamp = System.currentTimeMillis();

    // ==========================
    // Read Dataset
    scanDB(filename, minsup);

    itemsetLen = 0;
    itemset = new int[numOfFItem];

    // Build BMC-tree
    construct_BMC_tree(filename); // Lines 2 to 6 of algorithm 3 in the paper

    nlRoot.label = numOfFItem;
    nlRoot.firstChild = null;
    nlRoot.next = null;

    // Lines 12 to 19 of algorithm 3 in the paper
    // Initialize tree
    initializeSetEnumerationTree();
    sameItems = new int[numOfFItem];

    // Recursively constructing_frequent_itemset_tree the tree
    SetEnumerationTreeNode curNode = nlRoot.firstChild;
    nlRoot.firstChild = null;
    SetEnumerationTreeNode next = null;
    while (curNode != null) {
      next = curNode.next;
      // call the recursive "constructing_frequent_itemset_tree" method
      constructing_frequent_itemset_tree(curNode, 1, 0);
      curNode.next = null;
      curNode = next;
    }

    MemoryLogger.getInstance().checkMemory();

    // record the end time
    endTimestamp = System.currentTimeMillis();

    return freqItemsetToSup;
  }
}
