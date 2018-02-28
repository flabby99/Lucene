#!/bin/bash

./index_and_search.sh "-index ../../index/ -result ../../results/results.txt -bm25"

echo "Results from bm25"

./eval.sh ../results/results.txt

./index_and_search.sh "-index ../../tfidf_index/ -result ../../results/tfidf_results.txt"

echo "Results from tf-idf"

./eval.sh ../results/tfidf_results.txt

./index_and_search.sh "-index ../../eng_index/ -result ../../results/eng_results.txt -bm25 -english_ana"

echo "Results from bm25 with english analyzer"

./eval.sh ../results/eng_results.txt

./index_and_search.sh "-index ../../eng_tfidf_index/ -result ../../results/eng_tfidf_results.txt -english_ana"

echo "Results from tfidf with english analyzer"

./eval.sh ../results/eng_tfidf_results.txt



