#!/bin/bash
./bin/master.sh -stop
echo "Starting the benchmark"
./bin/benchmark.sh -t $(head -n $1 machines)
