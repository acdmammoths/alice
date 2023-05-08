package alice.structures;

/*
 * Copyright (C) 2022 Alexander Lee, Giulia Preti, and Matteo Riondato
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
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * An implementation of a sparse 0-1 matrix using an array of hash sets. Any
 * nonzero value that is inserted into the matrix is considered a 1.
 */
public class SparseMatrix_ {

    /**
     * A list of vectors to store the rows of the matrix.
     */
    private final RawFastIntCollectionFixedSize[] listOfRows;
    
    /**
     * A list of vectors to store the cols of the matrix.
     */
    private final RawFastIntCollectionFixedSize[] listOfCols;

    /**
     * Initialize an empty matrix with dimensions numRows x numCols.
     *
     * @param numRows the number of rows in the matrix
     * @param numCols the number of columns in the matrix
     */
    public SparseMatrix_(int numRows, int numCols) {
        this.listOfRows = new RawFastIntCollectionFixedSize[numRows];
        this.listOfCols = new RawFastIntCollectionFixedSize[numCols];
    }
    
    public SparseMatrix_(RawFastIntCollectionFixedSize[] rows, RawFastIntCollectionFixedSize[] cols) {
        this.listOfRows = new RawFastIntCollectionFixedSize[rows.length];
        for (int i = 0; i < rows.length; i++) {
            this.listOfRows[i] = new RawFastIntCollectionFixedSize(rows[i]);
        }
        this.listOfCols = new RawFastIntCollectionFixedSize[cols.length];
        for (int i = 0; i < cols.length; i++) {
            this.listOfCols[i] = new RawFastIntCollectionFixedSize(cols[i]);
        }
    }

    /**
     * Initializes the matrix using the entries in twoDimArray.
     *
     * @param twoDimArray a two dimensional array representation of the matrix
     */
    public SparseMatrix_(int[][] twoDimArray) {
        this(twoDimArray.length, twoDimArray[0].length);
        Map<Integer, IntArrayList> cols = Maps.newHashMap();
        for (int r = 0; r < twoDimArray.length; r++) {
            IntArrayList row = new IntArrayList();
            for (int c = 0; c < twoDimArray[0].length; c++) {
                if (twoDimArray[r][c] == 1) {
                    row.add(c);
                    if (!cols.containsKey(c)) {
                        cols.put(c, new IntArrayList(twoDimArray.length/2));
                    }
                    cols.get(c).add(r);
                }
            }
            this.listOfRows[r] = new RawFastIntCollectionFixedSize(row);
        }
        for (int c = 0; c < twoDimArray[0].length; c++) {
            this.listOfCols[c] = new RawFastIntCollectionFixedSize(cols.get(c));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        } 
        SparseMatrix_ otherMatrix = (SparseMatrix_) o;
        for (int i = 0; i < this.listOfRows.length; i++) {
            if (!this.listOfRows[i].equals(otherMatrix.listOfRows[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.listOfRows);
    }

    @Override
    public String toString() {
        return Arrays.toString(listOfRows);
    }

    public int getNumRows() {
        return this.listOfRows.length;
    }

    public int getNumCols() {
        return this.listOfCols.length;
    }

    public int isInRow(int r, int c) {
        if (this.listOfRows[r].contains(c)) {
            return 1;
        }
        return 0;
    }
    
    public void replaceValueInRow(int r, int oldc, int newc) {
        this.listOfRows[r].fastReplaceWithoutChecks(oldc, newc);
    }
    
    public void replaceValueInCol(int c, int oldr, int newr) {
        this.listOfCols[c].fastReplaceWithoutChecks(oldr, newr);
    }
    
    public void replaceRow(int r, RawFastIntCollectionFixedSize row) {
        this.listOfRows[r] = row;
    }
    
    public void replaceCol(int c, RawFastIntCollectionFixedSize col) {
        this.listOfCols[c] = col;
    }
    
    public int[] getNonzeroIndices(int r) {
        return this.listOfRows[r].values;
    }
    
    public int[] getNonzeroColIndices(int c) {
        return this.listOfCols[c].values;
    }

    public int getNumNonzeroIndices(int r) {
        return this.listOfRows[r].size();
    }

    public RawFastIntCollectionFixedSize getRowCopy(int r) {
        return new RawFastIntCollectionFixedSize(this.listOfRows[r]);
    }
    
    public RawFastIntCollectionFixedSize getColCopy(int c) {
        return new RawFastIntCollectionFixedSize(this.listOfCols[c]);
    }

    public RawFastIntCollectionFixedSize getRowInstance(int r) {
        return this.listOfRows[r];
    }
    
    public RawFastIntCollectionFixedSize getColInstance(int c) {
        return this.listOfCols[c];
    }
    
    public RawFastIntCollectionFixedSize[] getCols() {
        return this.listOfCols;
    }
    
    public RawFastIntCollectionFixedSize[] getRows() {
        return this.listOfRows;
    }

}
