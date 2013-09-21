package org.radargun.stages.synthetic;

import org.radargun.CacheWrapper;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class SyntheticXact_PreDap extends SyntheticXact {

   public SyntheticXact_PreDap(CacheWrapper wrapper) {
      super(wrapper);
   }

   public void executeLocally() throws Exception {
      for (XactOp op : ops) {
         if (op.isPut())
            cache.put(null, op.getKey(), op.getValue());
         else
            cache.get(null, op.getKey());
      }
   }

}
