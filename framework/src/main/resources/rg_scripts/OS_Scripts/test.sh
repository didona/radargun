#!/bin/bash
#set -e
min=10
max=40
inc=10

home="/home/didona"
writeP="50 10 90"
replD="1 3"
numK="1000 100000"
bench="conf/benchmark.xml"
benchST="stub/benchmark.xml"
ispn="plugins/infinispan4/conf/cloudtm.xml"
ispnST="stub/cloudtm.xml"
oscp="${home}/OS_Scripts/"
ops="10_10 100_10"

function change_replication_degree(){
cp ${ispnST} ${ispn}
sed -i "s/REPD/$1/g" ${ispn}
cd ${oscp}
from=$home/gmuGun/${ispn}
./oscp  ${from} ${home}/gmuGun/${ispn}
cd -
}

#Change the number of nodes   min max inc
function change_nodes(){
sed -i "s/INITS/$1/1" ${bench}
sed -i "s/MAXS/$2/1" ${bench}
sed -i "s/INCS/$3/1" ${bench}
}

function change_rw(){
sed -i "s/WRITEP/$1/1" ${bench}
}

function get_r(){
echo $1 | cut -d"_" -f 1
}

function get_w(){
echo $1 | cut -d"_" -f 2
}

function change_ops(){
nr=$(get_r $1)
nw=$(get_w $1)
sed -i "s/NUMR/${nr}/g" ${bench}
sed -i "s/NUMW/${nw}/g" ${bench}
}

function change_keys(){
sed -i "s/NKEYS/$1/g" ${bench}
}

for rd in ${replD}
do
for k in ${numK}
do
for op in ${ops}
do
for wp in ${writeP}
do
for (( c=${min}; c<=${max}; c=$(($c + $inc)) ))
do
cp ${benchST} ${bench}
change_replication_degree ${rd}
change_ops ${op}
change_nodes $c $c $c
change_rw ${wp}
change_keys ${k}
echo "Running test with replication degree ${rd}, ${op} ops, ${wp} updateXactPerc, ${k} keys and on $c nodes"
./tosb.sh $c
done #c
mkdir REP_${rd}_${k}${op}_${wp}
mv reports/* REP_${rd}_${k}_${op}_${wp}/
#sleep 5
done #readP
done #ops
done #keys
done #replDegree
