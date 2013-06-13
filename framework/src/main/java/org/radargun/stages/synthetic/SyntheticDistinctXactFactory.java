package org.radargun.stages.synthetic;

import org.radargun.stressors.KeyGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class SyntheticDistinctXactFactory extends SyntheticXactFactory {

   private boolean[] rwB;

   public SyntheticDistinctXactFactory(SyntheticXactParams params) {
      super(params);
      rwB = this.rwB();
      log.trace(rwB);
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
      Integer key;
      int lastRead = 0, lastWrite = 0;
      //Generate rwSet
      for (int i = 0; i < total; i++) {
         if (!rwB[i]) {
            do {
               key = r.nextInt();
            }
            while (readSet.contains(key));
            readSet.add(0, key);
            ops[i] = new XactOp(kg.generateKey(nodeIndex, threadIndex, key),
                                null, false);    //add a read op and increment
         } else {
            if (bW) {        //You can have (distinct) blind writes
               do {
                  key = r.nextInt();
               }
               while (writeSet.contains(key));
               writeSet.add(0, key);
               ops[i] = new XactOp(kg.generateKey(nodeIndex, threadIndex, key),
                                   generateRandomString(sizeS), true);    //add a write op
            } else { //No blind writes: Take a value already read and increment         To have distinct writes, remember numWrites<=numReads in this case
               ops[i] = new XactOp(ops[lastWrite++].getKey(),
                                   generateRandomString(sizeS), true);
            }
         }
      }
      log.trace(ops);
      return ops;
   }


   /*
   @Override
   protected XactOp[] buildReadWriteSet() {
      List<Integer> readSet = new ArrayList<Integer>(), writeSet = new ArrayList<Integer>();
      int numReads = params.getUpReads();
      int numWrites = params.getUpPuts();
      KeyGenerator kg = params.getKeyGenerator();
      Random r = params.getRandom();
      XactOp[] ops = new XactOp[numReads + numWrites];
      Integer key;
      //Generate readSet
      for (int i = 1; i <= numReads; i++) {
         do {
            key = r.nextInt();
         }
         while (readSet.contains(key));
         readSet.add(0, key);
      }
      //Generate write set
      if (!params.isAllowBlindWrites()) {
         //If no blind writes, map every write to a read element
         for (int i = 0; i < numWrites; i = (i + 1) % numReads) {
            writeSet.add(0, readSet.get(i));
         }
      } else {
         for (int i = 1; i <= numWrites; i++) {
            do {
               key = r.nextInt();
            }
            while (writeSet.contains(key));
            writeSet.add(key);
         }
      }

      int firstWrite = params.getReadsBeforeFirstWrite();
      int nodeIndex = params.getNodeIndex(), threadIndex = params.getThreadIndex();
      int sizeS = params.getSizeOfValue();
      int readI = 0;
      for (; readI < firstWrite; readI++) {
         ops[readI] = new XactOp(kg.generateKey(nodeIndex, threadIndex, readSet.get(readI)),
                                 null, false);
      }

      ops[firstWrite] = new XactOp(kg.generateKey(nodeIndex, threadIndex, writeSet.get(0)),
                                   generateRandomString(sizeS), true);

      double remainingReads = numReads - firstWrite;
      double remainingWrites = numWrites - 1;
      boolean moreReads = false;

      if (remainingReads >= remainingWrites)
         moreReads = true;
      int every = (int) (moreReads ? (remainingReads / remainingWrites) : remainingWrites / remainingReads);


      int writeI = 1;
      int I = readI + writeI;
      int e = 0;

      while (I < numReads + numWrites) {
         if (moreReads) {
            ops[I++] = new XactOp(kg.generateKey(nodeIndex, threadIndex, writeSet.get(readI++)),
                                  null, false);
         } else {
            ops[I++] = new XactOp(kg.generateKey(nodeIndex, threadIndex, writeSet.get(writeI++)),
                                  generateRandomString(sizeS), true);
         }
         e++;

         if (e % every == 0) {
            if (moreReads)
               ops[I++] = new XactOp(kg.generateKey(nodeIndex, threadIndex, writeSet.get(writeI++)),
                                     generateRandomString(sizeS), true);
            else
               ops[I++] = new XactOp(kg.generateKey(nodeIndex, threadIndex, writeSet.get(readI++)),
                                     null, false);

         }
      }
      if (log.isTraceEnabled()) {
         for (int j = 0; j <= ops.length; j++)
            log.trace("[" + j + "] = " + ops[j].getKey());
      }
      return ops;
   }
   */

   /**
    * @return A boolean array. True means put, false means get
    */
   private boolean[] rwB() {
      int numReads = params.getUpReads();
      int numWrites = params.getUpPuts();
      int total = numReads + numWrites;
      boolean[] rwB = new boolean[total];
      int fW = params.getReadsBeforeFirstWrite();
      if (numReads < numWrites && !params.isAllowBlindWrites())
         throw new RuntimeException("NumWrites has to be greater than numReads to avoid blindWrites and have no duplicates");
      int readI = 0;

      //Set reads before first write
      for (; readI < fW; readI++) {
         rwB[readI] = false;
      }
      rwB[fW] = true;
      if (total == fW + 1)
         return rwB;
      double remainingReads = numReads - fW;
      double remainingWrites = numWrites - 1;
      boolean moreReads = false;
      //If you have more remaining reads than writes, then each X reads you'll do ONE write; otherwise it's the opposite. If you have no more of one kind, you'll only have of the other one
      int groupRead, groupWrite, numGroups;
      if (remainingReads >= remainingWrites) {
         moreReads = true;
         groupRead = remainingWrites > 0 ? (int) Math.ceil(remainingReads / remainingWrites) : (int) remainingReads;
         groupWrite = remainingWrites > 0 ? 1 : 0;
         numGroups = remainingWrites > 0 ? (int) remainingWrites : 1;
      } else {
         moreReads = false;
         groupRead = remainingReads > 0 ? 1 : 0;
         groupWrite = remainingReads > 0 ? (int) Math.ceil(remainingWrites / remainingReads) : (int) remainingWrites;
         numGroups = remainingReads > 0 ? (int) remainingReads : 1;
      }
      int index = fW + 1;
      while (numGroups-- > 0) {
         int r = groupRead;
         int w = groupWrite;
         while (r-- > 0) {
            rwB[index++] = false;
         }
         while (w-- > 0) {
            rwB[index++] = true;
         }
      }
      while (index < total) {
         rwB[index] = !moreReads;
      }

      return rwB;

   }


}
