package org.radargun.stages.synthetic;

import java.util.Arrays;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public abstract class SyntheticDistinctXactFactory <P extends SyntheticXactParams, S extends SyntheticXact> extends SyntheticXactFactory<P,S> {

   protected final boolean[] rwB;

   public SyntheticDistinctXactFactory(P params) {
      super(params);
      rwB = this.rwB();
      if (log.isTraceEnabled()) log.trace(Arrays.toString(rwB));
   }

   /**
    * @return A boolean array. True means put, false means get
    */
   private boolean[] rwB() {
      final boolean trace = log.isTraceEnabled();
      int numReads = params.getUpReads();
      int numWrites = params.getUpPuts();
      int total = numReads + numWrites;
      boolean[] rwB = new boolean[total];
      String msgExc = null;
      int fW = params.getReadsBeforeFirstWrite();
      if (fW > numReads)
         msgExc = ("NumReadsBeforeFirstWrite > numReads!");
      if (numReads < numWrites && !params.isAllowBlindWrites())
         msgExc = "NumWrites has to be greater than numReads to avoid blindWrites and have no duplicates";
      if (fW == 0 && !params.isAllowBlindWrites())
         msgExc = ("Without blind writes you must at least read once before writing! NumReadsBeforeWrites at least 1!");
      if (msgExc != null) {
         log.fatal(msgExc);
         throw new RuntimeException(msgExc);
      }
      int readI = 0;
      try {
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
            groupRead = remainingWrites > 0 ? (int) Math.floor(remainingReads / remainingWrites) : (int) remainingReads;
            groupWrite = remainingWrites > 0 ? 1 : 0;
            numGroups = remainingWrites > 0 ? (int) remainingWrites : 1;
            if (trace) log.trace("More remaining reads than write: " + remainingReads + " vs " + remainingWrites);
            log.trace("I will have " + numGroups + " groups of " + groupRead + " reads and " + groupWrite + " writes");
         } else {
            moreReads = false;
            groupRead = remainingReads > 0 ? 1 : 0;
            groupWrite = remainingReads > 0 ? (int) Math.floor(remainingWrites / remainingReads) : (int) remainingWrites;
            numGroups = remainingReads > 0 ? (int) remainingReads : 1;
            if (trace) log.trace("More remaining writes than reads: " + remainingWrites + " vs " + remainingReads);
            if (trace)
               log.trace("I will have " + numGroups + " groups of " + groupRead + " reads and " + groupWrite + " writes");
         }
         int index = fW + 1;
         while (numGroups-- > 0) {
            if (trace) log.trace(numGroups + " groups to go");
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
            rwB[index++] = !moreReads;  //If you had more reads you have to top-up with writes(true) and vice versa
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      return rwB;

   }
}
