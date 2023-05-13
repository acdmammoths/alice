package alice.test;

import alice.structures.GmmtMatrix;
import alice.structures.MultiGraph;
import alice.structures.SparseMatrix;
import alice.utils.CMDLineParser;
import alice.utils.Config;
import alice.utils.Transformer;
import java.io.IOException;

/*
 * Copyright (C) 2023 Giulia Preti
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

/**
 * Class for finding the number of paths of length 3 in the graph representation of a dataset.
 */
public class ComputeNumberOfCaterpillars {
    
    public static void main(String[] args) throws IOException {
        CMDLineParser.parse(args);

        String[] datasets = new String[]{
            "edit-iewikibooks.txt", 
            "kosarak_3000.txt", 
            "chess.txt", 
            "foodmart.txt",
            "dbpedia-occupation.txt",
            "BMS1.txt",
            "BMS2.txt", 
            "retail.txt"};
        
        String[] seq_datasets = new String[]{
            "SIGN.txt", 
            "FIFA.txt", 
            "BIKE.txt", 
            "BIBLE.txt",
            "LEVIATHAN.txt",
            "BMS1Seq.txt"};
        
        for (String dataset : datasets) {

            System.out.println("Finding number of caterpillars for dataset " + dataset + " at " + Config.datasetsDir);

            final Transformer transformer = new Transformer();
            final SparseMatrix matrix = transformer.createMatrix(Config.datasetsDir + "/" + dataset);
            final GmmtMatrix gmatrix = new GmmtMatrix(matrix);
            System.out.println(gmatrix.getNumZstructs());

        }
        
        for (String dataset : seq_datasets) {

            System.out.println("Finding number of caterpillars for dataset " + dataset);

            final Transformer transformer = new Transformer();
            final MultiGraph matrix = transformer.createMultiGraph(Config.datasetsDir + "/sequential/" + dataset);
            System.out.println(matrix.getNumZstructs());

        }
    }
}