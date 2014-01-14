package org.radargun.stages.synthetic.runtimeDap.prepareTest.MultiOwnerSingleOp;

import org.radargun.stages.synthetic.common.XactOp;
import org.radargun.stages.synthetic.common.synth.SyntheticXactParams;
import org.radargun.stages.synthetic.common.xactClass;
import org.radargun.stages.synthetic.runtimeDap.SyntheticXact_RunTimeDaP;

import java.util.Iterator;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class SyntheticXactMultiOwnerSingleOp_RunTimeDaP extends SyntheticXact_RunTimeDaP {

   public SyntheticXactMultiOwnerSingleOp_RunTimeDaP(SyntheticXactParams params) {
      super(params);
   }

   @Override
   protected Iterator<XactOp> iterator() {
      if (getClazz().equals(xactClass.RO))
         throw new IllegalArgumentException("No read only xacts while testing prepare!");
      return new IteratorRunTimeDapMultipleOwnersOneOp_UP(params, RWB);
   }

}
