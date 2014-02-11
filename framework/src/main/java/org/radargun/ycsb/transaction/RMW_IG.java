package org.radargun.ycsb.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radargun.CacheWrapper;
import org.radargun.stressors.ContentionYCSBStringKeyGenerator;
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
   private int numWrites;
   private ContentionYCSBStringKeyGenerator generator = new ContentionYCSBStringKeyGenerator();
   private final static Log log = LogFactory.getLog(RMW_IG.class);
   private final static boolean trace = log.isTraceEnabled();

   public RMW_IG(int k, int random, int multiplereadcount, int numW, int recordCount, boolean blindWrites, IntegerGenerator integerGenerator) {
      super(k, random, multiplereadcount, recordCount, blindWrites);
      this.integerGenerator = integerGenerator;
      this.numWrites = numW;
   }

   @Override
   //TODO: we may choose to write different things for different put operations
   public void executeTransaction(CacheWrapper cacheWrapper) throws Throwable {
      HashMap<String, ByteIterator> values = new HashMap<String, ByteIterator>();
      int temp;
      for (int i = 0; i < YCSB.fieldcount; i++) {
         String fieldkey = "field" + i;
         if (trace)
            log.trace("FieldKey " + fieldkey);
         temp = YCSB.fieldlengthgenerator.nextInt();
         if (trace)
            log.trace("length " + temp);
         ByteIterator data = new RandomByteIterator(temp);
         values.put(fieldkey, data);
      }

      Map<String, String> row = StringByteIterator.getStringMap(values);
      //int toWrite = (Math.abs(random)) % multiplereadcount;
      int next;
      boolean remainder = multiplereadcount % numWrites != 0;
      int readBetweenWrites = (int) Math.ceil((double) multiplereadcount / (double) numWrites);
      boolean toWriteB;
      Object key;
      for (int i = 1; i <= multiplereadcount; i++) {
         /**
          * If I had the remainder in the first place, I have to write at the last one
          * 10 reads, 4 writes==> every 3 reads do a write plus at the last one  (3,6,9,10)
          * If I had not the ramainder, just write according to the modulo
          * 10 reads, 5 writes==> every 2 reads do a write (2 4 6 8 10)
          * Of course you have to start from 1 ;)
          */

         toWriteB = ((i % readBetweenWrites) == 0) || (i == multiplereadcount && remainder);
         next = integerGenerator.nextInt();
         key = generator.generateKey(0, next);
         if (toWriteB) {
            //If we do not want blind writes, then we have to read the user and then write to it
            if (!blindWrites) {
               if (trace)
                  log.trace("Read " + key);
               cacheWrapper.get(null, key);
            }
            if (trace)
               log.trace("Put " + key);
            cacheWrapper.put(null, key, row);
         } else {
            if (trace)
               log.trace("Read " + key);
            cacheWrapper.get(null, key);
         }
      }

   }
}
