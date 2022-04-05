#!/usr/bin/env python3

import sys
from collections import defaultdict


def ibm_to_spmf(in_path, out_path):
    spmf_dict = defaultdict(list)

    with open(in_path) as f:
        for line in f:
            tid, _, item = line.split()
            spmf_dict[tid].append(item)

    with open(out_path, "w") as f:
        for i in range(1, len(spmf_dict) + 1):
            tid = str(i)
            transaction_str = " ".join(spmf_dict[tid])
            f.write(transaction_str)
            f.write("\n")


if __name__ == "__main__":
    ibm_to_spmf(sys.argv[1], sys.argv[2])
