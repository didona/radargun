package org.radargun.stages;

import org.radargun.stressors.YCSBWarmupOnlyPrimaryStressor;
import org.radargun.ycsb.YCSB;

import java.util.Map;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class YCSBWarmupStage extends SyntheticWarmupStage {

   protected Map<String, String> doWork() {
      log.fatal("Pre-initing YCSB. ALSO on the slaves!");
      YCSB.preinit();
      if (!cacheWrapper.isCoordinator()) {
         log.fatal("I am a slave, thus I do not populate. Bye");
         return null;
      }
      log.info("Starting " + getClass().getSimpleName() + ": " + this);
      YCSBWarmupOnlyPrimaryStressor putGetStressor = new YCSBWarmupOnlyPrimaryStressor();
      putGetStressor.setNodeIndex(getSlaveIndex());
      putGetStressor.setNumberOfAttributes(numberOfAttributes);
      putGetStressor.setNumberOfRequests(numberOfRequests);
      putGetStressor.setNumOfThreads(numOfThreads);
      putGetStressor.setOpsCountStatusLog(opsCountStatusLog);
      putGetStressor.setSizeOfAnAttribute(sizeOfAnAttribute);
      putGetStressor.setWritePercentage(writePercentage);
      putGetStressor.setKeyGeneratorClass(keyGeneratorClass);
      putGetStressor.setUseTransactions(useTransactions);
      putGetStressor.setCommitTransactions(commitTransactions);
      putGetStressor.setTransactionSize(transactionSize);
      putGetStressor.setDurationMillis(-1);
      return putGetStressor.stress(cacheWrapper);
   }
}
