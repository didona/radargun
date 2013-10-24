package org.radargun.stages.synthetic.runtimeDap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radargun.stages.synthetic.common.synth.SyntheticXact;
import org.radargun.stages.synthetic.common.synth.SyntheticXactParams;
import org.radargun.stages.synthetic.common.XactOp;
import org.radargun.stages.synthetic.common.xactClass;

import java.util.Iterator;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class SyntheticXact_RunTimeDaP extends SyntheticXact {

   private int roRead, upRead, upWrite;

   protected boolean[] RWB;

   private final static Log log = LogFactory.getLog(SyntheticXactFactory_RunTimeDaP.class);
   private final static boolean trace = log.isTraceEnabled();
   //TODO: now this is ONLY for RR. The extension for GMU without blind writes has to be created.
   //TODO: this is not optimized for encounter time locking: reads and writes can happen at any place in the xact


   public SyntheticXact_RunTimeDaP(SyntheticXactParams params) {
      super(params);
      this.params = params;
   }

   public void setParams(SyntheticXactParams params) {
      this.params = params;
   }

   public void setRWB(boolean[] RWB) {
      this.RWB = RWB;
   }

   @Override
   protected Iterator<XactOp> iterator() {
      if (getClazz().equals(xactClass.RO))
         return new IteratorRuntimeDap_RO(params);
      return new IteratorRuntimeDap_UP(params, RWB);
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

   /*
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
      int i = 0;
      while (i < toDo) {
         if (RWB[i]) {
            cache.put(null, kg.generateKey(ni, ti, r.nextInt(numKeys)), generateRandomString(size));
         } else {
            cache.get(null, kg.generateKey(ni, ti, r.nextInt(numKeys)));
         }
         i++;
      }
   }

   protected final String generateRandomString(int size) {
      // each char is 2 bytes
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < size / 2; i++) sb.append((char) (64 + params.getRandom().nextInt(26)));
      return sb.toString();
   }
   */
}
