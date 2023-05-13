package alice.utils;

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
import alice.structures.SparseMatrix;
import alice.config.Delimiters;
import alice.structures.MultiGraph;
import alice.structures.RawFastIntCollectionFixedSizeWithOrder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to transform a dataset into a 0-1 matrix representation of the
 * dataset.
 */
public class Transformer {

    // Transactional Datasets
    public IntArrayList itemsList;
    public Int2IntOpenHashMap itemToColIndex;
    // Sequential Datasets
    public Map<IntOpenHashSet, Integer> itemsetToIndex;
    public IntOpenHashSet[] indexToItemset;

    /**
     * Creates a {@link SparseMatrix} from the dataset at datasetPath. Rows are
     * transactions and columns are items.
     *
     * @param datasetPath the file path for the dataset
     * @return a {@link SparseMatrix} representation of the dataset
     */
    public SparseMatrix createMatrix(String datasetPath) {
        SparseMatrix matrix = null;

        try {
            final Set<Integer> itemsSet = Sets.newHashSet();
            int numRows = 0;
            BufferedReader br = new BufferedReader(new FileReader(datasetPath));
            String line;
            while ((line = br.readLine()) != null) {
                for (String itemString : line.split(Delimiters.space)) {
                    final int itemInt = Integer.parseInt(itemString);
                    itemsSet.add(itemInt);
                }
                numRows++;
            }
            br.close();

            matrix = new SparseMatrix(numRows, itemsSet.size());

            // Construct matrix such that items are sorted in increasing order of their integer value
            this.itemToColIndex = new Int2IntOpenHashMap();
            this.itemsList = new IntArrayList(itemsSet);
            Collections.sort(this.itemsList);

            for (int i = 0; i < this.itemsList.size(); i++) {
                this.itemToColIndex.put(this.itemsList.getInt(i), i);
            }
            int rowIndex = 0;
            br = new BufferedReader(new FileReader(datasetPath));
            while ((line = br.readLine()) != null) {
                for (String itemString : line.split(Delimiters.space)) {
                    final int colIndex = this.itemToColIndex.get(Integer.parseInt(itemString));
                    matrix.setInRow(rowIndex, colIndex, 1);
                    matrix.setInCol(rowIndex, colIndex, 1);
                }
                rowIndex++;
            }
            br.close();
        } catch (IOException e) {
            System.err.println("Error reading " + datasetPath);
            e.printStackTrace();
            System.exit(1);
        }
        return matrix;
    }

    /**
     * 
     * @param datasetPath the file path for the sequence dataset
     * @return multigraph representation of the sequence dataset
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public MultiGraph createMultiGraph(String datasetPath) throws FileNotFoundException, IOException {

        Int2ObjectOpenHashMap<IntArrayList> rowSumToVertices = new Int2ObjectOpenHashMap();
        Int2ObjectOpenHashMap<IntArrayList> colSumToVertices = new Int2ObjectOpenHashMap();
        List<int[]> tmpRV2N = Lists.newArrayList();
        List<IntArrayList> tmpCV2N = Lists.newArrayList();

        this.itemsetToIndex = Maps.newHashMap();
        
        BufferedReader br = new BufferedReader(new FileReader(datasetPath));
        String line;
        IntOpenHashSet itemSet;
        int leftCount = 0;
        int pos;

        while ((line = br.readLine()) != null) {
            pos = 0;
            int[] tmpRow = new int[line.split("-1").length - 1];
            for (String itemsetString : line.split("-1")) {
                itemsetString = itemsetString.trim();
                if (itemsetString.equals("-2")) {
                    continue;
                }
                itemSet = new IntOpenHashSet();
                for (String item : itemsetString.split(" ")) {
                    itemSet.add(Integer.parseInt(item));
                }
                if (!this.itemsetToIndex.containsKey(itemSet)) {
                    this.itemsetToIndex.put(itemSet, this.itemsetToIndex.size());
                    tmpCV2N.add(new IntArrayList());
                }
                tmpRow[pos] = this.itemsetToIndex.get(itemSet);
                tmpCV2N.get(this.itemsetToIndex.get(itemSet)).add(leftCount);
                pos++;
            }
            tmpRV2N.add(tmpRow);
            rowSumToVertices.putIfAbsent(tmpRow.length, new IntArrayList());
            rowSumToVertices.get(tmpRow.length).add(leftCount);
            leftCount++;
        }
        br.close();

        // Left Nodes = Transactions, Right Nodes = Itemsets
        RawFastIntCollectionFixedSizeWithOrder[] rowToNeighbors = new RawFastIntCollectionFixedSizeWithOrder[leftCount];
        for (int l = 0; l < leftCount; l++) {
            rowToNeighbors[l] = new RawFastIntCollectionFixedSizeWithOrder(tmpRV2N.get(l));
        }
        RawFastIntCollectionFixedSizeWithOrder[] colToNeighbors = new RawFastIntCollectionFixedSizeWithOrder[tmpCV2N.size()];
        for (int l = 0; l < colToNeighbors.length; l++) {
            colToNeighbors[l] = new RawFastIntCollectionFixedSizeWithOrder(tmpCV2N.get(l));
        }
        
        Int2ObjectOpenHashMap<int[]> rowSumToV = new Int2ObjectOpenHashMap();
        rowSumToVertices.keySet()
                .intStream()
                .forEach(i -> {
                    int[] vals = new int[rowSumToVertices.get(i).size()];
                    int p = 0;
                    for (int v : rowSumToVertices.get(i)) {
                        vals[p] = v;
                        p++;
                    }
                    rowSumToV.put(i, vals);
                });
        
        for (int c = 0 ; c < tmpCV2N.size(); c++) {
            int colSum = tmpCV2N.get(c).size();
            colSumToVertices.putIfAbsent(colSum, new IntArrayList());
            colSumToVertices.get(colSum).add(c);
        }
        Int2ObjectOpenHashMap<int[]> colSumToV = new Int2ObjectOpenHashMap();
        colSumToVertices.keySet()
                .intStream()
                .forEach(i -> {
                    int[] vals = new int[colSumToVertices.get(i).size()];
                    int p = 0;
                    for (int v : colSumToVertices.get(i)) {
                        vals[p] = v;
                        p++;
                    }
                    colSumToV.put(i, vals);
                });
        // update invertex index
        this.indexToItemset = new IntOpenHashSet[this.itemsetToIndex.size()];
        this.itemsetToIndex.entrySet().stream().forEach(entry -> {
            this.indexToItemset[entry.getValue()] = entry.getKey();
        });
        return new MultiGraph(rowToNeighbors, colToNeighbors, rowSumToV, colSumToV);
    }

    /**
     * Creates a dataset from a {@link SparseMatrix}.
     *
     * @param datasetPath the file path for the dataset to be written
     * @param matrix a {@link SparseMatrix} representation of the dataset
     */
    public void createDataset(String datasetPath, SparseMatrix matrix) {
        try {
            final BufferedWriter bw = new BufferedWriter(new FileWriter(datasetPath));
            for (int r = 0; r < matrix.getNumRows(); r++) {
                final StringBuilder line = new StringBuilder();
                for (int c : matrix.getNonzeroIndices(r)) {
                    line.append(this.itemsList.getInt(c));
                    line.append(Delimiters.space);
                }
                line.deleteCharAt(line.length() - 1);
                bw.write(line.toString());
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            System.err.println("Error writing to " + datasetPath);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 
     * @param datasetPath the file path for the dataset to be written
     * @param graph multigraph representation of the dataset
     */
    public void createSequenceDataset(String datasetPath, MultiGraph graph) {
        try {
            final BufferedWriter bw = new BufferedWriter(new FileWriter(datasetPath));
            for (int r = 0; r < graph.getNumRows(); r++) {
                final StringBuilder line = new StringBuilder();
                for (int c : graph.getRowInstance(r).values) {
                    for (int el : this.indexToItemset[c]) {
                        line.append(el);
                        line.append(" ");
                    }
                    line.append("-1 ");
                }
                line.append("-2");
                bw.write(line.toString());
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            System.err.println("Error writing to " + datasetPath);
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 
     * @param graph multi-graph representation of a sequence dataset
     * @return the sequence dataset as a list of sequences
     */
    public String[][] createSequenceDataset(MultiGraph graph) {
    	String[][] dataset = new String[graph.getNumRows()][];
        for (int r = 0; r < graph.getNumRows(); r++) {
            final List<String> line = Lists.newArrayList();
            for (int c : graph.getRowInstance(r).values) {
                for (int el : this.indexToItemset[c]) {
                    line.add(String.valueOf(el));
                }
                line.add("-1");
            }
            line.add("-2");
            String[] seq = new String[line.size()];
            for (int i = 0; i < seq.length; i++) {
            	seq[i] = line.get(i);
            }
            dataset[r] = seq;
        }
        return dataset;
    }

    /**
     * 
     * @return map storing the incremental id associated to each itemset in the sequence dataset
     * when it was transformed into a bipartite multigraph
     */
    public Int2IntOpenHashMap getItemToColIndex() {
        return this.itemToColIndex;
    }


}
