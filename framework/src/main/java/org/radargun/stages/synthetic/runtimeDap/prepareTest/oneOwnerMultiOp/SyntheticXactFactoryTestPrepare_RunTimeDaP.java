package org.radargun.stages.synthetic.runtimeDap.prepareTest.oneOwnerMultiOp;

import org.radargun.stages.synthetic.common.XACT_RETRY;
import org.radargun.stages.synthetic.common.synth.SyntheticXactParams;
import org.radargun.stages.synthetic.runtimeDap.SyntheticXactFactory_RunTimeDaP;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class SyntheticXactFactoryTestPrepare_RunTimeDaP extends SyntheticXactFactory_RunTimeDaP {

   public SyntheticXactFactoryTestPrepare_RunTimeDaP(SyntheticXactParams params) {
      super(params);
      log.fatal("Be sure to have\n1) If blindWrites-->numReadsPerUpdate == 0\n2) ReadsBeforeFirstWrite=0");
   }

   protected void sanityCheck() {
      if (params.getXact_retry().equals(XACT_RETRY.RETRY_SAME_XACT))
         throw new IllegalArgumentException("For now, runTimeDap transactions cannot be exactly retried. Only no_retry or same_class is allowed");
      //TODO I just have to do the following: populate at runtime the XactOps, so that it can be injected. I just have to know if I am
      //TODO a retried transaction, so that I can use the XactOps instead of computing it at runtime

   }


   @Override
   protected SyntheticXactTestPrepare_RunTimeDaP generateXact(SyntheticXactParams p) {
      SyntheticXactTestPrepare_RunTimeDaP s = new SyntheticXactTestPrepare_RunTimeDaP(p);
      s.setRoRead(roRead);
      s.setUpRead(upRead);
      s.setUpWrite(upWrite);
      s.setParams(p);
      s.setRWB(rwB);
      return s;
   }

}
