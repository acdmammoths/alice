#!/usr/bin/env python3

import os
import sys
import json

import pandas as pd
from matplotlib import pyplot as plt
import seaborn as sns


naive_sampler_key = "NaiveSampler"
refined_sampler_key = "RefinedSampler"
gmmt_sampler_key = "GmmtSampler"
sampler_to_algo_name = {
    naive_sampler_key: "DiFfuSR-N",
    refined_sampler_key: "DiFuSR-R",
    gmmt_sampler_key: "GMMT",
}
swap_num_mult_title = "Swap number multiplier k"
arsd_title = "Avg. relative support diff."
algo_title = "Algorithm"


def gen_convergence_fig(result_path):
    with open(result_path) as f:
        result = json.load(f)

        args_str = get_args_str(result)

        convergence_stats = result["convergenceStats"]

        naive_stats = convergence_stats[naive_sampler_key]
        refined_stats = convergence_stats[refined_sampler_key]
        gmmt_stats = convergence_stats[gmmt_sampler_key]

        num_swaps_factors = [stats["numSwapsFactor"] for stats in gmmt_stats]

        sampler_to_arfds = get_sampler_to_arfds(naive_stats, refined_stats, gmmt_stats)

        data_dict = get_data_dict(sampler_to_arfds, num_swaps_factors)
        df = pd.DataFrame(data_dict)
        sns.set_theme(
            style="whitegrid",
            rc={"axes.labelsize": 20, "xtick.labelsize": 15, "ytick.labelsize": 15},
        )
        sns.lineplot(
            x=swap_num_mult_title,
            y=arsd_title,
            hue=algo_title,
            style=algo_title,
            markers=True,
            linewidth=2.5,
            data=df,
        )
        plt.gca().xaxis.grid(False)
        plt.legend(fontsize=14)
        plt.tight_layout()
        plt.savefig(f"images/convergence-{args_str}.pdf")


def get_args_str(result):
    args = result["runInfo"]["args"]
    dataset_path = args["datasetPath"]
    max_num_swaps_factor = args["maxNumSwapsFactor"]
    min_freq = args["minFreq"]
    seed = args["seed"]
    dataset_name, _ = os.path.splitext(os.path.basename(dataset_path))
    return "-".join([dataset_name, str(max_num_swaps_factor), str(min_freq), str(seed)])


def get_sampler_to_arfds(naive_stats, refined_stats, gmmt_stats):
    return {
        naive_sampler_key: get_sampler_arfds(naive_stats),
        refined_sampler_key: get_sampler_arfds(refined_stats),
        gmmt_sampler_key: get_sampler_arfds(gmmt_stats),
    }


def get_sampler_arfds(sampler_stats):
    return [stats["avgRelFreqDiff"] for stats in sampler_stats]


def get_data_dict(sampler_to_arfds, num_swap_factors):
    data_dict = {
        swap_num_mult_title: num_swap_factors * 3,
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
