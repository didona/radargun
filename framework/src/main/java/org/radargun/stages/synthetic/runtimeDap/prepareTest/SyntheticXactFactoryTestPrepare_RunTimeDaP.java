package org.radargun.stages.synthetic.runtimeDap.prepareTest;

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
