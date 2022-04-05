#!/usr/bin/env python3

import os
import sys
import json


def analyze(result_path_A, result_path_B):
    sig_freq_itemsets_A = get_sig_freq_itemsets(result_path_A)
    sig_freq_itemsets_B = get_sig_freq_itemsets(result_path_B)
    jaccard_index = get_jaccard_index(sig_freq_itemsets_A, sig_freq_itemsets_B)
    A_is_subset_of_B = sig_freq_itemsets_A.issubset(sig_freq_itemsets_B)
    B_is_subset_of_A = sig_freq_itemsets_B.issubset(sig_freq_itemsets_A)
    basename_A = os.path.basename(result_path_A)
    basename_B = os.path.basename(result_path_B)
    print(f"Jaccard index: {jaccard_index}")
    print(f"{basename_A} is subset of {basename_B}: {A_is_subset_of_B}")
    print(f"{basename_B} is subset of {basename_A}: {B_is_subset_of_A}")


def get_sig_freq_itemsets(result_path):
    sig_freq_itemsets = set()
    with open(result_path) as f:
        result = json.load(f)
        sig_freq_itemsets_dict = result["itemsets"]["sigFreqItemsets"]
        for sig_freq_itemset_str in sig_freq_itemsets_dict.keys():
            sig_freq_itemset_list = sig_freq_itemset_str.split()
            sig_freq_itemset_list.sort()
            sig_freq_itemset_tup = tuple(sig_freq_itemset_list)
            sig_freq_itemsets.add(sig_freq_itemset_tup)
    return sig_freq_itemsets


def get_jaccard_index(set_A, set_B):
    intersection_size = len(set_A.intersection(set_B))
    union_size = len(set_A.union(set_B))
    return intersection_size / union_size


if __name__ == "__main__":
    analyze(sys.argv[1], sys.argv[2])
