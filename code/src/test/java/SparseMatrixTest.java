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
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;

public class SparseMatrixTest {
  private final Random rnd = new Random();

  @Test
  public void dimensions() {
    final SparseMatrix m = new SparseMatrix(100, 10);

    Assert.assertEquals(100, m.getNumRows());
    Assert.assertEquals(10, m.getNumCols());
  }

  @Test
  public void twoDimArrayInit() {
    final int[][] a = {
      {1, 0, 1, 1},
      {1, 1, 0, 0},
      {0, 0, 1, 0}
    };

    final SparseMatrix n = new SparseMatrix(a.length, a[0].length);
    n.set(0, 0, 1);
    n.set(0, 1, 0);
    n.set(0, 2, 1);
    n.set(0, 3, 1);
    n.set(1, 0, 1);
    n.set(1, 1, 1);
    n.set(1, 2, 0);
    n.set(1, 3, 0);
    n.set(2, 0, 0);
    n.set(2, 1, 0);
    n.set(2, 2, 1);
    n.set(2, 3, 0);

    final SparseMatrix m = new SparseMatrix(a);

    Assert.assertEquals(n, m);
  }

  @Test
  public void emptyGet() {
    final SparseMatrix m = new SparseMatrix(100, 10);
    for (int r = 0; r < m.getNumRows(); r++) {
      for (int c = 0; c < m.getNumCols(); c++) {
        Assert.assertEquals(0, m.get(r, c));
      }
    }
  }

  @Test
  public void getAndSet() {
    final SparseMatrix m = new SparseMatrix(100, 10);

    for (int i = 0; i < 10; i++) {
      final int rndR = this.rnd.nextInt(m.getNumRows());
      final int rndC = this.rnd.nextInt(m.getNumCols());
      final int rndVal = this.rnd.nextInt(2);
      m.set(rndR, rndC, rndVal);
      Assert.assertEquals(rndVal, m.get(rndR, rndC));
    }
  }

  @Test
  public void setZero() {
    final SparseMatrix m = new SparseMatrix(100, 10);

    for (int r = 0; r < m.getNumRows(); r++) {
      for (int c = 0; c < m.getNumCols(); c++) {
        m.set(r, c, 0);
        Assert.assertEquals(0, m.get(r, c));
      }
    }

    Assert.assertEquals(new SparseMatrix(100, 10), m);
  }

  @Test
  public void multipleGetAndSet() {
    final SparseMatrix m = new SparseMatrix(100, 10);

    m.set(0, 0, 1);
    m.set(0, 0, 0);
    m.set(0, 0, 1);

    m.get(0, 0);
    m.get(0, 1);

    Assert.assertEquals(1, m.get(0, 0));
  }

  @Test
  public void getRow() {
    final int[][] a = {
      {1, 1, 1, 1},
      {1, 0, 1, 1},
      {1, 1, 0, 1}
    };
    final SparseMatrix m = new SparseMatrix(a);

    for (int r = 0; r < a.length; r++) {
      Assert.assertEquals(new Vector(a[r]), m.getRowCopy(r));
    }
  }

  @Test
  public void getRowDotProd() {
    final SparseMatrix m =
        new SparseMatrix(
            new int[][] {
              {1, 0, 0, 1},
              {1, 0, 1, 1},
              {1, 1, 1, 0}
            });

    // 0, 1: 1*1 + 0*0 + 0*1 + 1*1 = 2
    Assert.assertEquals(2, m.getRowDotProd(0, 1));
    // 0, 2: 1*1 + 0*1 + 0*1 + 1*0 = 1
    Assert.assertEquals(1, m.getRowDotProd(0, 2));
    // 1, 2: 1*1 + 0*1 + 1*1 + 1*0 = 2
    Assert.assertEquals(2, m.getRowDotProd(1, 2));
  }

  @Test
  public void nonZeroEntries() {
    final SparseMatrix m =
        new SparseMatrix(
            new int[][] {
              {1, 0, 0, 1},
              {1, 0, 1, 1},
              {1, 1, 1, 0}
            });
    for (int r = 0; r < m.getNumRows(); r++) {
      final Vector row = m.getRowCopy(r);
      Assert.assertEquals(row.getNumNonzeroIndices(), m.getNumNonzeroIndices(r));
      Assert.assertEquals(row.getNonzeroIndices(), m.getNonzeroIndices(r));
    }
  }

  @Test
  public void onlyZeroOrOnes() {
    final SparseMatrix m =
        new SparseMatrix(
            new int[][] {
              {1, 0, 0},
              {1, 0, 1},
              {1, 1, 1}
            });

    final SparseMatrix n = new SparseMatrix(m.getNumRows(), m.getNumCols());
    n.set(0, 0, 5);
    n.set(1, 0, 3);
    n.set(1, 2, 1);
    n.set(2, 0, 8);
    n.set(2, 1, 4);
    n.set(2, 2, 2);

    Assert.assertEquals(m, n);
  }

  @Test
  public void getNumEntriesNeq() {
    final SparseMatrix m =
        new SparseMatrix(
            new int[][] {
              {1, 0, 0, 1},
              {1, 0, 1, 1},
              {1, 1, 1, 0}
            });

    Assert.assertEquals(1, m.getNumEntriesNeq(0, 1));
    Assert.assertEquals(1, m.getNumEntriesNeq(1, 0));

    Assert.assertEquals(3, m.getNumEntriesNeq(0, 2));
    Assert.assertEquals(3, m.getNumEntriesNeq(2, 0));

    Assert.assertEquals(2, m.getNumEntriesNeq(1, 2));
    Assert.assertEquals(2, m.getNumEntriesNeq(2, 1));
  }

  @Test
  public void entriesEqual() {
    final SparseMatrix m =
        new SparseMatrix(
            new int[][] {
              {1, 1, 0, 1},
              {1, 0, 1, 1},
              {0, 1, 1, 0}
            });

    Assert.assertTrue(m.entriesEqual(0, 1, 1, 2));
    Assert.assertTrue(m.entriesEqual(0, 1, 2, 1));
    Assert.assertTrue(m.entriesEqual(1, 0, 1, 2));
    Assert.assertTrue(m.entriesEqual(1, 0, 2, 1));

    Assert.assertFalse(m.entriesEqual(0, 2, 0, 2));
    Assert.assertFalse(m.entriesEqual(0, 2, 2, 0));
    Assert.assertFalse(m.entriesEqual(2, 0, 0, 2));
    Assert.assertFalse(m.entriesEqual(2, 0, 2, 0));

    Assert.assertFalse(m.entriesEqual(1, 2, 1, 3));
    Assert.assertFalse(m.entriesEqual(1, 2, 3, 1));
    Assert.assertFalse(m.entriesEqual(2, 1, 1, 3));
    Assert.assertFalse(m.entriesEqual(2, 1, 3, 1));
  }
}
