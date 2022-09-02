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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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

    public List<Integer> itemsList;
    public Map<Integer, Integer> itemToColIndex;

    /**
     * Creates a {@link SparseMatrix} from the dataset at datasetPath.
     * Rows are transactions and columns are items.
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
            this.itemToColIndex = Maps.newHashMap();
            this.itemsList = Lists.newArrayList(itemsSet);
            Collections.sort(this.itemsList);
            
            for (int i = 0; i < this.itemsList.size(); i++) {
                this.itemToColIndex.put(this.itemsList.get(i), i);
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
                    line.append(this.itemsList.get(c));
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
}
