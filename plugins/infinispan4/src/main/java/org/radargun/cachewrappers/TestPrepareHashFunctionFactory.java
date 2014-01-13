package org.radargun.cachewrappers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.commons.hash.Hash;
import org.infinispan.distribution.ch.ConsistentHashFactory;
import org.infinispan.marshall.AbstractExternalizer;
import org.infinispan.remoting.transport.Address;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class TestPrepareHashFunctionFactory implements ConsistentHashFactory<TestPrepareSingleOwnerContendedStringHash> {
   private static final Log log = LogFactory.getLog(TestPrepareHashFunctionFactory.class);
   private static final int defaultOwner = 1;

   @Override
   public TestPrepareSingleOwnerContendedStringHash create(Hash hash, int numOwners, int numSegments, List<Address> addresses) {
      log.fatal("Creating new with " + hash.getClass() + " " + addresses.toString());
      return new TestPrepareSingleOwnerContendedStringHash(hash, defaultOwner, numSegments, addresses);
   }

   @Override
   public TestPrepareSingleOwnerContendedStringHash updateMembers(TestPrepareSingleOwnerContendedStringHash testPrepareSingleOwnerContendedStringHash, List<Address> addresses) {
      log.fatal("Updating with members " + testPrepareSingleOwnerContendedStringHash.getThisHash().getClass() + " " + addresses.toString());
      return new TestPrepareSingleOwnerContendedStringHash(testPrepareSingleOwnerContendedStringHash.getThisHash(), defaultOwner, addresses.size(), addresses);
   }

   /**
    * I guess this has been added by Pedro to support deltas in the mapping
    *
    * @param testPrepareSingleOwnerContendedStringHash
    * @param o
    * @return
    */
   @Override
   public TestPrepareSingleOwnerContendedStringHash rebalance(TestPrepareSingleOwnerContendedStringHash testPrepareSingleOwnerContendedStringHash, Object o) {
      log.fatal("Rebalancing " + testPrepareSingleOwnerContendedStringHash + " against " + o);
      return testPrepareSingleOwnerContendedStringHash;
   }

   /**
    * This hash function is not supposed to work with changing number of nodes, so the two hash should be the same
    *
    * @param testPrepareSingleOwnerContendedStringHash
    * @param testPrepareSingleOwnerContendedStringHash2
    * @return
    */
   @Override
   public TestPrepareSingleOwnerContendedStringHash union(TestPrepareSingleOwnerContendedStringHash testPrepareSingleOwnerContendedStringHash, TestPrepareSingleOwnerContendedStringHash testPrepareSingleOwnerContendedStringHash2) {
      List<Address> one = testPrepareSingleOwnerContendedStringHash.getMembers();
      List<Address> two = testPrepareSingleOwnerContendedStringHash2.getMembers();
      for (Address a : one) {
         if (!two.contains(a))
            two.add(a);
      }
      TestPrepareSingleOwnerContendedStringHash newH = new TestPrepareSingleOwnerContendedStringHash(testPrepareSingleOwnerContendedStringHash.getThisHash(), defaultOwner, two.size(), two);
      log.fatal("Union between " + testPrepareSingleOwnerContendedStringHash + " and " + testPrepareSingleOwnerContendedStringHash2 + " returning " + newH);
      return newH;
   }

   public static class Externalizer extends AbstractExternalizer<TestPrepareHashFunctionFactory> {

      @Override
      public void writeObject(ObjectOutput output, TestPrepareHashFunctionFactory chf) throws IOException {
      }

      @Override
      @SuppressWarnings("unchecked")
      public TestPrepareHashFunctionFactory readObject(ObjectInput unmarshaller) throws IOException, ClassNotFoundException {
         return new TestPrepareHashFunctionFactory();
      }

      @Override
      public Integer getId() {
         return 254;
      }

      @Override
      public Set<Class<? extends TestPrepareHashFunctionFactory>> getTypeClasses() {
         return Collections.<Class<? extends TestPrepareHashFunctionFactory>>singleton(TestPrepareHashFunctionFactory.class);
      }
   }
}
