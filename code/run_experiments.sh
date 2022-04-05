#!/bin/bash

distortion_type="distortion"
runtime_type="runtime"
scalability_type="scalability"
convergence_type="convergence"
num_fis_type="numFreqItemsets"
sig_fis_type="sigFreqItemsets"
all_type="all"

confs_dir=experiments/confs
figures_dir=experiments/figures

usage() {
    echo "Usage ./$(basename $0) [-ht]"
    echo
    echo "$(basename $0) runs a specified type of experiment for DiFfuSR on"
    echo "all configuration files in $confs_dir/<experiment_type> or all"
    echo "experiments in $confs_dir"
    echo
    echo "-h                  display this help menu"
    echo "-t experiment_type  run the type of experiment"
    echo "                    types: $distortion_type, $runtime_type,"
    echo "                           $scalability_type, $convergence_type,"
    echo "                           $num_fis_type, $sig_fis_type,"
    echo "                           $all_type"
    exit
}

while getopts :ht: options; do
    case $options in
    h) usage ;;
    t) experiment_type=$OPTARG ;;
    :)
        echo "Argument needed for option -$OPTARG"
        usage
        ;;
    *)
        echo "Invalid option -$OPTARG"
        usage
        ;;
    esac
done

run() {
    experiment_class=$1
    experiment_confs_dir=$2
    for conf_file in $confs_dir/$experiment_confs_dir/*.json; do
        java -cp target/DiFfuSR-1.0-SNAPSHOT-jar-with-dependencies.jar \
            $experiment_class $conf_file
    done
}

distortion() {
    dir=$distortion_type
    run "DistortionExperiment" $dir
}

runtime() {
    dir=$runtime_type
    run "RuntimeExperiment" $dir
}

scalability() {
    dir=$scalability_type
    run "RuntimeExperiment" $dir
    cd $figures_dir
    ./gen_scalability_figs.py ../results/scalability
    cd ../..
}

convergence() {
    dir=$convergence_type
    run "ConvergenceExperiment" $dir
    cd $figures_dir
    for result in ../results/convergence/*.json; do
        ./gen_convergence_fig.py $result
    done
    cd ../..
}

num_fis() {
    dir=$num_fis_type
    run "NumFreqItemsetsExperiment" $dir
}

sig_fis() {
    dir=$sig_fis_type
    run "SigFreqItemsetsExperiment" $dir
}

all() {
    distortion
    runtime
    scalability
    convergence
    num_fis
    sig_fis
}

case $experiment_type in
$distortion_type) distortion ;;
$runtime_type) runtime ;;
$scalability_type) scalability ;;
$convergence_type) convergence ;;
$num_fis_type) num_fis ;;
$sig_fis_type) sig_fis ;;
$all_type) all ;;
*)
    echo "Invalid experiment type: $experiment_type"
    usage
    ;;
esac
