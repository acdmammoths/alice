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
import java.util.Map;
import java.util.Objects;

/** A class to represent a transactional dataset. */
class Dataset {
  /** A map from a transaction to the number of times it appears in the dataset. */
  private final Map<Vector, Integer> transactionCounts;

  Dataset(SparseMatrix matrix) {
    this.transactionCounts = new HashMap<>();
    for (int r = 0; r < matrix.getNumRows(); r++) {
      final Vector transaction = matrix.getRowCopy(r);
      final int count = this.transactionCounts.getOrDefault(transaction, 0) + 1;
      this.transactionCounts.put(transaction, count);
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
      final Dataset otherDataset = (Dataset) o;
      return Objects.equals(this.transactionCounts, otherDataset.transactionCounts);
    }
  }

  @Override
  public int hashCode() {
    return this.transactionCounts.hashCode();
  }

  @Override
  public String toString() {
    return this.transactionCounts.toString();
  }
}
