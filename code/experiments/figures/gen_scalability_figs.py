#!/usr/bin/env python3

import os
import sys
import json

import numpy as np
from matplotlib import pyplot as plt
import seaborn as sns
import pandas as pd


num_trans_title = "Number of transactions"
step_time_title = "Step time (ms)"
algo_title = "Algorithm"
sampler_to_algo_name = {
    "NaiveSampler": "DiFfuSR-N",
    "RefinedSampler": "DiFuSR-R",
    "GmmtSampler": "GMMT",
}


def gen_scalability_figs(results_dir):
    data_dict = get_data_dict(results_dir)
    df = pd.DataFrame(data_dict)
    sns.set_theme(
        style="whitegrid",
        rc={"axes.labelsize": 20, "xtick.labelsize": 15, "ytick.labelsize": 15},
    )
    sns.boxplot(
        x=num_trans_title, y=step_time_title, hue=algo_title, data=df, whis=np.inf
    )
    plt.ylim(top=100)
    plt.legend(ncol=3, fontsize=14)
    plt.tight_layout()
    plt.savefig("images/scalability.pdf")


def get_data_dict(results_dir):
    data_dict = {num_trans_title: [], step_time_title: [], algo_title: []}
    result_files = os.listdir(results_dir)
    for result_file in result_files:
        result_path = os.path.join(results_dir, result_file)
        num_trans = (
            int(result_file.split("-")[1]) * 1000
        )  # number of transactions is saved in 000s
        with open(result_path) as f:
            result = json.load(f)
            for sampler_name in sampler_to_algo_name.keys():
                add_sampler_data(data_dict, sampler_name, num_trans, result)
    return data_dict


def add_sampler_data(data_dict, sampler_name, num_trans, result):
    algo_name = sampler_to_algo_name[sampler_name]
    step_times = get_sampler_step_times(sampler_name, result)
    for step_time in step_times:
        data_dict[algo_title].append(algo_name)
        data_dict[num_trans_title].append(num_trans)
        data_dict[step_time_title].append(step_time)


def get_sampler_step_times(sampler_name, result):
    step_times = []
    sampler_stats = result["runtimeStats"][sampler_name]
    step_times.append(sampler_stats["minStepTime"])
    step_times.append(sampler_stats["q1StepTime"])
    step_times.append(sampler_stats["medianStepTime"])
    step_times.append(sampler_stats["q3StepTime"])
    step_times.append(sampler_stats["maxStepTime"])
    return step_times


if __name__ == "__main__":
    gen_scalability_figs(sys.argv[1])
