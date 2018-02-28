#!/bin/bash

cd trec_eval.8.1/

#Input the results directory
./trec_eval -l3 ../res/cranqrel_trec $1
