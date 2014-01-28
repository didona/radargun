package org.radargun.ycsb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radargun.CacheWrapper;
import org.radargun.stages.synthetic.common.XACT_RETRY;
import org.radargun.stamp.vacation.VacationStressor;
import org.radargun.stressors.AbstractCacheWrapperStressor;
import org.radargun.ycsb.generators.IntegerGenerator;
import org.radargun.ycsb.transaction.RMW;
import org.radargun.ycsb.transaction.RMW_IG;
import org.radargun.ycsb.transaction.Read;
import org.radargun.ycsb.transaction.YCSBTransaction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class YCSBStressor extends AbstractCacheWrapperStressor implements Runnable {

   private static Log log = LogFactory.getLog(VacationStressor.class);

   public static final int TEST_PHASE = 2;
   public static final int SHUTDOWN_PHASE = 3;

   private XACT_RETRY retryMode = XACT_RETRY.RETRY_SAME_CLASS;

   volatile protected int m_phase = TEST_PHASE;

   private CacheWrapper cacheWrapper;
   private int multiplereadcount;
   private int recordCount;


   private int numWrites = 1;
   private long restarts = 0;
   private long throughput = 0;

   private boolean allowBlindWrites = true;

   //The random should *not* be shared among threads!!
   protected Random r = new Random();
   private IntegerGenerator ig = null;

   public void setCacheWrapper(CacheWrapper cacheWrapper) {
      this.cacheWrapper = cacheWrapper;
   }

   @Override
   public void run() {
      stress(cacheWrapper);
   }

   private YCSBTransaction generateNextTransaction() {
      int ran = (Math.abs(r.nextInt())) % 100;
      int keynum = (Math.abs(r.nextInt())) % recordCount;
      if (ran < YCSB.readOnly) {
         return new Read(keynum);
      } else {
         if (ig != null) {
            return new RMW_IG(keynum, Math.abs(r.nextInt() % numWrites), numWrites, multiplereadcount, recordCount, allowBlindWrites, ig);
         }
         return new RMW(keynum, Math.abs(r.nextInt()), multiplereadcount, recordCount, allowBlindWrites);
      }
   }

   @Override
   public Map<String, String> stress(CacheWrapper wrapper) {
      this.cacheWrapper = wrapper;

      while (m_phase == TEST_PHASE) {
         processTransaction(wrapper, generateNextTransaction());
         this.throughput++;
      }

      Map<String, String> results = new LinkedHashMap<String, String>();

      return results;
   }

   private void processTransaction(CacheWrapper wrapper, YCSBTransaction transaction) {
      boolean successful = false;

      while (!successful) {
         if (m_phase != TEST_PHASE) {
            this.throughput--;
            return;
         }
         successful = true;
         cacheWrapper.startTransaction(transaction.isReadOnly());
         try {
            transaction.executeTransaction(cacheWrapper);
         } catch (Throwable e) {
            successful = false;
         }
         try {
            cacheWrapper.endTransaction(successful);
            if (!successful) {
               setRestarts(getRestarts() + 1);
            }
         } catch (Throwable rb) {
            setRestarts(getRestarts() + 1);
            successful = false;
         }
         if (retryMode == XACT_RETRY.NO_RETRY)
            break;
      }
   }

   @Override
   public void destroy() throws Exception {

   }

   public int getMultiplereadcount() {
      return multiplereadcount;
   }

   public void setMultiplereadcount(int multiplereadcount) {
      this.multiplereadcount = multiplereadcount;
   }

   public int getRecordCount() {
      return recordCount;
   }

   public void setRecordCount(int recordcount) {
      this.recordCount = recordcount;
   }

   public long getRestarts() {
      return restarts;
   }

   public void setRestarts(long restarts) {
      this.restarts = restarts;
   }

   public long getThroughput() {
      return throughput;
   }

   public void setThroughput(long throughput) {
      this.throughput = throughput;
   }

   public void setPhase(int shutdownPhase) {
      this.m_phase = shutdownPhase;
   }

   public boolean isAllowBlindWrites() {
      return allowBlindWrites;
   }

   public void setAllowBlindWrites(boolean allowBlindWrites) {
      this.allowBlindWrites = allowBlindWrites;
   }

   public IntegerGenerator getIg() {
      return ig;
   }

   public void setIg(IntegerGenerator ig) {
      this.ig = ig;
   }

   public int getNumWrites() {
      return numWrites;
   }

   public void setNumWrites(int numWrites) {
      this.numWrites = numWrites;
   }

   public XACT_RETRY getRetryMode() {
      return retryMode;
   }

   public void setRetryMode(String retryMode) {
      this.retryMode = XACT_RETRY.valueOf(retryMode);
      if (this.retryMode == XACT_RETRY.RETRY_SAME_XACT)
         throw new IllegalArgumentException(this.retryMode + " not supported yet");
   }

   /**
    * private void processTransaction(CacheWrapper wrapper, YCSBTransaction transaction) {
    boolean successful = true;

    while (true) {
    if (m_phase != TEST_PHASE) {
    this.throughput--;
    break;
    }
    cacheWrapper.startTransaction(transaction.isReadOnly());
    try {
    transaction.executeTransaction(cacheWrapper);
    } catch (Throwable e) {
    successful = false;
    }
    try {
    cacheWrapper.endTransaction(successful);

    if (!successful) {
    setRestarts(getRestarts() + 1);
    }
    } catch (Throwable rb) {
    setRestarts(getRestarts() + 1);
    successful = false;
    }

    if (!successful) {
    successful = true;
    } else {
    break;
    }
    }
    }
    */
}
