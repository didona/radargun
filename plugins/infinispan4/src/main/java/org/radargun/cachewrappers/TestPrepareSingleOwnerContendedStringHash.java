package org.radargun.cachewrappers;

import org.infinispan.commons.hash.Hash;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.marshall.AbstractExternalizer;
import org.infinispan.remoting.transport.Address;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class TestPrepareSingleOwnerContendedStringHash implements ConsistentHash {

   private Hash thisHash;
   private int numNodes;
   private int numSegments;
   private List<Address> members;
   private final static Log log = LogFactory.getLog(TestPrepareSingleOwnerContendedStringHash.class);
   private final static boolean trace = log.isTraceEnabled();

   public TestPrepareSingleOwnerContendedStringHash() {
      log.fatal("Creating " + this);
   }

   public TestPrepareSingleOwnerContendedStringHash(Hash thisHash, int numOwners, int numSegments, List<Address> members) {
      this.thisHash = thisHash;
      ((TestPrepareHashFunction) thisHash).setNumNodes(members.size());
      this.numNodes = members.size();
      this.numSegments = numSegments;
      this.members = members;
      log.fatal("Creating " + this);
   }

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
      int index = this.thisHash.hash(o);
      Address r = this.members.get(index);
      if (trace)
         log.trace("Owner for " + o + " is " + index + " i.e. " + r);
      return r;
   }

   @Override
   public List<Address> locateOwners(Object o) {
      List<Address> ret = new ArrayList<Address>();
      ret.add(locatePrimaryOwner(o));
      if (trace)
         log.trace("Owner for " + o + " are " + ret);
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

   public Hash getThisHash() {
      return thisHash;
   }

   public int getNumNodes() {
      return numNodes;
   }

   @Override
   public String toString() {
      return "TestPrepareSingleOwnerContendedStringHash{" +
              "thisHash=" + thisHash +
              ", numNodes=" + numNodes +
              ", numSegments=" + numSegments +
              ", members=" + members.toString() +
              '}';
   }

   public static class Externalizer extends AbstractExternalizer<TestPrepareSingleOwnerContendedStringHash> {

      @Override
      public void writeObject(ObjectOutput output, TestPrepareSingleOwnerContendedStringHash ch) throws IOException {
         output.writeInt(ch.numSegments);
         output.writeObject(ch.members);
         output.writeObject(ch.thisHash);
      }

      @Override
      @SuppressWarnings("unchecked")
      public TestPrepareSingleOwnerContendedStringHash readObject(ObjectInput unmarshaller) throws IOException, ClassNotFoundException {
         int numSegments = unmarshaller.readInt();
         //int numOwners = unmarshaller.readInt();
         List<Address> members = (List<Address>) unmarshaller.readObject();
         Hash hash = (Hash) unmarshaller.readObject();
         //List<Address>[] owners = (List<Address>[]) unmarshaller.readObject();

         return new TestPrepareSingleOwnerContendedStringHash(hash, numSegments, numSegments, members);
      }

      @Override
      public Integer getId() {
         return 253;
      }

      @Override
      public Set<Class<? extends TestPrepareSingleOwnerContendedStringHash>> getTypeClasses() {
         return Collections.<Class<? extends TestPrepareSingleOwnerContendedStringHash>>singleton(TestPrepareSingleOwnerContendedStringHash.class);
      }
   }
}
