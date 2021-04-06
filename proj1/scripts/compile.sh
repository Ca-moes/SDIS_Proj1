#! /usr/bin/bash

mkdir build

javac files/*.java messages/*.java peer/*.java tasks/*.java client/*.java jobs/*.java -d build

cd build

rmiregistry &
