#!/usr/bin/env python3

import sys
import json


setup_time = "setupTime"
median_step_time = "medianStepTime"


def get_sampler_stats(runtime_result_path):
    with open(runtime_result_path) as f:
        result = json.load(f)
        runtime_stats = result["runtimeStats"]
        gmmt_stats = runtime_stats["GmmtSampler"]
        naive_stats = runtime_stats["NaiveSampler"]
        refined_stats = runtime_stats["RefinedSampler"]
        return gmmt_stats, naive_stats, refined_stats


def get_setup_times(runtime_result_path):
    gmmt_stats, naive_stats, refined_stats = get_sampler_stats(runtime_result_path)
    gmmt_setup_time = gmmt_stats[setup_time]
    naive_setup_time = naive_stats[setup_time]
    refined_setup_time = refined_stats[setup_time]
    return gmmt_setup_time, naive_setup_time, refined_setup_time


def get_med_step_times(runtime_result_path):
    gmmt_stats, naive_stats, refined_stats = get_sampler_stats(runtime_result_path)
    gmmt_med_step_time = gmmt_stats[median_step_time]
    naive_med_step_time = naive_stats[median_step_time]
    refined_med_step_time = refined_stats[median_step_time]
    return gmmt_med_step_time, naive_med_step_time, refined_med_step_time


def to_hours(time_ms):
    time_s = time_ms * 1e-3
    time_h = time_s / 60 / 60
    return time_h


def convergence_time(
    runtime_result_path, num_ones, max_num_swaps_factor, num_of_num_swaps_factors
):
    """
    Prints an estimate of the total setup time and swapping time for the
    convergence experiment.
    """

    gmmt_setup_time, naive_setup_time, refined_setup_time = get_setup_times(
        runtime_result_path
    )

    gmmt_med_step_time, naive_med_step_time, refined_med_step_time = get_med_step_times(
        runtime_result_path
    )

    sum_setup_times = gmmt_setup_time + naive_setup_time + refined_setup_time
    sum_med_step_times = (
        gmmt_med_step_time + naive_med_step_time + refined_med_step_time
    )

    # we execute the setup portion of the sampler method for every num_swaps_factor
    total_setup_time = num_of_num_swaps_factors * sum_setup_times
    total_swap_time = max_num_swaps_factor * num_ones * sum_med_step_times

    total_setup_time_h = to_hours(total_setup_time)
    total_swap_time_h = to_hours(total_swap_time)

    print("convergence experiment time:")
    print(f"total setup time (h): {total_setup_time_h}")
    print(f"total swap time (h): {total_swap_time_h}")
    print(f"total time (h): {total_setup_time_h + total_swap_time_h}")


def diffusr_time(
    runtime_result_path, num_swaps, num_est_samples, num_wy_samples, num_threads
):
    """
    Prints an estimate of the total setup time and swapping time to run the
    sampling portion of the diffusr experiment.
    """
    gmmt_setup_time, naive_setup_time, refined_setup_time = get_setup_times(
        runtime_result_path
    )

    gmmt_med_step_time, naive_med_step_time, refined_med_step_time = get_med_step_times(
        runtime_result_path
    )

    sum_setup_times = gmmt_setup_time + naive_setup_time + refined_setup_time
    sum_med_step_times = (
        gmmt_med_step_time + naive_med_step_time + refined_med_step_time
    )

    total_num_samples = num_est_samples + num_wy_samples

    # num_threads samples can be created in parallel
    total_setup_time = (total_num_samples / num_threads) * sum_setup_times
    total_swap_time = (total_num_samples / num_threads) * num_swaps * sum_med_step_times

    total_setup_time_h = to_hours(total_setup_time)
    total_swap_time_h = to_hours(total_swap_time)

    print("diffusr experiment time:")
    print(f"total setup time (h): {total_setup_time_h}")
    print(f"total swap time (h): {total_swap_time_h}")
    print(f"total time (h): {total_setup_time_h + total_swap_time_h}")


if __name__ == "__main__":
    if sys.argv[1] == "-c":
        convergence_time(
            sys.argv[2], int(sys.argv[3]), int(sys.argv[4]), int(sys.argv[5])
        )
    elif sys.argv[1] == "-d":
        diffusr_time(
            sys.argv[2],
            int(sys.argv[3]),
            int(sys.argv[4]),
            int(sys.argv[5]),
            int(sys.argv[6]),
        )
    else:
        print("invalid option")
