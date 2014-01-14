#!/bin/bash
./bin/master.sh -stop
echo "Starting the benchmark"
./bin/benchmark_wpm.sh -t $(head -n $1 machines)
