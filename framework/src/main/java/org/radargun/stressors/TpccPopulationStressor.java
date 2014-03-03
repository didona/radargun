package org.radargun.stressors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radargun.CacheWrapper;
import org.radargun.stages.TpccPopulationStage;
import org.radargun.tpcc.DeterministicThreadParallelTpccPopulation;
import org.radargun.tpcc.Diego_DeterministicPopulation;
import org.radargun.tpcc.PassiveReplicationTpccPopulation;
import org.radargun.tpcc.ThreadParallelTpccPopulation;
import org.radargun.tpcc.TpccPopulation;

import java.util.Map;

/**
 * Populate <code>numWarehouses</code> Warehouses in cache.
 *
 * @author peluso@gsd.inesc-id.pt , peluso@dis.uniroma1.it
 * @author Diego Didona, didona@gsd.inesc-id.pt
 * @author Pedro Ruivo
 */
public class TpccPopulationStressor extends AbstractCacheWrapperStressor {

   private static Log log = LogFactory.getLog(TpccPopulationStage.class);
   private static final String POPULATION_STRING = "___TPCC___ALREADY___POPULATED___";

   private int numWarehouses;

   private long cLastMask = 255L;

   private long olIdMask = 8191L;

   private long cIdMask = 1023L;

   private int slaveIndex;

   private int numSlaves;

   //For thread-grain parallel warmup
   private boolean threadParallelLoad = false;
   private boolean deterministicLoad = false;
   private boolean diegoLoad = false;
   private boolean populateOnlyLocalWarehouses = false;

   private int numLoadersThread = 4;

   private int batchLevel = 100;

   /**
    * if true, means that the cache was already preload from a database. So, no population is needed.
    */
   private boolean preloadedFromDB = false;

   private boolean oneWarmup = false;

   public Map<String, String> stress(CacheWrapper wrapper) {
      if (wrapper == null) {
         throw new IllegalStateException("Null wrapper not allowed");
      }
      try {
         log.info("Performing Population Operations");
         performPopulationOperations(wrapper);
      } catch (Exception e) {
         log.warn("Received exception during cache population" + e.getMessage());
      }
      return null;
   }

   public void performPopulationOperations(CacheWrapper wrapper) throws Exception {

      TpccPopulation tpccPopulation;

      if (wrapper.isPassiveReplication()) {
         log.info("Performing passive-replication aware population...");
         tpccPopulation = new PassiveReplicationTpccPopulation(wrapper, numWarehouses, slaveIndex,
                                                               numSlaves, cLastMask, olIdMask,
                                                               cIdMask, (threadParallelLoad ? numLoadersThread : 1),
                                                               batchLevel);
      } else if (this.diegoLoad) {
         log.info("Loading with diego method");
         tpccPopulation = new Diego_DeterministicPopulation(wrapper, this.numWarehouses, this.slaveIndex,
                                                            this.numSlaves, this.cLastMask, this.olIdMask,
                                                            this.cIdMask, this.populateOnlyLocalWarehouses);
      } else if (this.threadParallelLoad) {
         log.info("Performing thread-parallel population...");
         tpccPopulation = new ThreadParallelTpccPopulation(wrapper, this.numWarehouses, this.slaveIndex,
                                                           this.numSlaves, this.cLastMask, this.olIdMask,
                                                           this.cIdMask, this.numLoadersThread, this.batchLevel);
      } else if (this.deterministicLoad) {
         log.info("Performing DETERMINISTIC thread-parallel population...");
         tpccPopulation = new DeterministicThreadParallelTpccPopulation(wrapper, this.numWarehouses, this.slaveIndex,
                                                                        this.numSlaves, this.cLastMask, this.olIdMask,
                                                                        this.cIdMask, this.batchLevel, this.populateOnlyLocalWarehouses);
      } else {
         log.info("Performing population...");
         tpccPopulation = new TpccPopulation(wrapper, this.numWarehouses, this.slaveIndex, this.numSlaves,
                                             this.cLastMask, this.olIdMask, this.cIdMask);
      }

      if (preloadedFromDB) {
         log.info("Skipping the population phase. The data was already preloaded from a DataBase");
         tpccPopulation.initTpccTools();
      } else if (!needToWarmup(wrapper)) {
         log.info("Skipping the population phase: population will happen via state transfer");
         tpccPopulation.initTpccTools();
      } else {
         tpccPopulation.performPopulation();
         if (oneWarmup) {
            setWarmedUp(wrapper);
         }
      }
      log.info("Population ended with " + wrapper.getCacheSize() + " elements!");

   }

   private boolean needToWarmup(CacheWrapper cacheWrapper) {
      if (!oneWarmup)
         return true;
      boolean ret = true;
      try {
         Object o;
         ret = ((o = cacheWrapper.get("", POPULATION_STRING)) == null);
         log.info("Checking if I have to warmup: returned value is " + o);
      } catch (Exception e) {
         log.error(e.getStackTrace());
      }
      return ret;
   }

   private void setWarmedUp(CacheWrapper cacheWrapper) {
      if (cacheWrapper.isPassiveReplication() && cacheWrapper.isTheMaster()
            || (!cacheWrapper.isPassiveReplication() && slaveIndex == 0)) {
         boolean sux = false;

         do {
            try {
               cacheWrapper.put("", POPULATION_STRING, "ALREADY_POPULATED");
               log.info("Writing " + POPULATION_STRING + " in the cache");
               sux = true;
            } catch (Exception e) {
               log.error(e.getStackTrace());
            }
         }
         while (!sux);
      }
   }


   @Override
   public String toString() {
      return "TpccPopulationStressor{" +
            "numWarehouses=" + numWarehouses +
            ", cLastMask=" + cLastMask +
            ", olIdMask=" + olIdMask +
            ", cIdMask=" + cIdMask +
            ", slaveIndex=" + slaveIndex +
            ", numSlaves=" + numSlaves +
            ", threadParallelLoad=" + threadParallelLoad +
            ", deterministicLoad=" + deterministicLoad +
            ", populateOnlyLocalWarehouses=" + populateOnlyLocalWarehouses +
            ", numLoadersThread=" + numLoadersThread +
            ", batchLevel=" + batchLevel +
            ", preloadedFromDB=" + preloadedFromDB +
            ", oneWarmup=" + oneWarmup +
            '}';
   }

   public void destroy() throws Exception {
      //Don't destroy data in cache!
   }

   public void setNumWarehouses(int numWarehouses) {
      this.numWarehouses = numWarehouses;
   }

   public void setSlaveIndex(int slaveIndex) {
      this.slaveIndex = slaveIndex;
   }

   public void setNumSlaves(int numSlaves) {
      this.numSlaves = numSlaves;
   }

   public void setCLastMask(long cLastMask) {
      this.cLastMask = cLastMask;
   }

   public void setOlIdMask(long olIdMask) {
      this.olIdMask = olIdMask;
   }

   public void setCIdMask(long cIdMask) {
      this.cIdMask = cIdMask;
   }

   public void setNumLoadersThread(int n) {
      this.numLoadersThread = n;
   }

   public void setThreadParallelLoad(boolean b) {
      this.threadParallelLoad = b;
   }

   public void setBatchLevel(int b) {
      this.batchLevel = b;
   }

   public void setPreloadedFromDB(boolean preloadedFromDB) {
      this.preloadedFromDB = preloadedFromDB;
   }

   public void setOneWarmup(boolean oneWarmup) {
      this.oneWarmup = oneWarmup;
   }

   public void setDeterministicLoad(boolean deterministicLoad) {
      this.deterministicLoad = deterministicLoad;
   }

   public void setPopulateOnlyLocalWarehouses(boolean populateOnlyLocalWarehouses) {
      this.populateOnlyLocalWarehouses = populateOnlyLocalWarehouses;
   }

   public void setDiegoLoad(boolean diegoLoad) {
      this.diegoLoad = diegoLoad;
   }
}
