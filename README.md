# Overview
This package includes ALICE, a suite of two Markov-Chain Monte-Carlo algorithms for sampling datasets from our novel null model, based on a carefully defined set of states and efficient operations to move between them.
This null model preserves the Bipartite Joint Degree Matrix of the bipartite graph corresponding to the dataset, which ensures that the number of caterpillars, i.e., paths of length three, is preserved, in addition to the item supports and the transaction lengths.

ALICE-A is based on Restricted Swap Operations (RSOs) on biadjacency matrices, which preserve the BJDM. 
ALICE-B adapts the CURVEBALL approach [1] to RSOs, to essentially perform multiple RSOs at every step, thus leading to faster mixing.

The package includes a Jupyter Notebook (Results.ipynb) with the complete experimental evaluation of ALICE. A selected subset of these results have been included in our conference paper, accepted for publication at the ICDM'22 conference.

# Content of Code
	datasets/ .....
	helpers/ ......
	notebooks/ ....
	output/ .......
	scripts/ ......
	src/ ..........

The folder *helpers* includes the scripts used to process the output of ALICE (folder *output*) and produce the statistics presented in the charts in the Jupyter Notebook *Results.ipynb* in the folder *notebooks*.
This folder includes also the Jupyter Notebook used to generate some of the dataset input files (DB_Generation.ipynb).

# Requirements
To run our code (the source files are in folder *src*):

	Java JRE v1.8.0

To check the results of our experimental evaluation:

	Jupyter Notebook

# Input Format
The input file must be a space separated list of integers, where each integer represents an item and each line represents a transaction in the database.
The script *run.sh* assumes that the file extension is *.txt*. The folder *datasets* includes all the datasets used in our experimental evaluation of ALICE.

# Usage
You can use ALICE by running the script *run.sh* included in this package (folder *scripts*).
The value of each parameter used by ALICE must be set in the configuration file *config.cfg* (folder *scripts*).

## General Settings

- datasetsDir: path to the folder containing the dataset files.
- resultsDir: path to the folder to store the results.
- seed: seed for reproducibility.
- numThreads: number of threads.
- maxNumSwapsFactor: integer used in the *Convergence* experiment.
- numSwaps: number of iterations (used in the *Scalability* experiment).
- cleanup: whether to delete the samples and frequent itemsets found during the experiments.
- fwer: family wise error rate (used in the *SigFreqItemsets* experiment).
- numWySamples: number of samples to compute the adjusted critical value (used in the *SigFreqItemsets* experiment).
- numEstSamples: samples to compute the p-value (used in the *SigFreqItemsets* experiment).

## Dataset-related Settings

- Dataset names: names of the dataset files (without file extension).
- Default values: comma-separated list of default values for each dataset, i.e., number of swaps/iterations to perform before returning the random sample, number of random samples to generate, and minimum frequency for an itemset to be frequent (value used in the *NumFreqItemset* experiment).
- Experimental flags: test to perform among (1) significant itemset mining (*SigFreqItemsets.java*, Table III paper), (2) convergence (*Convergence.java*, Figure 1 paper), (3) scalability (*Scalability.java*, Table II paper), and (4) number of frequent itemsets by size (*NumFreqItemsets.java*, Figure 3 paper).
Then, the arrays that store the names, the default values, and the experimental flags of each dataset to test must be declared at the beginning of the script *run.sh*.

# License
This package is released under the GNU General Public License.

# References
[1] N. D. Verhelst, “An efficient MCMC algorithm to sample binary matrices with fixed marginals,” Psychometrika, vol. 73, no. 4, pp. 705–728, 2008.
