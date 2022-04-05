#!/usr/bin/env python3

import sys


def transaction_len(dataset_path, transaction_len):
    """Checks to see if all are transactions are the given length."""
    with open(dataset_path) as f:
        for i, line in enumerate(f):
            transaction = line.split()
            if len(transaction) != transaction_len:
                print(f"Transaction {i} is not length {transaction_len}")


if __name__ == "__main__":
    transaction_len(sys.argv[1], int(sys.argv[2]))
