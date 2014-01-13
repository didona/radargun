package org.radargun.cachewrappers;

import org.infinispan.commons.hash.Hash;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.remoting.transport.Address;
import org.radargun.stressors.TestPrepareContentionStringKeyGenerator;

import java.util.*;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class TestPrepareSingleOwnerContendedStringHash implements ConsistentHash {

   private int numNodes;
   private Hash thisHash = new TestPrepareHasFunction();


   private List<Address> members;

   public void onAllMembersJoin(List<Address> members) {
      this.members = members;
      this.numNodes = this.members.size();
   }

   @Override
   public int getNumOwners() {
      return 1;
   }

   @Override
   public Hash getHashFunction() {
      return this.thisHash;
   }

   @Override
   public int getNumSegments() {
      return this.numNodes;
   }

   @Override
   public List<Address> getMembers() {
      return this.members;
   }

   @Override
   public Address locatePrimaryOwner(Object o) {
      return this.members.get(this.thisHash.hash(o));
   }

   @Override
   public List<Address> locateOwners(Object o) {
      List<Address> ret = new ArrayList<Address>();
      ret.add(locatePrimaryOwner(o));
      return ret;
   }

   @Override
   public Set<Address> locateAllOwners(Collection<Object> objects) {
      Set<Address> ret = new HashSet<Address>();
      for (Object o : objects) {
         ret.addAll(locateOwners(o));
      }
      return ret;
   }

   @Override
   public boolean isKeyLocalToNode(Address address, Object o) {
      return locatePrimaryOwner(o).equals(address);
   }

   @Override
   public int getSegment(Object o) {
      return thisHash.hash(o);
   }

   @Override
   public List<Address> locateOwnersForSegment(int i) {
      List<Address> ret = new ArrayList<Address>();
      ret.add(members.get(i));
      return ret;
   }

   @Override
   public Address locatePrimaryOwnerForSegment(int i) {
      return members.get(i);
   }

   @Override
   public Set<Integer> getSegmentsForOwner(Address address) {
      Set<Integer> s = new HashSet<Integer>();
      s.add(getMemberByAddress(address));
      return s;
   }

   private int getMemberByAddress(Address a) {
      int i = 0;
      for (Address aa : members) {
         if (aa.equals(a))
            return i;
         i++;
      }
      return i;
   }

   @Override
   public String getRoutingTableAsString() {
      return null;  // TODO: Customise this generated block
   }

   /**
    * This class expects Contended String Keys
    */
   private class TestPrepareHasFunction implements Hash {

      @Override
      public int hash(byte[] bytes) {
         throw new IllegalArgumentException("This works only with properly formatted String keys");
      }

      @Override
      public int hash(int i) {
         throw new IllegalArgumentException("This works only with properly formatted String keys");
      }

      @Override
      public int hash(Object o) {
         if (o instanceof String)
            return TestPrepareContentionStringKeyGenerator.keyIndex((String) o) % numNodes;
         throw new IllegalArgumentException("This works only with properly formatted String keys");
      }
   }
}
