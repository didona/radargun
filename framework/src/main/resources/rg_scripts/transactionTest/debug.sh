uuuuuuubash
#set -e
min=10
max=20
inc=10
#nodes="2 4 6 8 10 20 30 40 50 60 70 80 90 99"
nodes="45 2 5 25 10"
FOLDER="RR_V3_TAAS"
FOLDER_WPM="${FOLDER}/wpm"
ID="DB"
TEST="${ID}_test.out"
home="/home/didona"
writeP="5 10 50"
replD="2 3"
numK="100000 5000 25000"
bench="conf/benchmark.xml"
benchST="stub/benchmark.xml"
ispn="plugins/infinispan4/conf/cloudtm.xml"
ispnGMU="stub/cloudtm_gmu.xml"
ispnRR="stub/cloudtm.xml"
ispnST="stub/cloudtm.xml"
oscp="${home}/${FOLDER}"
ispnJAR="plugins/infinispan4/lib/infinispan-core.jar"
#ops="10_2_10 15_5_15"
#ops="6_1_6 16_2_16 50_5_50"
ops="10_2_10 15_5_15"
retries="RETRY_SAME_CLASS"
estimated_duration=10 #in mins
#replP="DEFAULT TOTAL_ORDER PASSIVE_REPLICATION" #TOTAL_ORDER DEFAULT
replP="DEFAULT" #PASSIVE_REPLICATION TOTAL_ORDER"
WPM= #blank to disable
ISO="SERIALIZABLE" # REPEATABLE_READ"
RTT_HISTO="rtt_histo.xml"
ACK_HISTO="ack_histo.xml"
LCQ_HISTO="lcq_histo.xml"

TEST_ID=

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

./bin/master.sh -stop
touch ${TEST}
echo "">${TEST}
echo "Going to take stub config files from ${FOLDER}">>${TEST}
for run in 4
do
for ret in ${retries}
do
for isoL in ${ISO}
do
for rp in ${replP}
do
for k in ${numK}
do
for op in ${ops}
do
for wp in ${writeP}
do
#for (( c=${min}; c<=${max}; c=$(($c + $inc)) ))dd
for c in ${nodes}
do
#for rd in 2 $(($c/3)) $(($c/2)) $c #2
for rd in $((($c)/2))
do
OK=1
attempts=0
while [[ $OK -eq 1 && $attempts -lt 2 ]]
do
okillj
TEST_ID=REP${ID}_${isoL}_${ret}_${k}_${op}_${wp}_${rp}_${c}_${rd}_RUN${run}
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
change_replication_protocol ${rp}
broadcast_ispn_config_file
change_ops ${op}
change_nodes $c $c $c
change_rw ${wp}
change_keys ${k}
change_retry ${ret}
date >> ${TEST}
echo "Running test with retry ${ret}, isolation level ${isoL}, $k keys, $op operations, $wp writePercentage, $rp replication protocol, $c nodes, $rd replication degree. The relevant folder will be ${TEST_ID}">>${TEST}
if [ $WPM ]
then
./tosb_wpm.sh $c
else
./tosb.sh $c
fi
echo "wait ${estimated_duration} minutes";
sleep ${estimated_duration}m;

wait_until_test_finish
./bin/master.sh -stop
okillj
OK=0
done #while
#REP_DIR=REP${ID}_${rp}_${rd}_${k}_${op}_${wp}
REP_DIR=${TEST_ID}
mkdir ${REP_DIR}  #This simply fails if the dir already exists
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
