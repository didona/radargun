#!/bin/bash

min=10
max=40
inc=10

writeP="10 50 100"
replD="1 2 3"

bench="conf/benchmark.xml"
benchST="stub/benchmark.xml"
ispn="plugins/infinispan4/conf/cloudtm.xml"
ispnST="conf/cloudtm.xml"

ops="10_1 50_5 100_10"

function change_replication_degree(){
cp ${ispnST} ${ispn}
sed -i "s/REPD/$1/g" ${ispn}
./oscp ${ispn} ~/gmuGun/${ispn}
}

#Change the number of nodes   min max inc
function change_nodes(){
sed -i "s/INITS/$1/1" ${bench}
sed -i "s/MAXS/$2/1" ${bench}
sed -i "s/INCS/$3/1" ${bench}
}

function change_rw(){
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



for op in ${ops}
do
for wp in ${writeP}
do
for (( c=${min}; c<=${max}; c=$(($c + $inc)) ))
do
cp ${benchST} ${bench}
change_ops ${op}
change_nodes $min $max $inc
change_rw ${wp}
echo "Running test with ${ops} ops, ${readP} readOnly and on $c nodes"
./tosb.sh $c
done #c
mv reports REP_${op}_${rp}
sleep 5
done #readP
done #ops
