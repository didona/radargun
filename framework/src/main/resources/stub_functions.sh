#!/bin/bash

function wait_until_test_finish() {
local MASTER_PID="";
#8 minutes max waiting time (+ estimated test duration)
for ((j = 0; j < 5; ++j)); do
#MASTER_PID=$(jps | grep LaunchMaster | cut -d" " -f1)
MASTER_PID=$(ps aux | grep java | grep LaunchMaster | awk ' { print $2 } ')
echo "Checking if the master finished..."
if [ -z "${MASTER_PID}" ]; then
echo "Master finished! No PID found! returning... @" `date` >> $TEST
return;
fi
echo "Master is running. PID is ${MASTER_PID}. @" `date` >> $TEST
sleep 60s
done
echo "Timeout!! Master is running. PID is ${MASTER_PID}. @" `date` >> $TEST
}

function handle_histograms(){
cp stub/${RTT_HISTO} conf/${RTT_HISTO}
sed -i "s/NAME/${TEST_ID}.rtt.txt/g" conf/${RTT_HISTO}
cp stub/${ACK_HISTO} conf/${ACK_HISTO}
sed -i "s/NAME/${TEST_ID}.ack.txt/g" conf/${ACK_HISTO}
cp stub/${LCQ_HISTO} conf/${LCQ_HISTO}
sed -i "s/NAME/${TEST_ID}.lcq.txt/g" conf/${LCQ_HISTO}
./oscp conf/${RTT_HISTO} $PWD/conf/
./oscp conf/${ACK_HISTO} $PWD/conf/
./oscp conf/${LCQ_HISTO} $PWD/conf/
}

#Collect the histogram from $3 nodes relevant to test $1 and move them into $2
function collect_histograms(){
for i in $(head -n $3 ../OS_Scripts/machines)
do
scp didona@$i:~/${FOLDER}/${1}.rtt.txt ${2}/${1}.rtt.txt.${i}
scp didona@$i:~/${FOLDER}/${1}.ack.txt ${2}/${1}.ack.txt.${i}
scp didona@$i:~/${FOLDER}/${1}.lcq.txt ${2}/${1}.lcq.txt.${i}
done
}


function collapse_histograms(){
#sum everything
cat ${1}.rtt* | awk '{sums[$1] += $2;} END { for (i in sums) print i " " sums[i]; }' > ${1}.rtt
#sort
#sort -n -o ${1}.rtt

cat ${1}.ack* | awk '{sums[$1] += $2;} END { for (i in sums) print i " " sums[i]; }' > ${1}.ack
#sort
#sort -n -o ${1}.ack

cat ${1}.lcq* | awk '{sums[$1] += $2;} END { for (i in sums) print i " " sums[i]; }' > ${1}.lcq
}

function change_isolation_level(){
iso=""
ver=""
if [ "$1" == "REPEATABLE_READ" ]
then
iso="REPEATABLE_READ"
ver="SIMPLE"
else
iso="SERIALIZABLE"
ver="GMU"
fi
sed -i "s/ISO/${iso}/g" ${ispn}
sed -i "s/VERS/${ver}/g" ${ispn}
}

function change_num_segments(){
let s=10*$1
sed -i "s/SEG/$s/g" ${ispn}
}

function change_replication_degree(){
sed -i "s/REPD/$1/g" ${ispn}
}

function change_spin(){
sed -i "s/SPIN/$1/g" ${bench}
}

function change_transport(){
sed -i "s/TRANSPORT/$1/g" ${ispn}
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

function broadcast_ispn_core(){
cd ${oscp}
from=$home/${FOLDER}/$1
./oscp  ${from} ${home}/${FOLDER}/${ispnJAR}
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

function get_wr(){
echo $1 | cut -d"_" -f 3
}

function change_ops(){
nr=$(get_r $1)
nw=$(get_w $1)
nwr=$(get_wr $1)
sed -i "s/NUMWR/${nwr}/g" ${bench}
sed -i "s/NUMR/${nr}/g" ${bench}
sed -i "s/NUMW/${nw}/g" ${bench}
}

function change_keys(){
sed -i "s/NKEYS/$1/g" ${bench}
}

function change_zipf(){
sed -i "s/ZIPF_P/$1/g" ${bench}
}

function change_generator(){
sed -i "s/GEN/$1/g" ${bench}
}

function change_readOnly(){
sed -i "s/READO/$1/g" ${bench}
}

function change_item_size(){
sed -i "s/ASIZE/${1}/g" ${bench}
}

