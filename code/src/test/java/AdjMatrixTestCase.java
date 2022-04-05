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
class AdjMatrixTestCase {
  final Matrix matrix;
  final Matrix adjMatrix;
  final Edge swappableEdge1;
  final Edge swappableEdge2;

  AdjMatrixTestCase(Matrix matrix, Matrix adjMatrix, Edge swappableEdge1, Edge swappableEdge2) {
    this.matrix = matrix;
    this.adjMatrix = adjMatrix;
    this.swappableEdge1 = swappableEdge1;
    this.swappableEdge2 = swappableEdge2;
  }
}
