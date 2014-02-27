package org.radargun.ycsb.transaction.diego;

import org.radargun.ycsb.generators.IntegerGenerator;
import org.radargun.ycsb.transaction.YCSBTransaction;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public abstract class D_YCSBTransaction extends YCSBTransaction {

   protected IntegerGenerator ig;

   protected D_YCSBTransaction(IntegerGenerator ig) {
      this.ig = ig;
   }

   public IntegerGenerator getIg() {
      return ig;
   }

   public void setIg(IntegerGenerator ig) {
      this.ig = ig;
   }
}
