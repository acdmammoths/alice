package alice.test;

import alice.structures.GmmtMatrix;
import alice.structures.MultiGraph;
import alice.structures.SparseMatrix;
import alice.utils.CMDLineParser;
import alice.utils.Config;
import alice.utils.Transformer;
import java.io.IOException;

/**
 *
 * @author giulia
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
        
//        for (String dataset : datasets) {
//
//            System.out.println("Finding number of caterpillars for dataset " + dataset + " at " + Config.datasetsDir);
//
//            final Transformer transformer = new Transformer();
//            final SparseMatrix matrix = transformer.createMatrix(Config.datasetsDir + "/" + dataset);
//            final GmmtMatrix gmatrix = new GmmtMatrix(matrix);
//            System.out.println(gmatrix.getNumZstructs());
//
//        }
        
        for (String dataset : seq_datasets) {

            System.out.println("Finding number of caterpillars for dataset " + dataset);

            final Transformer transformer = new Transformer();
            final MultiGraph matrix = transformer.createMultiGraph(Config.datasetsDir + "/sequential/" + dataset);
            System.out.println(matrix.getNumZstructs());

        }
    }
}