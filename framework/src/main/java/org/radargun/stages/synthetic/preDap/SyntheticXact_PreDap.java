package org.radargun.stages.synthetic.preDap;

import org.radargun.stages.synthetic.common.synth.SyntheticXact;
import org.radargun.stages.synthetic.common.synth.SyntheticXactParams;
import org.radargun.stages.synthetic.common.XactOp;

import java.util.Iterator;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class SyntheticXact_PreDap extends SyntheticXact {

   public SyntheticXact_PreDap(SyntheticXactParams p) {
      super(p);
   }

   @Override
   protected Iterator<XactOp> iterator() {
      return new IteratorPreDap(this.ops);
   }
}
