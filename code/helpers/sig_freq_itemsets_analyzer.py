#!/usr/bin/env python3

import os
import sys
import json
import pandas as pd


def analyze(result_path_A, result_path_B):
    sig_freq_itemsets_A = get_sig_freq_itemsets(result_path_A)
    sig_freq_itemsets_B = get_sig_freq_itemsets(result_path_B)
    if len(sig_freq_itemsets_A) > 0 or len(sig_freq_itemsets_B) > 0:
        jaccard_index = get_jaccard_index(sig_freq_itemsets_A, sig_freq_itemsets_B)
        A_diff_B = get_disapp_index(sig_freq_itemsets_A,sig_freq_itemsets_B)
        B_diff_A = get_disapp_index(sig_freq_itemsets_B,sig_freq_itemsets_A)
        A_is_subset_of_B = sig_freq_itemsets_A.issubset(sig_freq_itemsets_B)
        B_is_subset_of_A = sig_freq_itemsets_B.issubset(sig_freq_itemsets_A)
    else:
        jaccard_index = B_diff_A = A_diff_B = 1
        A_is_subset_of_B = B_is_subset_of_A = True
    return len(sig_freq_itemsets_A), len(sig_freq_itemsets_B), jaccard_index, A_diff_B, B_diff_A, A_is_subset_of_B, B_is_subset_of_A


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


def get_freq_itemsets(result_path, db_name):
    rows = []
    with open(result_path) as f:
        result = json.load(f)
        freq_itemsets_dict = result["itemsets"]["freqItemsets"]
        for freq_itemset_str, lst in freq_itemsets_dict.items():
            freq_itemset_len = len(freq_itemset_str.split())
            rows.append([db_name, freq_itemset_str, freq_itemset_len, lst["sup"]])
    df = pd.DataFrame(rows)
    df.columns = ['DataSet', 'FI', 'Size', 'Support']
    return df


def get_jaccard_index(set_A, set_B):
    intersection_size = len(set_A.intersection(set_B))
    union_size = len(set_A.union(set_B))
    return intersection_size / union_size


def get_disapp_index(orig, sampled):
    diff = set(orig)
    diff_size = len(diff.difference(sampled))
    return diff_size / len(diff)


if __name__ == "__main__":
    analyze(sys.argv[1], sys.argv[2])
