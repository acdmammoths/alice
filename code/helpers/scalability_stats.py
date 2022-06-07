#!/usr/bin/env python3

import sys
import json
import pandas as pd


num_trans_title = "Number of transactions"
step_time_title = "Step time (ms)"
algo_title = "Algorithm"

samplers = ["caterpillars.samplers.NaiveBJDMSampler", 
            "caterpillars.samplers.CurveballBJDMSampler",
            "diffusr.samplers.GmmtSampler"]


def get_scalability_df(results_dir, result_file):
    data_dict = get_data_dict(results_dir, result_file)
    return pd.DataFrame(data_dict)


def get_step_times_df(result_path):
    data = []
    with open(result_path) as f:
        result = json.load(f)
        for sampler_name in samplers:
            try:
                data.append(get_sampler_data(sampler_name, result))
            except:
                print(f'{sampler_name} not found in file')
    return pd.DataFrame(data)


def get_data_dict(result_path, result_file):
    data_dict = {num_trans_title: [], step_time_title: [], algo_title: []}
    num_trans = 0
    if 'synthetic' in result_file:
        num_trans = (
            int(result_file.split("-")[1]) * 1000
        )  # number of transactions is saved in 000s
    with open(result_path) as f:
        result = json.load(f)
        for sampler_name in samplers:
            try:
                add_sampler_data(data_dict, sampler_name, num_trans, result)
            except:
                print(f'{sampler_name} not found in file')
    return data_dict


def add_sampler_data(data_dict, sampler_name, num_trans, result):
    algo_name = sampler_name
    step_times = get_sampler_step_times(sampler_name, result)
    for step_time in step_times:
        data_dict[algo_title].append(algo_name)
        data_dict[num_trans_title].append(num_trans)
        data_dict[step_time_title].append(step_time)


def get_sampler_data(sampler_name, result):
    sampler_data = list([sampler_name])
    step_times = get_sampler_step_times(sampler_name, result)
    sampler_data.extend(step_times)
    return sampler_data


def get_sampler_step_times(sampler_name, result):
    step_times = []
    sampler_stats = result["runtimeStats"][sampler_name]
    step_times.append(sampler_stats["minStepTime"])
    step_times.append(sampler_stats["c10StepTime"])
    step_times.append(sampler_stats["q1StepTime"])
    step_times.append(sampler_stats["medianStepTime"])
    step_times.append(sampler_stats["q3StepTime"])
    step_times.append(sampler_stats["c90StepTime"])
    step_times.append(sampler_stats["maxStepTime"])
    return step_times
