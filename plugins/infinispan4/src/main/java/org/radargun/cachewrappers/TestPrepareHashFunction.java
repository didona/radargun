package org.radargun.cachewrappers;

import org.infinispan.commons.hash.Hash;
import org.infinispan.marshall.exts.NoStateExternalizer;
import org.infinispan.util.Util;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;
import org.radargun.stressors.TestPrepareContentionStringKeyGenerator;

import java.io.ObjectInput;
import java.util.Set;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class TestPrepareHashFunction implements Hash {

   private int numNodes;
   private final static Log log = LogFactory.getLog(TestPrepareHashFunction.class);

   public void setNumNodes(int numNodes) {
      this.numNodes = numNodes;
   }

   public TestPrepareHashFunction(int numNodes) {
      this.numNodes = numNodes;
   }

   public TestPrepareHashFunction() {
   }

   @Override
   public int hash(byte[] bytes) {
      log.fatal("No byte");
      throw new IllegalArgumentException("This works only with properly formatted String keys");
   }

   @Override
   public int hash(int i) {
      log.fatal("No int");
      throw new IllegalArgumentException("This works only with properly formatted String keys");
   }

   @Override
   public int hash(Object o) {
      if (o instanceof String)
         return TestPrepareContentionStringKeyGenerator.keyIndex((String) o) % numNodes;
      throw new IllegalArgumentException("This works only with properly formatted String keys");
   }

   public static class Externalizer extends NoStateExternalizer<TestPrepareHashFunction> {
      @Override
      public Set<Class<? extends TestPrepareHashFunction>> getTypeClasses() {
         return Util.<Class<? extends TestPrepareHashFunction>>asSet(TestPrepareHashFunction.class);
      }

      @Override
      public TestPrepareHashFunction readObject(ObjectInput input) {
         return new TestPrepareHashFunction();
      }

      @Override
      public Integer getId() {
         return 252;
      }
   }

   @Override
   public String toString() {
      return "TestPrepareHashFunction{" +
              "numNodes=" + numNodes +
              '}';
   }
}
