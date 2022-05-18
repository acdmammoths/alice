#!/usr/bin/env python3

import sys
import numpy as np


def get_dataset_stats(data_dir, dataset):
    num_transactions = 0
    items = set()
    sum_transaction_lens = 0

    with open(data_dir+dataset+'.txt') as f:
        for line in f:
            num_transactions += 1
            transaction = line.split()
            for item in transaction:
                items.add(item)
            sum_transaction_lens += len(transaction)

    num_items = len(items)
    avg_transaction_len = sum_transaction_lens / num_transactions
    density = avg_transaction_len / num_items
    return [dataset, num_transactions, num_items, sum_transaction_lens, avg_transaction_len, density]


def compute_BJDM(data_dir, dataset):
    counter = 0
    edges = list()
    left_degrees = dict()
    right_degrees = dict()
    max_len = 0
    with open(data_dir+dataset+'.txt') as f:
        for line in f:
            transaction = line.split()
            left_degrees[counter] = len(transaction)
            items = [int(x) for x in transaction]
            for item in items:
                edges.append((counter, item))
                right_degrees[item] = right_degrees.get(item, 0) + 1
            counter += 1
    max_len = max(left_degrees.values())
    min_len = min(left_degrees.values())
    max_deg = max(right_degrees.values())
    print(max_len, max_deg)
    if max_len == min_len:
        max_len = 1
    BJDM = np.zeros((max_len, max_deg))
    for edge in edges:
        l = left_degrees[edge[0]]-1 if max_len > 1 else 0
        BJDM[l][right_degrees[edge[1]]-1] += 1
    return BJDM, min_len
