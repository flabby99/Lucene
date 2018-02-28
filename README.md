# Lucene - Sean Martin 13319354

# Building
To build the project, run ./build.sh

# Running
Provided are three scripts, run ./run.sh which will run indexing, searching and evaluating with different options.
index_and_search.sh does what you might expect, use the -h flag for more info
by default the script takes in the cran.all.1400 file to index
eval.sh uses trec_eval to evaluate a search results file, again the -h flag works for more info
by default the script takes in the cranqrel_trec file to evaluate against
