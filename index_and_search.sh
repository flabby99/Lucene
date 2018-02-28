#!/bin/bash

cd build/classes/

#Do a new index and search based on argument
java -cp :../../lib/* indexing.IndexFiles -docs ../../res/cran.all.1400 $1

java -cp :../../lib/* searching.SearchFiles -queries ../../res/cran.qry $1

