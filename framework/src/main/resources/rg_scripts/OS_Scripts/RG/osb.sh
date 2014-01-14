#!/bin/bash
./bin/master.sh -stop
echo "Starting the benchmark"
./bin/benchmark.sh $(cat ~/OS_Scripts/machines)
