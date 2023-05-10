package alice.test;

import alice.structures.GmmtMatrix;
import alice.structures.SparseMatrix;
import alice.utils.CMDLineParser;
import alice.utils.Config;
import alice.utils.Transformer;

/**
 *
 * @author giulia
 */
public class ComputeNumberOfCaterpillars {
    
    public static void main(String[] args) {
        CMDLineParser.parse(args);

        String[] datasets = new String[]{"edit-iewikibooks.txt", 
            "kosarak_3000.txt", 
            "chess.txt", 
            "foodmart.txt",
            "dbpedia-occupation.txt",
            "BMS1.txt",
            "BMS2.txt", 
            "retail.txt"};
        
        for (String dataset : datasets) {

            System.out.println("Finding number of caterpillars for dataset " + dataset + " at " + Config.datasetsDir);

            final Transformer transformer = new Transformer();
            final SparseMatrix matrix = transformer.createMatrix(Config.datasetsDir + "/" + dataset);
            final GmmtMatrix gmatrix = new GmmtMatrix(matrix);
            System.out.println(gmatrix.getNumZstructs());

        }
    }
}