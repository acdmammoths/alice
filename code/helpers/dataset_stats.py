#!/usr/bin/env python3

import sys


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
