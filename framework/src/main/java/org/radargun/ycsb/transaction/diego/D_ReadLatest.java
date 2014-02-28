package org.radargun.ycsb.transaction.diego;

import org.radargun.ycsb.generators.IntegerGenerator;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class D_ReadLatest extends D_Read {
   public D_ReadLatest(IntegerGenerator ig) {
      super(ig);
   }

   @Override
   protected int nextKey() {
      return ig.lastInt();
   }
}
