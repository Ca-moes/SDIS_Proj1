#!/bin/bash

# get the container IP
IP=`ifconfig eth0 | grep 'inet ' | awk '{print $2}'`

# get the service name you specified in the docker-compose.yml 
# by a reverse DNS lookup on the IP
SERVICE=`dig -x $IP +short | cut -d'_' -f2`

# the number of replicas is equal to the A records 
# associated with the service name
COUNT=`dig $SERVICE +short | wc -l`

# extract the replica number from the same PTR entry
INDEX=`dig -x $IP +short | sed 's/.*_\([0-9]*\)\..*/\1/'`

# Hello I'm container 1 of 5 
echo "Hello I'm container $INDEX of $COUNT"

mkdir -p out

javac files/*.java messages/*.java peer/*.java tasks/*.java client/*.java -d ./out

cd out

rmiregistry &

sleep 1

java peer.Peer 1.0 "$INDEX" peer"$INDEX" 224.1.1.1:81 224.1.1.2:82 224.1.1.3:83