package org.radargun.stressors;

import org.radargun.ycsb.ByteIterator;
import org.radargun.ycsb.RandomByteIterator;
import org.radargun.ycsb.StringByteIterator;
import org.radargun.ycsb.YCSB;

import java.util.HashMap;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */

/**
 * This resembles the Insert YCSB transactional class
 */
public class YCSBWarmupOnlyPrimaryStressor extends SyntheticWarmupOnlyPrimaryStressor {
   @Override
   protected Object payload(Object key) {
      HashMap<String, ByteIterator> values = new HashMap<String, ByteIterator>();

      for (int i = 0; i < YCSB.fieldcount; i++) {
         String fieldkey = "field" + i;
         ByteIterator data = new RandomByteIterator(YCSB.fieldlengthgenerator.nextInt());
         values.put(fieldkey, data);
      }
      return StringByteIterator.getStringMap(values);

   }
}
