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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class VectorTest {
  private final Random rnd = new Random();

  @Test
  public void arrayInit() {
    final int[] a = {1, 0, 1, 0};
    final Vector v = new Vector();
    for (int i = 0; i < a.length; i++) {
      v.set(i, a[i]);
    }

    final Vector u = new Vector(a);

    Assert.assertEquals(v, u);
  }

  @Test
  public void emptyGet() {
    final Vector v = new Vector();

    for (int i = 0; i < 10; i++) {
      Assert.assertEquals(0, v.get(i));
    }
  }

  @Test
  public void getAndSet() {
    final Vector v = new Vector();

    for (int i = 0; i < 10; i++) {
      final int rndIdx = this.rnd.nextInt(100);
      final int rndVal = this.rnd.nextInt(2);
      v.set(rndIdx, rndVal);
      Assert.assertEquals(rndVal, v.get(rndIdx));
    }
  }

  @Test
  public void setZero() {
    final Vector v = new Vector();

    for (int i = 0; i < 10; i++) {
      v.set(i, 0);
      Assert.assertEquals(0, v.get(i));
    }
    Assert.assertEquals(new Vector(), v);
  }

  @Test
  public void multipleGetAndSet() {
    final Vector v = new Vector();

    v.set(1, 0);
    v.set(1, 1);

    v.get(1);
    v.get(2);

    Assert.assertEquals(1, v.get(1));
  }

  @Test
  public void copy() {
    final Vector v = new Vector();
    for (int i = 0; i < 10; i++) {
      final int rndIdx = this.rnd.nextInt(100);
      final int rndVal = this.rnd.nextInt(2);
      v.set(rndIdx, rndVal);
    }

    final Vector u = v.copy();

    Assert.assertEquals(v, u);
  }

  @Test
  public void nonzeroEntries() {
    final Vector v = new Vector();
    v.set(0, 1);
    v.set(0, 0);
    v.set(1, 0);
    v.set(1, 1);
    v.set(2, 1);
    v.set(3, 1);
    v.set(3, 1);

    final Set<Integer> expectedNonzeroIndices = new HashSet<>();

    expectedNonzeroIndices.add(1);
    expectedNonzeroIndices.add(2);
    expectedNonzeroIndices.add(3);

    Assert.assertEquals(expectedNonzeroIndices.size(), v.getNumNonzeroIndices());
    Assert.assertEquals(expectedNonzeroIndices, v.getNonzeroIndices());
  }

  @Test
  public void hash() {
    final Vector v = new Vector();
    v.set(0, 0);
    v.set(8, 1);

    final Vector u = new Vector();
    u.set(8, 1);
    u.set(0, 0);

    final HashMap<Vector, Integer> m = new HashMap<>();
    m.put(v, 1);

    Assert.assertEquals(v.hashCode(), u.hashCode());
    Assert.assertSame(1, m.get(u));
  }

  @Test
  public void onlyZeroOrOnes() {
    final Vector v = new Vector(new int[] {0, 1, 1});

    final Vector u = new Vector();
    u.set(0, 2);
    u.set(0, 0);
    u.set(1, 5);
    u.set(2, 4);

    Assert.assertEquals(v, u);
  }
}
