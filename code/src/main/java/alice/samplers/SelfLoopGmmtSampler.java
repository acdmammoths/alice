package alice.samplers;

import alice.helpers.SwappableAndNewEdges;
import alice.structures.GmmtMatrix;
import alice.structures.SparseMatrix;
import alice.utils.Timer;
import java.util.Random;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;


public class SelfLoopGmmtSampler implements Sampler {

    public SparseMatrix sample(SparseMatrix inMatrix, int numSwaps, long seed, Timer timer) {
        final long setupTimeStart = System.currentTimeMillis();

        final GmmtMatrix matrix = new GmmtMatrix(inMatrix);
        final Random rnd = new Random(seed);

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);

        SwappableAndNewEdges sne;
        
        for (int i = 0; i < numSwaps; i++) {
            timer.start();
            
            sne = matrix.getRandomSwappables(rnd);
            
            if (matrix.areSwappable(sne)) {
                matrix.transition(sne);
            }
            timer.stop();
        }
        return matrix.getMatrix();
    }
    
    @Override
    public SparseMatrix sample(SparseMatrix inMatrix, long degree, int numSwaps, long seed, Timer timer) {
        final long setupTimeStart = System.currentTimeMillis();

        final GmmtMatrix matrix = new GmmtMatrix(inMatrix);

        final Random rnd = new Random(seed);

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);

        SwappableAndNewEdges sne;
        for (int i = 0; i < numSwaps; i++) {
            timer.start();
            
            sne = matrix.getRandomSwappables(rnd);
            
            if (matrix.areSwappable(sne)) {
                matrix.transition(sne);
            }
            timer.stop();
        }
        return matrix.getMatrix();
    }

    @Override
    public SparseMatrix sample(SparseMatrix inMatrix, 
            int numSwaps, 
            long seed, 
            Timer timer, 
            DescriptiveStatistics stats,
            DescriptiveStatistics catNum) {
        
        final long setupTimeStart = System.currentTimeMillis();

        final GmmtMatrix matrix = new GmmtMatrix(inMatrix);

        final Random rnd = new Random(seed);

        final long setupTime = System.currentTimeMillis() - setupTimeStart;
        timer.save(setupTime);
        
        // starting BJDM vector
        double[] start = matrix.getBJDMVector(true);

        SwappableAndNewEdges sne;
        for (int i = 0; i < numSwaps; i++) {
            timer.start();
            
            sne = matrix.getRandomSwappables(rnd);
            
            if (matrix.areSwappable(sne)) {
                matrix.transition(sne);
            }
            timer.stop();
            
            if (i % 100 == 0) {
                double distance = matrix.getDistanceFrom(start, true);
                stats.addValue(distance);
            } 
        }
        catNum.addValue(matrix.getNumZstructs());
        
        return matrix.getMatrix();
    
    }

}
