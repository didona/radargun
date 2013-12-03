package org.radargun.stages;

import org.radargun.CacheWrapper;
import org.radargun.DistStageAck;

/**
 * @author Diego Didona
 * @email didona@gsd.inesc-id.pt
 */
public class DumpHistogramStage extends AbstractDistStage {

   @Override
   public DistStageAck executeOnSlave() {
      DefaultDistStageAck defaultDistStageAck = newDefaultStageAck();
      CacheWrapper cacheWrapper = slaveState.getCacheWrapper();

      if (cacheWrapper == null) {
         log.info("Not dumping histograms on this slave as the wrapper hasn't been configured.");
         return defaultDistStageAck;
      }

      long start = System.currentTimeMillis();
      cacheWrapper.dumpHistograms();
      long duration = System.currentTimeMillis() - start;
      defaultDistStageAck.setDuration(duration);
      return defaultDistStageAck;
   }

   @Override
   public String toString() {
      return "DumpHistograms{" + super.toString();
   }
}
