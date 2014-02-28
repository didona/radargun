package org.radargun.ycsb.transaction.diego;

import org.radargun.CacheWrapper;
import org.radargun.ycsb.ByteIterator;
import org.radargun.ycsb.RandomByteIterator;
import org.radargun.ycsb.StringByteIterator;
import org.radargun.ycsb.YCSB;
import org.radargun.ycsb.generators.IntegerGenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class D_Read_N extends D_Read {
   private Set<Integer> alreadyRead = new HashSet<Integer>();
   private int numOps;

   public D_Read_N(IntegerGenerator ig, int numOps) {
      super(ig);
      this.numOps = numOps;
   }

   @Override
   public void executeTransaction(CacheWrapper cacheWrapper) throws Throwable {
      int k;
      HashMap<String, ByteIterator> values = new HashMap<String, ByteIterator>();
      for (int j = 0; j < numOps; j++) {

         do {k = ig.nextInt(); }
         while (alreadyRead.contains(k));
         alreadyRead.add(k);

         //TODO:What is this for?
         for (int i = 0; i < YCSB.fieldcount; i++) {
            String fieldkey = "field" + i;
            ByteIterator data = new RandomByteIterator(YCSB.fieldlengthgenerator.nextInt());
            values.put(fieldkey, data);
         }
         Map<String, String> row = StringByteIterator.getStringMap(values);

         cacheWrapper.get(null, "user" + k);
      }

   }
}
