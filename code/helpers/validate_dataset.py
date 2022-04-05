#!/usr/bin/env python3

import sys


def check_dataset(dataset_path):
    """Checks to see if all transactions are sets."""
    with open(dataset_path) as f:
        for i, line in enumerate(f):
            transaction_list = line.split()
            transaction_set = set(transaction_list)
            if len(transaction_set) != len(transaction_list):
                print(f"Transaction {i} is not a set: {transaction_list}")


if __name__ == "__main__":
    check_dataset(sys.argv[1])
