#!/bin/bash

gen_synthetic_dataset() {
    num_trans=$1 # in 000s
    avg_trans_len=$2
    num_items=$3 # in 000s
    output_file="synthetic-$num_trans-$avg_trans_len-$num_items"
    datasetgenerator/gen lit \
        -ntrans $num_trans -tlen $avg_trans_len -nitems $num_items \
        -randseed -2 -ascii \
        -fname datasetgenerator/datasets/$output_file
    ./ibm_to_spmf.py datasetgenerator/datasets/$output_file.data \
        ../datasets/$output_file.txt
}

avg_trans_len=25
num_items=0.1
for ((num_trans = 5; num_trans <= 20; num_trans += 5)); do
    gen_synthetic_dataset $num_trans $avg_trans_len $num_items
done
