# Lucene - Sean Martin 13319354

#Simple demo
type "sudo ./build.sh"
type "sudo ./run.sh"

# Building
To build the project, type "sudo ./build.sh"

# Running
Provided are three scripts, run.sh, index_and_search.sh, and eval.sh

## run.sh
type "sudo ./run.sh" which will run indexing, searching and evaluating with different options.

## index_and_search.sh
index_and_search.sh does what you might expect, but arguments need to be passed in to this script. 

Use the -h flag for more info, by default the script takes in the cran.all.1400 file to index.

See the run.sh file for examples of using this script.

## eval.sh
eval.sh uses trec_eval to evaluate a search results file, but arguments need to be passed in to this script.

Use the -h flag works for more info, by default the script takes in the cranqrel_trec file to evaluate against.

See the run.sh file for examples of using this script.
