package org.radargun.stressors;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 * Keys are of the form key_node0_thread0_K with K in [0...numkeys-1]
 * The application asks a key for a given node, namely "give me key Y for node Z"
 * This generator translates Y and Z into K
 * K = xZ + Y
 * where x is numKeys / numNodes, i.e., the number of keys per node.
 * This is because the TestPrepareSingleOwnerHash maps key K to node K%numNodes
 */
public class TestPrepareContentionStringKeyGenerator extends ContentionStringKeyGenerator {

   private int keysPerNode;
   private final static Log log = LogFactory.getLog(TestPrepareContentionStringKeyGenerator.class);
   private final static boolean trace = log.isTraceEnabled();

   public TestPrepareContentionStringKeyGenerator(int numNodes, int numKeys) {
      int remainder = numKeys % numNodes;
      int actualKeys = numKeys - remainder;
      this.keysPerNode = (int) ((double) actualKeys / (double) numNodes);
      if (trace)
         log.trace(this);
   }

   public TestPrepareContentionStringKeyGenerator() {
   }

   @Override
   public Object generateKey(int threadIndex, int keyIndex) {
      throw new IllegalArgumentException("We only want keys like key_node_thread_index");
   }

   @Override
   public Object generateKey(int nodeIndex, int threadIndex, int keyIndex) {

      int actualKey = keysPerNode * keyIndex + nodeIndex;
      return super.generateKey(CONTEND, CONTEND, actualKey);
   }

   public static int keyIndex(String s) {
      String[] split = s.split("_");
      return Integer.parseInt(split[split.length - 1]);
   }

   @Override
   public String toString() {
      return "TestPrepareContentionStringKeyGenerator{" +
              "keysPerNode=" + keysPerNode +
              '}';
   }
}
