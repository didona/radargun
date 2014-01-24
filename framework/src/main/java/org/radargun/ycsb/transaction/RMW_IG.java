package org.radargun.ycsb.transaction;

import org.radargun.CacheWrapper;
import org.radargun.ycsb.ByteIterator;
import org.radargun.ycsb.RandomByteIterator;
import org.radargun.ycsb.StringByteIterator;
import org.radargun.ycsb.YCSB;
import org.radargun.ycsb.generators.IntegerGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class RMW_IG extends RMW {

   private IntegerGenerator integerGenerator;

   public RMW_IG(int k, int random, int multiplereadcount, int recordCount, boolean blindWrites, IntegerGenerator integerGenerator) {
      super(k, random, multiplereadcount, recordCount, blindWrites);
      this.integerGenerator = integerGenerator;
   }

   @Override
   public void executeTransaction(CacheWrapper cacheWrapper) throws Throwable {
      HashMap<String, ByteIterator> values = new HashMap<String, ByteIterator>();

      for (int i = 0; i < YCSB.fieldcount; i++) {
         String fieldkey = "field" + i;
         ByteIterator data = new RandomByteIterator(YCSB.fieldlengthgenerator.nextInt());
         values.put(fieldkey, data);
      }

      Map<String, String> row = StringByteIterator.getStringMap(values);
      int toWrite = (Math.abs(random)) % multiplereadcount;
      int next;
      for (int i = 0; i < multiplereadcount; i++) {
         next = integerGenerator.nextInt();
         if (toWrite == i) {
            //If we do not want blind writes, then we have to read the user and then write to it
            if (!blindWrites) {
               cacheWrapper.get(null, "user" + next);
            }
            cacheWrapper.put(null, "user" + next, row);
         } else {
            cacheWrapper.get(null, "user" + next);
         }
      }

   }
}
