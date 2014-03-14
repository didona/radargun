package org.radargun.stages;

import org.radargun.CacheWrapper;
import org.radargun.DistStageAck;
import org.radargun.stamp.vacation.VacationStressor;
import org.radargun.state.MasterState;
import org.radargun.utils.StatSampler;
import org.radargun.ycsb.YCSB;
import org.radargun.ycsb.YCSBStressor;
import org.radargun.ycsb.generators.CounterGenerator;
import org.radargun.ycsb.generators.DZipfianGenerator;
import org.radargun.ycsb.generators.HotspotIntegerGenerator;
import org.radargun.ycsb.generators.IntegerGenerator;
import org.radargun.ycsb.generators.SkewedLatestGenerator;
import org.radargun.ycsb.generators.UniformIntegerGenerator;
import org.radargun.ycsb.generators.ZipfianGenerator;
import org.radargun.ycsb.transaction.diego.D_YCSBStressor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YCSBBenchmarkStage extends AbstractDistStage {

   private static final String SIZE_INFO = "SIZE_INFO";

   private transient CacheWrapper cacheWrapper;

   private transient YCSBStressor[] ycsbStressors;

   private int multipleReadCount;
   private int recordCount;
   private int executionTime;
   private int threads;
   private int readOnly;
   private boolean allowBlindWrites = true;
   private e_gen generator = null;
   private double zipf_const = .99D;
   private int numWrites = 1;
   private int dzipf_groups = 0;
   protected long statsSamplingInterval = 0L;
   protected StatSampler sampler = null;
   protected double dataHotSpot = 0.01;
   protected double reqHotSpot = 0.99;

   private String workload = null; //If != null I will use the workload based stressor
   private int numOps;

   private YCSBStressor buildYCSBSTressor() {
      if (workload == null)
         return new YCSBStressor();
      else
         return new D_YCSBStressor(workload, numOps);
   }

   public void setNumOps(int numOps) {
      this.numOps = numOps;
   }

   @Override
   public DistStageAck executeOnSlave() {
      DefaultDistStageAck result = new DefaultDistStageAck(slaveIndex, slaveState.getLocalAddress());
      this.cacheWrapper = slaveState.getCacheWrapper();
      if (cacheWrapper == null) {
         log.info("Not running test on this slave as the wrapper hasn't been configured.");
         return result;
      }

      log.info("Starting YCSBBenchmarkStage: " + this.toString());

      YCSB.init(this.readOnly, recordCount);
      ycsbStressors = new YCSBStressor[threads];

      for (int t = 0; t < ycsbStressors.length; t++) {

         ycsbStressors[t] = buildYCSBSTressor();
         ycsbStressors[t].setCacheWrapper(cacheWrapper);
         ycsbStressors[t].setRecordCount(this.recordCount);
         ycsbStressors[t].setMultiplereadcount(this.multipleReadCount);
         ycsbStressors[t].setAllowBlindWrites(this.allowBlindWrites);
         if (generator != null) {
            ycsbStressors[t].setIg(buildIntegerGenerator());
            ycsbStressors[t].setNumWrites(this.numWrites);
         }
      }

      try {
         Thread[] workers = new Thread[ycsbStressors.length];
         for (int t = 0; t < workers.length; t++) {
            workers[t] = new Thread(ycsbStressors[t]);
         }
         for (int t = 0; t < workers.length; t++) {
            workers[t].start();
         }
         try {
            if (statsSamplingInterval > 0) {
               sampler = new StatSampler(statsSamplingInterval);
               log.trace("Starting sampler with samplingInterval " + statsSamplingInterval);
               sampler.startAfterInterval();
            }
            Thread.sleep(executionTime);
         } catch (InterruptedException e) {
         }
         for (int t = 0; t < workers.length; t++) {
            ycsbStressors[t].setPhase(VacationStressor.SHUTDOWN_PHASE);
            if (sampler != null)
               sampler.cancel();
         }
         for (int t = 0; t < workers.length; t++) {
            workers[t].join();
         }
         Map<String, String> results = new LinkedHashMap<String, String>();
         String sizeInfo = "size info: " + cacheWrapper.getInfo() +
               ", clusterSize:" + super.getActiveSlaveCount() +
               ", nodeIndex:" + super.getSlaveIndex() +
               ", cacheSize: " + cacheWrapper.getCacheSize();
         results.put(SIZE_INFO, sizeInfo);
         long aborts = 0L;
         long throughput = 0L;
         for (int t = 0; t < workers.length; t++) {
            aborts += ycsbStressors[t].getRestarts();
            throughput += ycsbStressors[t].getThroughput();
         }
         results.put("DURATION", str(executionTime));
         results.put("THROUGHPUT", (((throughput + 0.0) * 1000) / executionTime) + "");
         results.put("TOTAL_RESTARTS", aborts + "");
         results.put("NUM_THREADS", str(threads));
         results.put("NUM_KEYS", str(recordCount));
         results.put("DATA_ACCESS_PATTERN", str(generator));
         results.put("SKEW", String.valueOf(zipf_const));
         results.put("DATA_HS", String.valueOf(dataHotSpot));
         results.put("REQUEST_HS", String.valueOf(reqHotSpot));
         results.put("CPU_USAGE", str(sampler != null ? sampler.getAvgCpuUsage() : "Not_Available"));
         results.put("MEM_USAGE", str(sampler != null ? sampler.getAvgMemUsage() : "Not_Available"));
         results.putAll(cacheWrapper.getAdditionalStats());
         log.info(sizeInfo);
         result.setPayload(results);
         return result;
      } catch (Exception e) {
         log.warn("Exception while initializing the test", e);
         result.setError(true);
         result.setRemoteException(e);
         return result;
      }
   }

   protected String str(Object o) {
      return String.valueOf(o);
   }

   public boolean processAckOnMaster(List<DistStageAck> acks, MasterState masterState) {
      logDurationInfo(acks);
      boolean success = true;
      Map<Integer, Map<String, Object>> results = new HashMap<Integer, Map<String, Object>>();
      masterState.put("results", results);
      for (DistStageAck ack : acks) {
         DefaultDistStageAck wAck = (DefaultDistStageAck) ack;
         if (wAck.isError()) {
            success = false;
            log.warn("Received error ack: " + wAck);
         } else {
            if (log.isTraceEnabled())
               log.trace(wAck);
         }
         Map<String, Object> benchResult = (Map<String, Object>) wAck.getPayload();
         if (benchResult != null) {
            results.put(ack.getSlaveIndex(), benchResult);
            Object reqPerSes = benchResult.get("THROUGHPUT");
            if (reqPerSes == null) {
               throw new IllegalStateException("This should be there!");
            }
            log.info("On slave " + ack.getSlaveIndex() + " it took " + (Double.parseDouble(reqPerSes.toString()) / 1000.0) + " seconds");
            log.info("Received " + benchResult.remove(SIZE_INFO));
         } else {
            log.trace("No report received from slave: " + ack.getSlaveIndex());
         }
      }
      return success;
   }

   public CacheWrapper getCacheWrapper() {
      return cacheWrapper;
   }

   public void setCacheWrapper(CacheWrapper cacheWrapper) {
      this.cacheWrapper = cacheWrapper;
   }

   public int getMultipleReadCount() {
      return multipleReadCount;
   }

   public void setMultipleReadCount(int multipleReadCount) {
      this.multipleReadCount = multipleReadCount;
   }

   public int getRecordCount() {
      return recordCount;
   }

   public void setRecordCount(int recordCount) {
      this.recordCount = recordCount;
   }

   public int getExecutionTime() {
      return executionTime;
   }

   public void setExecutionTime(int executionTime) {
      this.executionTime = executionTime;
   }

   public int getThreads() {
      return threads;
   }

   public void setThreads(int threads) {
      this.threads = threads;
   }

   public int getReadOnly() {
      return readOnly;
   }

   public void setReadOnly(int readOnly) {
      this.readOnly = readOnly;
   }

   public boolean isAllowBlindWrites() {
      return allowBlindWrites;
   }

   public void setAllowBlindWrites(boolean allowBlindWrites) {
      this.allowBlindWrites = allowBlindWrites;
   }


   public void setGenerator(String generator) {
      this.generator = e_gen.valueOf(generator);
   }

   public int getNumWrites() {
      return numWrites;
   }

   public void setNumWrites(int numWrites) {
      this.numWrites = numWrites;
   }

   public long getStatsSamplingInterval() {
      return statsSamplingInterval;
   }

   public void setStatsSamplingInterval(long statsSamplingInterval) {
      this.statsSamplingInterval = statsSamplingInterval;
   }

   public void setZipf_const(double zipf_const) {
      this.zipf_const = zipf_const;
   }

   public String getWorkload() {
      return workload;
   }

   public void setWorkload(String workload) {
      this.workload = workload;
   }

   private IntegerGenerator buildIntegerGenerator() {
      switch (this.generator) {
         case UNIFORM:
            return new UniformIntegerGenerator(0, recordCount);
         case SKEWED_LAST:
            return new SkewedLatestGenerator(new CounterGenerator(recordCount), zipf_const);
         case ZIPF:
            return new ZipfianGenerator(recordCount, zipf_const);
         case HOTSPOT:
            return new HotspotIntegerGenerator(0, recordCount, dataHotSpot, reqHotSpot);
         case D_ZIPF: {
            if (dzipf_groups == 0)
               throw new IllegalArgumentException("DZIPF generator but no groups specified!");
            if (dzipf_groups < cacheWrapper.getNumMembers())
               throw new IllegalArgumentException("Number of groups is smaller than number of nodes!");
            return new DZipfianGenerator(recordCount, zipf_const, dzipf_groups);
         }
         default:
            throw new IllegalArgumentException(this.generator + " is not a valid generator. Valid values are " + Arrays.toString(e_gen.values()));
      }

   }

   public void setDataHotSpot(double dataHotSpot) {
      this.dataHotSpot = dataHotSpot;
   }

   public void setReqHotSpot(double reqHotSpot) {
      this.reqHotSpot = reqHotSpot;
   }

   private enum e_gen {
      UNIFORM, ZIPF, SKEWED_LAST, D_ZIPF, HOTSPOT
   }

}
