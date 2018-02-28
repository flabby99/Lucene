#!/bin/bash

javac -sourcepath src -d ./build/classes/ -cp :./lib/* src/common/Util.java

javac -sourcepath src -d ./build/classes/ -cp :./lib/* src/common/analyzers/TestAnalyzer.java

javac -sourcepath src -d ./build/classes/ -cp :./lib/*:./build/classes src/searching/SearchFiles.java

javac -sourcepath src -d ./build/classes/ -cp :./lib/*:./build/classes src/indexing/IndexFiles.java

