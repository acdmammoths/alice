import json
import pandas as pd


def get_num_freq_itemsets_stats(result_path, db_name):
    rows = []
    with open(result_path) as f:
        result = json.load(f)
        fi_by_size = result["runInfo"]["freqItemsetLenToCount"]
        for k,lst in fi_by_size.items():
            # original dataset
            rows.append([k, db_name, lst, lst, lst, lst, lst])
        numFreqItemsetsStats = result["numFreqItemsetsStats"]
        for numFIStats in numFreqItemsetsStats:
            sampler = numFIStats["sampler"]
            for k,lst in numFIStats["freqItemsetLenToCountQuartiles"].items():
                r = list([k, sampler])
                r.extend(lst)
                r.append(numFIStats["pvalue"])
                rows.append(r)
    df = pd.DataFrame(rows)
    df.columns = ['Size', 'Algorithm', 'min', 'Q1', 'med', 'Q3', 'max', 'pvalue']
    return df