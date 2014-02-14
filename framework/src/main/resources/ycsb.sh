#!/bin/bash
#set -e
min=10
max=20
inc=10
nodes="2 10 40"
FOLDER="PREPARE"
FOLDER_WPM="${FOLDER}/wpm"
ID="1402012_YCSB"
RUN="ACF2"
TEST="${ID}_test.out"
home="/home/didona"
readP="0"
replD="2 3"
numK="1000 25000 100000"
generator="UNIFORM"
zipfP="0.99"
bench="conf/benchmark.xml"
benchST="stub/benchmark_ycsb.xml"
ispn="plugins/infinispan4/conf/cloudtm.xml"
ispnGMU="stub/cloudtm_gmu.xml"
ispnRR="stub/cloudtm.xml"
ispnST="stub/cloudtm.xml"
oscp="${home}/${FOLDER}"
ispnJAR="plugins/infinispan4/lib/infinispan-core.jar"
ops="5_5_5 10_10_10"
retries="RETRY_SAME_CLASS"
estimated_duration=6 #in mins
replP="DEFAULT" #PASSIVE_REPLICATION TOTAL_ORDER"
WPM= #blank to disable
#ISO="SERIALIZABLE" # REPEATABLE_READ"
ISO="REPEATABLE_READ"
RTT_HISTO="rtt_histo.xml"
ACK_HISTO="ack_histo.xml"
LCQ_HISTO="lcq_histo.xml"
transport="tcp"
spins="0"

TEST_ID=

source stub_functions.sh
./bin/master.sh -stop
touch ${TEST}
echo "">${TEST}
echo "Going to take stub config files from ${FOLDER}">>${TEST}
for run in ${RUN}; do
for ret in ${retries}; do
for isoL in ${ISO}; do
for rp in ${replP}; do
for k in ${numK}; do
for op in ${ops}; do
for ro in ${readP}; do
for z in ${zipfP};do
#for (( c=${min}; c<=${max}; c=$(($c + $inc)) ))
for c in ${nodes}; do
#for rd in 2 3 $((($c)/2)) $c; do
for rd in 1 2 $((($c)/2)); do
for spin in ${spins}; do
for gen in ${generator}; do
OK=1
attempts=0
while [[ $OK -eq 1 && $attempts -lt 2 ]]
do
okillj
wp=$(( 100 - $ro ))
TEST_ID=REP${ID}_${isoL}_${ret}_${k}_${op}_${wp}_${rp}_${c}_${rd}_${gen}_${z}_${spin}_RUN${run}
#Move stubs to proper folders
#handle histogram files
handle_histograms
#move benchmark config
cp ${benchST} ${bench}
#change ispn config file
cp ${ispnST} ${ispn}
change_isolation_level ${isoL}
change_num_segments ${c}
change_replication_degree ${rd} 
change_transport ${transport}
change_replication_protocol ${rp}
broadcast_ispn_config_file
#change_spin ${spin}
change_ops ${op}
change_generator ${gen}
change_zipf ${z}
change_nodes $c $c $c
change_readOnly ${ro}
change_keys ${k}
change_retry ${ret}
date >> ${TEST}
echo "Running test with retry ${ret}, isolation level ${isoL}, $k keys, $op operations, $wp writeP, $rp replication protocol, $c nodes, $rd replication degree, ${spin} spin, ${z} zip, ${gen} generator. The relevant folder will be ${TEST_ID}">>${TEST}
if [ $WPM ]
then
./tosb_wpm.sh $c
else
./tosb.sh $c
fi
echo "wait ${estimated_duration} minutes";
for sl in $(seq 1 ${estimated_duration}) 
do
sleep 1m
echo "${sl} slept"
done

wait_until_test_finish
./bin/master.sh -stop
okillj
OK=0
done #while
#REP_DIR=REP${ID}_${rp}_${rd}_${k}_${op}_${wp}
REP_DIR=${TEST_ID}
mkdir ${REP_DIR}  #This simply fails if the dir already exists
echo "The content of report is $(ls report)"
mv reports/* ${REP_DIR}
mv radargun.log ${REP_DIR}
collect_histograms ${TEST_ID} ${REP_DIR} ${c}
cd ${REP_DIR}
collapse_histograms ${TEST_ID}
cd -
mv ${REP_DIR} ../TEST_RESULTS/
if [ $WPM ]
then
cd ../WPM_HISTORY
mkdir ${REP_DIR}
cd -
mv ~/${FOLDER}/wpm/log/csv/* ../WPM_HISTORY/${REP_DIR}/
fi
done 
done #c
#sleep 5
done #readP
done #ops
done #keys
done #replDegree
done #replProtocol
done #isolation level
done #reties
done #run
done
done
