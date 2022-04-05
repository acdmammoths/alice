#!/usr/bin/env python3


import sys


def get_dataset_stats(dataset_path):
    num_transactions = 0
    items = set()
    sum_transaction_lens = 0

    with open(dataset_path) as f:
        for line in f:
            num_transactions += 1
            transaction = line.split()
            for item in transaction:
                items.add(item)
            sum_transaction_lens += len(transaction)

    num_items = len(items)
    avg_transaction_len = sum_transaction_lens / num_transactions
    density = avg_transaction_len / num_items

    print(f"Number of transactions: {num_transactions}")
    print(f"Number of items: {num_items}")
    print(f"Sum of transaction lengths: {sum_transaction_lens}")
    print(f"Average transaction length: {avg_transaction_len}")
    print(f"Density: {density}")


if __name__ == "__main__":
    get_dataset_stats(sys.argv[1])
