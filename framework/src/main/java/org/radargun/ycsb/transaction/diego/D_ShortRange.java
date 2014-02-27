package org.radargun.ycsb.transaction.diego;

import org.radargun.CacheWrapper;
import org.radargun.ycsb.generators.IntegerGenerator;
import org.radargun.ycsb.generators.UniformIntegerGenerator;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class D_ShortRange extends D_YCSBTransaction {
   private IntegerGenerator lengthIg;
   private int recordCount;

   public D_ShortRange(IntegerGenerator keyIg, IntegerGenerator lengthIg, int recordCount) {
      super(keyIg);
      this.lengthIg = lengthIg;
      this.recordCount = recordCount;
   }

   public D_ShortRange(IntegerGenerator keyIg, int recordCount) {
      super(keyIg);
      this.lengthIg = new UniformIntegerGenerator(1, 100);   //Default: scan one to 100 elements, ~U(1,100)
      this.recordCount = recordCount;
   }

   @Override
   public void executeTransaction(CacheWrapper cacheWrapper) throws Throwable {
      int toRead = lengthIg.nextInt();
      int startK = ig.nextInt();
      for (int i = 0; i < toRead; i++) {
         cacheWrapper.get(null, "user" + ((startK + i) % recordCount));
      }
   }


   @Override
   public boolean isReadOnly() {
      return true;
   }
}
