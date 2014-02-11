package org.radargun.stages;

import org.radargun.DistStageAck;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
@Deprecated
public class InitTestPrepareHashStage extends AbstractDistStage {
   private int numKeys;

   public void setNumKeys(int numKeys) {
      this.numKeys = numKeys;
   }

   @Override
   public DistStageAck executeOnSlave() {
      DefaultDistStageAck defaultDistStageAck = newDefaultStageAck();
      long start = System.currentTimeMillis();
      log.info("InitTestPrepareHashStage: INIT");
      slaveState.getCacheWrapper().initHashIfNecessary();
      log.info("InitTestPrepareHashStage: END");
      long duration = System.currentTimeMillis() - start;
      defaultDistStageAck.setDuration(duration);
      return defaultDistStageAck;
   }
}
