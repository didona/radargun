package org.radargun.stages.synthetic.runtimeDap;

import org.radargun.stages.synthetic.common.XACT_RETRY;
import org.radargun.stages.synthetic.common.XactOp;
import org.radargun.stages.synthetic.common.synth.SyntheticDistinctXactFactory;
import org.radargun.stages.synthetic.common.synth.SyntheticXactParams;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class SyntheticXactFactory_RunTimeDaP extends SyntheticDistinctXactFactory<SyntheticXactParams, SyntheticXact_RunTimeDaP> {

   protected int roRead;
   protected int upRead;
   protected int upWrite;

   public SyntheticXactFactory_RunTimeDaP(SyntheticXactParams params) {
      super(params);
      roRead = params.getROGets();
      upRead = params.getUpReads();
      upWrite = params.getUpPuts();
      sanityCheck();

   }

   protected void sanityCheck() {
      if (upRead < upWrite)
         throw new IllegalArgumentException("For now, numWrites has to be <= numReads");
      if (params.getXact_retry().equals(XACT_RETRY.RETRY_SAME_XACT))
         throw new IllegalArgumentException("For now, runTimeDap transactions cannot be exactly retried. Only no_retry or same_class is allowed");
      //TODO I just have to do the following: populate at runtime the XactOps, so that it can be injected. I just have to know if I am
      //TODO a retried transaction, so that I can use the XactOps instead of computing it at runtime

   }

   @Override
   protected SyntheticXact_RunTimeDaP generateXact(SyntheticXactParams p) {
      SyntheticXact_RunTimeDaP s = new SyntheticXact_RunTimeDaP(p);
      s.setRoRead(roRead);
      s.setUpRead(upRead);
      s.setUpWrite(upWrite);
      s.setParams(p);
      s.setRWB(rwB);
      return s;
   }

   @Override
   protected final XactOp[] buildReadSet() {
      return null;
   }

   @Override
   protected final XactOp[] buildReadWriteSet() {
      return null;
   }
}
