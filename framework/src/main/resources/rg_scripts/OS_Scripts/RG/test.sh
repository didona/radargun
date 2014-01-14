#!/bin/bash

min=10
max=40
inc=10

readP="90 50 0"
replD="1 2 3"



bench="conf/benchmark.xml"
benchST="stub/benchmark.xml"
ispn="plugins/infinispan4/conf/cloudtm.xml"
ispnST="conf/cloudtm.xml"
function change_replication_degree(){
cp ${ispnST} ${ispn}
sed -i "s/REPD/$1/g" ${ispn}
}

#Change the number of nodes   min max inc
function change_nodes(){
sed -i "s/INITS/$1/1" ${bench}
sed -i "s/MAXS/$2/1" ${bench}
sed -i "s/INCS/$3/1" ${bench}
}

function change_rw(){
let w=$((100 - $1))
sed -i "s/READP/$1/1" ${bench}
sed -i "s/WRITEP/$w/1" ${bench}
}

function get_r(){
cut -d"_" -f 1 $1
}

function get_w(){
cut -d"_" -f 2 $1
}

function change_ops(){
nr=$(get_r) $1
nw=${get_w} $1
sed -i "s/NUMR/${nr}/g" ${bench}
sed -i "s/NUMW/${nw}/g" ${bench}
}

for rp in ${readP}
do
for (( c=${min}; c<=${max}; c=$(($c + $inc)) ))
do
cp ${benchST} ${bench}
change_nodes $min $max $inc
change_rw
echo "Running test on $c nodes"
./tosb.sh $c
done #c
done #readP
