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
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.Set;

/**
 * An implementation of a sparse 0-1 matrix using an array of hash sets. Any
 * nonzero value that is inserted into the matrix is considered a 1.
 */
public class SparseMatrix {

    /**
     * A list of vectors to store the rows of the matrix.
     */
    private final Vector[] listOfRows;
    
    /**
     * A list of vectors to store the cols of the matrix.
     */
    private final Vector[] listOfCols;

    /**
     * Initialize an empty matrix with dimensions numRows x numCols.
     *
     * @param numRows the number of rows in the matrix
     * @param numCols the number of columns in the matrix
     */
    public SparseMatrix(int numRows, int numCols) {
        this.listOfRows = new Vector[numRows];
        for (int i = 0; i < numRows; i++) {
            listOfRows[i] = new Vector();
        }
        this.listOfCols = new Vector[numCols];
        for (int i = 0; i < numCols; i++) {
            listOfCols[i] = new Vector();
        }
    }
    
    /**
     * Initialize a matrix with given rows and cols.
     *
     * @param rows the rows to be copied in the matrix
     * @param cols the columns to be copied in the matrix
     */
    public SparseMatrix(Vector[] rows, Vector[] cols) {
        this.listOfRows = new Vector[rows.length];
        for (int i = 0; i < rows.length; i++) {
            this.listOfRows[i] = rows[i].copy();
        }
        this.listOfCols = new Vector[cols.length];
        for (int i = 0; i < cols.length; i++) {
            this.listOfCols[i] = cols[i].copy();
        }
    }

    /**
     * Initializes the matrix using the entries in twoDimArray.
     *
     * @param twoDimArray a two dimensional array representation of the matrix
     */
    public SparseMatrix(int[][] twoDimArray) {
        this(twoDimArray.length, twoDimArray[0].length);
        for (int r = 0; r < twoDimArray.length; r++) {
            for (int c = 0; c < twoDimArray[0].length; c++) {
                setInRow(r, c, twoDimArray[r][c]);
                setInCol(r, c, twoDimArray[r][c]);
            }
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
        SparseMatrix otherMatrix = (SparseMatrix) o;
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

    /**
     * 
     * @return number of rows in the matrix
     */
    public int getNumRows() {
        return this.listOfRows.length;
    }

    /**
     * 
     * @return number of columns in the matrix
     */
    public int getNumCols() {
        return this.listOfCols.length;
    }

    /**
     * 
     * @param r row id
     * @param c col id
     * @return 1 if c is in the row r; 0 otherwise
     */
    public int isInRow(int r, int c) {
        return this.listOfRows[r].get(c);
    }
    
    /**
     * If 0, removes col c from row r; if 1 adds col c to row r.
     * @param r row id
     * @param c col id
     * @param value value (0 or 1)
     */
    public void setInRow(int r, int c, int value) {
        this.listOfRows[r].set(c, value);
    }

    /**
     * Replace the r-th row with this row.
     * @param r row id
     * @param row new row
     */
    public void replaceRow(int r, Vector row) {
        this.listOfRows[r] = row;
    }
    
    /**
     * Replace the c-th col with this col.
     * @param c col id
     * @param col new col
     */
    public void replaceCol(int c, Vector col) {
        this.listOfCols[c] = col;
    }
    
    /**
     * If 0, removes row r from col c; if 1 adds row r to col c.
     * @param r row id
     * @param c col id
     * @param value value (0 or 1)
     */
    public void setInCol(int r, int c, int value) {
        this.listOfCols[c].set(r, value);
    }

    /**
     * 
     * @param r row id
     * @return non-zero indices in row r
     */
    public IntOpenHashSet getNonzeroIndices(int r) {
        return this.listOfRows[r].getNonzeroIndices();
    }
    
    /**
     * 
     * @param c col id
     * @return non-zero indices in col c
     */
    public IntOpenHashSet getNonzeroColIndices(int c) {
        return this.listOfCols[c].getNonzeroIndices();
    }

    /**
     * 
     * @param r row id
     * @return number of non-zero indices in row r
     */
    public int getNumNonzeroIndices(int r) {
        return this.listOfRows[r].getNumNonzeroIndices();
    }

    /**
     * 
     * @param r row id
     * @return a copy of row
     */
    public Vector getRowCopy(int r) {
        return this.listOfRows[r].copy();
    }

    /**
     * 
     * @param r row id
     * @return reference to row
     */
    public Vector getRowInstance(int r) {
        return this.listOfRows[r];
    }
    
    /**
     * 
     * @param c col id
     * @return a copy of col
     */
    public Vector getColCopy(int c) {
        return this.listOfCols[c].copy();
    }

    /**
     * 
     * @param c col id
     * @return reference to col
     */
    public Vector getColInstance(int c) {
        return this.listOfCols[c];
    }
    
    /**
     * 
     * @return array with the cols in this matrix
     */
    public Vector[] getCols() {
        return this.listOfCols;
    }
    
    /**
     * 
     * @return array with the rows in this matrix
     */
    public Vector[] getRows() {
        return this.listOfRows;
    }

    /**
     * Gets the dot product between the values of rows r1 and r2.
     *
     * @param r1 the first row index
     * @param r2 the second row index
     * @return dot product between the values of rows r1 and r2
     */
    public int getRowDotProd(int r1, int r2) {
        int shorter;
        int longer;
        if (this.getNumNonzeroIndices(r1) < this.getNumNonzeroIndices(r2)) {
            shorter = r1;
            longer = r2;
        } else {
            shorter = r2;
            longer = r1;
        }

        int rowDotProd = 0;
        final Set<Integer> longerNonzeroIndices = this.getNonzeroIndices(longer);
        for (int c : this.getNonzeroIndices(shorter)) {
            if (longerNonzeroIndices.contains(c)) {
                rowDotProd++;
            }
        }

        return rowDotProd;
    }

    /**
     * Gets the number of entries that are not equal across the two rows.The
 implementation of this method is preferred over iterating over all the
 columns in the matrix because each row in the matrix usually contains
 only a few nonzero entries.
     *
     * @param r1 the first row index
     * @param r2 the second row index
     * @return number of entries that are not equal across the two rows
     */
    public int getNumEntriesNeq(int r1, int r2) {
        final Set<Integer> row1NonzeroIndices = this.getNonzeroIndices(r1);
        final Set<Integer> row2NonzeroIndices = this.getNonzeroIndices(r2);

        int numEntriesNeq = 0;
        for (int c : row1NonzeroIndices) {
            if (!row2NonzeroIndices.contains(c)) {
                numEntriesNeq++;
            }
        }
        for (int c : row2NonzeroIndices) {
            if (!row1NonzeroIndices.contains(c)) {
                numEntriesNeq++;
            }
        }

        return numEntriesNeq;
    }
    
    /**
     * Determines whether all entries are equal across the two rows, excluding
     * the entries in the specified skipped columns. The implementation of this
     * method is preferred over iterating over all the columns in the matrix
     * because each row in the matrix usually contains only a few nonzero entries.
     *
     * @param r1 the first row index
     * @param r2 the second row index
     * @param skipCol1 the first column index to skip
     * @param skipCol2 the second column index to skip
     * @return true if all entries but the skipped ones are equal across the two rows
     */
    public boolean entriesEqual(int r1, int r2, int skipCol1, int skipCol2) {
        final Set<Integer> row1NonzeroIndices = this.getNonzeroIndices(r1);
        final Set<Integer> row2NonzeroIndices = this.getNonzeroIndices(r2);

        for (int c : row1NonzeroIndices) {
            if (c == skipCol1 || c == skipCol2) {
                continue;
            }
            if (!row2NonzeroIndices.contains(c)) {
                return false;
            }
        }
        for (int c : row2NonzeroIndices) {
            if (c == skipCol1 || c == skipCol2) {
                continue;
            }
            if (!row1NonzeroIndices.contains(c)) {
                return false;
            }
        }

        return true;
    }
    
}
