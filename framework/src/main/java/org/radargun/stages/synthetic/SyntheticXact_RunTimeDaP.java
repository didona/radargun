package org.radargun.stages.synthetic;

import org.radargun.CacheWrapper;
import org.radargun.stressors.KeyGenerator;

import java.util.Random;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class SyntheticXact_RunTimeDaP extends SyntheticXact {

   private int roRead, upRead, upWrite;
   private SyntheticXactParams params;
   protected boolean[] RWB;

   //TODO: now this is ONLY for RR. The extension for GMU without blind writes has to be created.
   //TODO: this is not optimized for encounter time locking: reads and writes can happen at any place in the xact

   public SyntheticXact_RunTimeDaP(CacheWrapper wrapper) {
      super(wrapper);
   }

   public void setParams(SyntheticXactParams params) {
      this.params = params;
   }

   public void setRWB(boolean[] RWB) {
      this.RWB = RWB;
   }

   @Override
   public void executeLocally() throws Exception {
      if (getClazz().equals(xactClass.RO)) {
         executeLocallyRO();
      } else {
         executeLocallyUP();
      }
   }

   private void executeLocallyRO() throws Exception {
      int readToDo = roRead;
      KeyGenerator kg = params.getKeyGenerator();
      int numKeys = params.getNumKeys();
      int ti = params.getThreadIndex();
      int ni = params.getNodeIndex();
      Random r = params.getRandom();
      while (readToDo > 0) {
         cache.get(null, kg.generateKey(ni, ti, r.nextInt(numKeys)));
         readToDo--;
      }
   }

   private void executeLocallyUP() throws Exception {

      int toDoR = upRead;
      int toDoW = upWrite;
      int toDo = toDoR + toDoW;
      KeyGenerator kg = params.getKeyGenerator();
      int numKeys = params.getNumKeys();
      int ti = params.getThreadIndex();
      int ni = params.getNodeIndex();
      Random r = params.getRandom();
      int size = params.getSizeOfValue();
      while (toDo > 0) {
         if (RWB[toDo]) {
            cache.put(null, kg.generateKey(ni, ti, r.nextInt(numKeys)), generateRandomString(size));
         } else {
            cache.get(null, kg.generateKey(ni, ti, r.nextInt(numKeys)));
         }
         toDo--;
      }
   }

   protected final String generateRandomString(int size) {
      // each char is 2 bytes
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < size / 2; i++) sb.append((char) (64 + params.getRandom().nextInt(26)));
      return sb.toString();
   }

   public int getRoRead() {
      return roRead;
   }

   public void setRoRead(int roRead) {
      this.roRead = roRead;
   }

   public int getUpRead() {
      return upRead;
   }

   public void setUpRead(int upRead) {
      this.upRead = upRead;
   }

   public int getUpWrite() {
      return upWrite;
   }

   public void setUpWrite(int upWrite) {
      this.upWrite = upWrite;
   }
}
