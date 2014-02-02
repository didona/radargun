package org.radargun.stressors;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class ContentionYCSBStringKeyGenerator extends ContentionStringKeyGenerator {

   @Override
   public Object generateKey(int threadIndex, int keyIndex) {
      return "user" + keyIndex;
   }

   @Override
   public Object generateKey(int nodeIndex, int threadIndex, int keyIndex) {
      return "user" + keyIndex;
   }
}
