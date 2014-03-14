package org.radargun.ycsb.transaction.diego;

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
public class D_Update extends D_YCSBTransaction {

   public D_Update(IntegerGenerator ig) {
      super(ig);
   }

   @Override
   public void executeTransaction(CacheWrapper cacheWrapper) throws Throwable {
      HashMap<String, ByteIterator> values = new HashMap<String, ByteIterator>();
      int k = ig.nextInt();

      for (int i = 0; i < YCSB.fieldcount; i++) {
         String fieldkey = "field" + i;
         ByteIterator data = new RandomByteIterator(YCSB.fieldlengthgenerator.nextInt());
         values.put(fieldkey, data);
      }
      Map<String, String> row = StringByteIterator.getStringMap(values);
      cacheWrapper.put(null, "user" + k, row);
   }

   @Override
   public boolean isReadOnly() {
      return false;
   }
}