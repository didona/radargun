#!/bin/bash
./bin/master.sh -stop
echo "Starting the benchmark"
./bin/benchmark.sh $(head -n $1 ~/OS_Scripts/machines)
