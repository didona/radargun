#!/bin/bash
#set -e
min=10
max=20
inc=10
#nodes="2 4 6 8 10 20 30 40 50 60 70 80 90 99"
nodes="4 8 10 20 40 60 80 100"
FOLDER="RR_V3_TAAS"
ID="1010_TAAS_V3_DEMO_PARTIAL"
TEST="${ID}_test.out"
home="/home/ubuntu"
writeP="1 50"
replD="1 2 8 14 20"
numK="100000"
bench="conf/benchmark.xml"
benchST="stub/benchmark.xml"
ispn="plugins/infinispan4/conf/cloudtm.xml"
ispnST="stub/cloudtm.xml"
oscp="${home}/${FOLDER}"
ops="1000_5"
retries="RETRY_SAME_CLASS"
estimated_duration=7 #in mins
#replP="DEFAULT TOTAL_ORDER PASSIVE_REPLICATION" #TOTAL_ORDER DEFAULT
replP="TOTAL_ORDER PASSIVE_REPLICATION DEFAULT"
function wait_until_test_finish() {
local MASTER_PID="";
#5 minutes max waiting time (+ estimated test duration)
for ((j = 0; j < 10; ++j)); do
#MASTER_PID=$(jps | grep LaunchMaster | cut -d" " -f1)
MASTER_PID=$(ps aux | grep java | grep LaunchMaster | awk ' { print $2 } ')
echo "Checking if the master finished..."
if [ -z "${MASTER_PID}" ]; then
echo "Master finished! No PID found! returning... @" `date`
return;
fi
echo "Master is running. PID is ${MASTER_PID}. @" `date`
sleep 30s
done
echo "Timeout!! Master is running. PID is ${MASTER_PID}. @" `date`
}


function change_replication_degree(){
sed -i "s/REPD/$1/g" ${ispn}
}

function change_replication_protocol(){
sed -i "s/REPP/$1/g" ${ispn}
if [ "$1" == "PASSIVE_REPLICATION" ]
then
sed -i "s/IPR/true/g" ${bench}
else
sed -i "s/IPR/false/g" ${bench}
fi
}

function broadcast_ispn_config_file(){
cd ${oscp}
from=$home/${FOLDER}/${ispn}
./oscp  ${from} ${home}/${FOLDER}/${ispn}
cd -
}

function okillj(){
cd ${oscp}
./okillj
cd -
}

function change_num_threads(){
sed -i "s/THREADS/$1/1" ${bench}
}

function change_retry(){
sed -i "s/RETRY_SAME_CLASS/$1/g" ${bench}
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

./bin/master.sh -stop
touch ${TEST}
echo "">${TEST}
echo "Going to take stub config files from ${FOLDER}">>${TEST}
for ret in ${retries}
do
for rp in ${replP}
do
for k in ${numK}
do
for op in ${ops}
do
for wp in ${writeP}
do
#for (( c=${min}; c<=${max}; c=$(($c + $inc)) ))
for c in ${nodes}
do
for rd in 2 $((($c)/2))
do
OK=1
attempts=0
while [[ $OK -eq 1 && $attempts -lt 2 ]]
do
okillj
#Move stubs to proper folders
#move benchmark config
cp ${benchST} ${bench}
#change ispn config file
cp ${ispnST} ${ispn}
change_replication_degree ${rd} 
change_replication_protocol ${rp}
broadcast_ispn_config_file
change_ops ${op}
change_nodes $c $c $c
change_rw ${wp}
change_keys ${k}
change_retry ${ret}
date >> ${TEST}
echo "Running test with retry ${ret} replication degree ${rd}, ${op} ops, ${wp} updateXactPerc, ${k} keys and on $c nodes">>${TEST}
./tosb.sh $c
echo "wait ${estimated_duration} minutes";
sleep ${estimated_duration}m;

wait_until_test_finish
./bin/master.sh -stop
okillj
OK=0
done #while
#REP_DIR=REP${ID}_${rp}_${rd}_${k}_${op}_${wp}
REP_DIR=REP${ID}_${ret}_${k}_${op}_${wp}_${rp}_${rd}
mkdir ${REP_DIR}  #This simply fails if the dir already exists
mv reports/* ${REP_DIR}
done #c
#sleep 5
done #readP
done #ops
done #keys
done #replDegree
done #replProtocol
done #reties
