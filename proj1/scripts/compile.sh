#! /usr/bin/bash

mkdir build

rmiregistry &

javac files/*.java messages/*.java peer/*.java tasks/*.java client/*.java jobs/*.java -d build
