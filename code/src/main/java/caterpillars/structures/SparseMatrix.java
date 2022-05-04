package caterpillars.structures;

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
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An implementation of a sparse 0-1 matrix using an array of hash sets. Any
 * nonzero value that is inserted into the matrix is considered a 1.
 */
public class SparseMatrix {

    /**
     * A list of vectors to store the rows of the matrix.
     */
    private final List<Vector> listOfRows;
    
    /**
     * A list of vectors to store the cols of the matrix.
     */
    private final List<Vector> listOfCols;

    /**
     * Initialize an empty matrix with dimensions numRows x numCols.
     *
     * @param numRows the number of rows in the matrix
     * @param numCols the number of columns in the matrix
     */
    public SparseMatrix(int numRows, int numCols) {
        this.listOfRows = IntStream.range(0, numRows)
                .mapToObj(i -> new Vector())
                .collect(Collectors.toList());
        this.listOfCols = IntStream.range(0, numCols)
                .mapToObj(i -> new Vector())
                .collect(Collectors.toList());
    }
    
    public SparseMatrix(List<Vector> rows, List<Vector> cols) {
        this.listOfRows = Lists.newArrayList();
        for (Vector row : rows) {
            this.listOfRows.add(row.copy());
        }
        this.listOfCols = Lists.newArrayList();
        for (Vector col : cols) {
            this.listOfCols.add(col.copy());
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
        } else if (o == null) {
            return false;
        } else if (this.getClass() != o.getClass()) {
            return false;
        } else {
            SparseMatrix otherMatrix = (SparseMatrix) o;
            return Objects.equals(this.listOfRows, otherMatrix.listOfRows);
        }
    }

    @Override
    public int hashCode() {
        return this.listOfRows.hashCode();
    }

    @Override
    public String toString() {
        return this.listOfRows.toString();
    }

    public int getNumRows() {
        return this.listOfRows.size();
    }

    public int getNumCols() {
        return this.listOfCols.size();
    }

    public int isInRow(int r, int c) {
        return this.listOfRows.get(r).get(c);
    }
    
    public void setInRow(int r, int c, int value) {
        this.listOfRows.get(r).set(c, value);
    }
    
    public void replaceRow(int r, Vector row) {
        this.listOfRows.set(r, row);
    }
    
    public void replaceCol(int c, Vector col) {
        this.listOfCols.set(c, col);
    }
    
    public void setInCol(int r, int c, int value) {
        this.listOfCols.get(c).set(r, value);
    }

    public Set<Integer> getNonzeroIndices(int r) {
        return this.listOfRows.get(r).getNonzeroIndices();
    }
    
    public Set<Integer> getNonzeroColIndices(int c) {
        return this.listOfCols.get(c).getNonzeroIndices();
    }

    public int getNumNonzeroIndices(int r) {
        return this.listOfRows.get(r).getNumNonzeroIndices();
    }

    public Vector getRowCopy(int r) {
        return this.listOfRows.get(r).copy();
    }

    public Vector getRowInstance(int r) {
        return this.listOfRows.get(r);
    }
    
    public Vector getColCopy(int c) {
        return this.listOfCols.get(c).copy();
    }

    public Vector getColInstance(int c) {
        return this.listOfCols.get(c);
    }
    
    public List<Vector> getCols() {
        return this.listOfCols;
    }
    
    public List<Vector> getRows() {
        return this.listOfRows;
    }

    /**
     * Gets the dot product between the values of rows r1 and r2.
     *
     * @param r1 the first row index
     * @param r2 the second row index
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
