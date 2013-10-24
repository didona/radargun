package org.radargun.stages.synthetic.preDap;

import org.radargun.stages.synthetic.common.synth.SyntheticDistinctXactFactory;
import org.radargun.stages.synthetic.common.synth.SyntheticXactParams;
import org.radargun.stages.synthetic.common.XactOp;
import org.radargun.stressors.KeyGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class SyntheticDistinctXactFactory_PreDaP extends SyntheticDistinctXactFactory<SyntheticXactParams, SyntheticXact_PreDap> {

   private final static int NOT_ACCESSED = -1;

   public SyntheticDistinctXactFactory_PreDaP(SyntheticXactParams params) {
      super(params);
      if (params.getNumKeys() < params.getROGets()) {
         log.fatal("You cannot read distinct keys if the number of keys is less than the number of accesses");
         throw new IllegalArgumentException("You cannot read distinct keys if the number of keys is less than the number of accesses");
      }
   }

   protected XactOp[] buildReadSet() {
      int numR = params.getROGets();
      XactOp[] ops = new XactOp[numR];
      KeyGenerator keyGen = params.getKeyGenerator();
      Object key;
      int keyToAccess;
      Random r = params.getRandom();
      int numKeys = params.getNumKeys();
      int nodeIndex = params.getNodeIndex();
      int threadIndex = params.getThreadIndex();
      Set<Integer> alreadyRead = new HashSet<Integer>();
      boolean okRead;
      for (int i = 0; i < numR; i++) {
         okRead = false;
         do {
            keyToAccess = r.nextInt(numKeys);
            if (!alreadyRead.contains(keyToAccess))
               okRead = true;
         }
         while (!okRead);

         key = keyGen.generateKey(nodeIndex, threadIndex, keyToAccess);
         alreadyRead.add(keyToAccess);
         ops[i] = new XactOp(key, "", false);
      }
      return ops;
   }

   @Override
   protected XactOp[] buildReadWriteSet() {
      XactOp[] ops = new XactOp[rwB.length];
      List<Integer> readSet = new ArrayList<Integer>(), writeSet = new ArrayList<Integer>();
      int numReads = params.getUpReads();
      int numWrites = params.getUpPuts();
      int total = numReads + numWrites;
      KeyGenerator kg = params.getKeyGenerator();
      Random r = params.getRandom();
      int nodeIndex = params.getNodeIndex(), threadIndex = params.getThreadIndex();
      int sizeS = params.getSizeOfValue();
      boolean bW = params.isAllowBlindWrites();
      int numK = params.getNumKeys();
      Integer key;
      int nextWrite = 0; //without blind writes, this points to the next read item to write. The first operation is a read
      //Generate rwSet
      try {
         for (int i = 0; i < total; i++) {
            if (!rwB[i]) {  //Read
               do {
                  key = r.nextInt(numK);
               }
               while (readSet.contains(key));  //avoid repetitions
               readSet.add(0, key);
               ops[i] = new XactOp(kg.generateKey(nodeIndex, threadIndex, key),
                                   null, false);    //add a read op and increment
            } else {    //Put
               if (bW) {        //You can have (distinct) blind writes
                  do {
                     key = r.nextInt(numK);
                  }
                  while (writeSet.contains(key));  //avoid repetitions among writes
                  writeSet.add(0, key);
                  ops[i] = new XactOp(kg.generateKey(nodeIndex, threadIndex, key),
                                      generateRandomString(sizeS), true);    //add a write op
               } else { //No blind writes: Take a value already read and increment         To have distinct writes, remember numWrites<=numReads in this case
                  ops[i] = new XactOp(ops[nextWrite++].getKey(),
                                      generateRandomString(sizeS), true);

                  while (nextWrite < total && rwB[nextWrite]) {       //scan the bitmask up to the next read operation while it is a put op, go on
                     nextWrite++;
                  }
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      if (log.isTraceEnabled())
         log.trace(ops);
      return ops;
   }

   //TODO: actually also the read-only transactions should read only a distinct number of items

   @Override
   protected SyntheticXact_PreDap generateXact(SyntheticXactParams p) {
      return new SyntheticXact_PreDap(p);
   }


}
