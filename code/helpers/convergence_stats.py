#!/usr/bin/env python3

import os
import sys
import json
import pandas as pd


naive_sampler_key = "caterpillars.samplers.NaiveBJDMSampler"
refined_sampler_key = "caterpillars.samplers.CurveballBJDMSampler"
gmmt_sampler_key = "diffusr.samplers.GmmtSampler"
sampler_to_algo_name = {
    naive_sampler_key: "Naive",
    refined_sampler_key: "Curveball",
    gmmt_sampler_key: "GMMT",
}

swap_num_mult_title = "Num Swap Multiplier"
arsd_title = "Avg. Rel. Support Diff."
algo_title = "Algorithm"


def get_convergence_df(result_path):
    with open(result_path) as f:
        result = json.load(f)

        convergence_stats = result["convergenceStats"]
        sampler_to_arfds = dict()
        try:
            naive_stats = convergence_stats[naive_sampler_key]
            sampler_to_arfds[naive_sampler_key] = get_sampler_arfds(naive_stats)
        except:
            print(f'{naive_sampler_key} not found in file')
        try:
            ref_stats = convergence_stats[refined_sampler_key]
            sampler_to_arfds[refined_sampler_key] = get_sampler_arfds(ref_stats)
        except:
            print(f'{refined_sampler_key} not found in file') 
        try:
            gmmt_stats = convergence_stats[gmmt_sampler_key]
            sampler_to_arfds[gmmt_sampler_key] = get_sampler_arfds(gmmt_stats)
        except:
            print(f'{gmmt_sampler_key} not found in file') 

        num_swaps_factors = [stats["numSwapsFactor"] for stats in naive_stats]

        data_dict = get_data_dict(sampler_to_arfds, num_swaps_factors)
        return pd.DataFrame(data_dict)


def get_args_str(result):
    args = result["runInfo"]["args"]
    dataset_path = args["datasetPath"]
    max_num_swaps_factor = args["maxNumSwapsFactor"]
    min_freq = args["minFreq"]
    seed = args["seed"]
    dataset_name, _ = os.path.splitext(os.path.basename(dataset_path))
    return "-".join([dataset_name, str(max_num_swaps_factor), str(min_freq), str(seed)])


def get_sampler_arfds(sampler_stats):
    return [stats["avgRelFreqDiff"] for stats in sampler_stats]


def get_data_dict(sampler_to_arfds, num_swap_factors):
    data_dict = {
        swap_num_mult_title: num_swap_factors * len(sampler_to_arfds.keys()),
        arsd_title: [],
        algo_title: [],
    }
    for sampler, arfds in sampler_to_arfds.items():
        for arfd in arfds:
            data_dict[algo_title].append(sampler_to_algo_name[sampler])
            data_dict[arsd_title].append(arfd)
    return data_dict


if __name__ == "__main__":
    gen_convergence_fig(sys.argv[1])
