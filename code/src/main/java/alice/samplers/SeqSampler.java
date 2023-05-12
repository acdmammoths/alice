package alice.samplers;


import alice.structures.MultiGraph;
import alice.utils.Timer;

/**
 * An interface for different dataset samplers to implement.
 */
public interface SeqSampler {

    MultiGraph sample(MultiGraph inMatrix, int numSwaps, long seed, Timer timer);
    
}
