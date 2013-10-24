package org.radargun.stages.synthetic;

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
