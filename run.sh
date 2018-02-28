#!/bin/bash

cd build/classes/

java -cp :../../lib/* indexing.IndexFiles -docs ../../res/cran.all.1400 -index ../../index/ -bm25

java -cp :../../lib/* searching.SearchFiles -index ../../index/ -queries ../../res/cran.qry -bm25 -result ../../results/results.txt

