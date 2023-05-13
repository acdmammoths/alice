#!/usr/bin/env bash

# Loading configurations for experiments
echo '>> Loading config file config_seq.cfg'
source config_seq.cfg

unset datasets
declare -A datasets
datasets[$fifa]=$fifa_defaults
datasets[$leviathan]=$leviathan_defaults
datasets[$kosarak]=$kosarak_defaults
datasets[$bms1]=$bms1_defaults
datasets[$bible]=$bible_defaults
datasets[$bike]=$bike_defaults
datasets[$sign]=$sign_defaults

unset flags
declare -A flags
flags[$fifa]=$fifa_flags
flags[$leviathan]=$leviathan_flags
flags[$kosarak]=$kosarak_flags
flags[$bms1]=$bms1_flags
flags[$bible]=$bible_flags
flags[$bike]=$bike_flags
flags[$sign]=$sign_flags

echo -e '\n\n>> Creating directories ...'
mkdir -p $resultsDir

for dataset in ${!datasets[@]}
do
	datasetPath=${datasetsDir}/${dataset}.txt
	default=${datasets[${dataset}]}
	flag=${flags[${dataset}]}
	defaults=(`echo $default|tr "," "\n"`)
	experiments=(`echo $flag|tr "," "\n"`)

	echo ">> Processing dataset ${dataset} with default values (${defaults[@]})"
	echo ">> Experiment flags ${experiments[@]}"


	if [[ ${experiments[0]} -eq "1" ]]; then
		echo '-----------------------'
		echo '      Convergence      '
		echo '-----------------------'

		OUTPUT="$resultsDir/convergence/"
		mkdir -p $OUTPUT

		echo "Running command ..."
		echo "$JVM $CONV_jar datasetPath=$datasetPath resultsDir=$OUTPUT seed=$seed maxNumSwapsFactor=$maxNumSwapsFactor minFreq=${defaults[2]}"
		echo "---- `date`"
		$JVM $CONV_jar datasetPath=$datasetPath resultsDir=$OUTPUT seed=$seed maxNumSwapsFactor=$maxNumSwapsFactor minFreq=${defaults[2]}
	fi

	if [[ ${experiments[1]} -eq "1" ]]; then
		echo '-----------------------'
		echo '      Scalability      '
		echo '-----------------------'

		OUTPUT="$resultsDir/scalability/"
		mkdir -p $OUTPUT

		echo "Running command ..."
		echo "$JVM $SCALA_jar datasetPath=$datasetPath resultsDir=$OUTPUT seed=$seed numSwaps=$numSwaps"
		echo "---- `date`"
		$JVM $SCALA_jar datasetPath=$datasetPath resultsDir=$OUTPUT seed=$seed numSwaps=$numSwaps
	fi

	if [[ ${experiments[2]} -eq "1" ]]; then
		echo '---------------------------------------'
		echo '      Number of Frequent Itemsets      '
		echo '---------------------------------------'

		OUTPUT="$resultsDir/numFreqItemsets"
		mkdir -p $OUTPUT
		OUTPUT2="${OUTPUT}/${dataset}/"
		mkdir -p $OUTPUT2

		echo "Running command ..."
		echo "$JVM $FREQ_jar datasetPath=$datasetPath resultsDir=$OUTPUT2 seed=$seed numThreads=$numThreads numSwaps=${defaults[0]} numSamples=${defaults[1]} minFreq=${defaults[2]} sampleAndMine=true"
		echo "---- `date`"
		$JVM $FREQ_jar datasetPath=$datasetPath resultsDir=$OUTPUT2 seed=$seed numThreads=$numThreads numSwaps=${defaults[0]} numSamples=${defaults[1]} minFreq=${defaults[2]} sampleAndMine=true
	fi

done
echo 'Terminated.'
