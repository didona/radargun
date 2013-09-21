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
public abstract class SyntheticXactFactory <P extends SyntheticXactParams, S extends SyntheticXact> extends XactFactory<P, S> {

   public SyntheticXactFactory(P params) {
      super(params);

   }

   @Override
   public final S buildXact(S last) {
      XACT_RETRY retry = params.getXact_retry();
      S toRet;
      xactClass clazz;
      XactOp[] ops;
      if (retry == XACT_RETRY.NO_RETRY || last == null || last.isCommit()) {    //brand new xact
         clazz = computeClazz();
         if (clazz == xactClass.RO) {
            ops = buildReadSet();
         } else {
            ops = buildReadWriteSet();
         }
         toRet = generateXact(params);//new SyntheticXact(params.getCache());
         toRet.setOps(ops);
         toRet.setClazz(clazz);
      } else {   //retried xact
         toRet = last;
         if (retry == XACT_RETRY.RETRY_SAME_CLASS) {    //If not the very same xact, rebuild the read/writeSet
            clazz = toRet.getClazz();
            if (clazz == xactClass.RO) {
               ops = buildReadSet();
            } else {
               ops = buildReadWriteSet();
            }
            toRet.setOps(ops);
         }
         last.setInitServiceTime(System.nanoTime());
      }

      if(log.isTraceEnabled())
         log.trace("New xact built " + toRet.toString());
      return toRet;
   }

   /**
    * This method builds a set containing keys to be read and <key, value> pairs to be written.  Put operations are
    * uniformly distributed across read operations (should work also with numReads<numWrites as long as there is at
    * least one read). If blind writes are not allowed, then writes are delayed till the first read operation. After
    * that point, put operations are allowed and write always on the last read value (even multiple times in a row, for
    * simplicity)
    *
    * @return a readWriteSet
    */
   protected XactOp[] buildReadWriteSet() {
      int toDoRead = params.getUpReads();
      int toDoWrite = params.getUpPuts();
      int toDo = toDoRead + toDoWrite;
      int total = toDo;
      int writePerc = (int) (100 * (((double) toDoWrite) / ((double) (toDo))));
      Random r = params.getRandom();
      int numKeys = params.getNumKeys();
      boolean allowBlindWrites = params.isAllowBlindWrites();
      XactOp[] ops = new XactOp[toDo];
      boolean doPut;
      int size = params.getSizeOfValue();
      boolean canWrite = false;
      int keyToAccess;
      int lastRead = 0;
      Object value;
      for (int i = 0; i < total; i++) {
         //Determine if you can read or not

         if (toDo == toDoWrite)      //I have only puts left
            doPut = true;
         else if (toDo == toDoRead)  //I have only reads left
            doPut = false;
         else if (allowBlindWrites) {     //I choose uniformly
            doPut = r.nextInt(100) < writePerc;
         } else {
            if (!canWrite) {   //first read
               doPut = false;
               canWrite = true;
            } else {
               doPut = r.nextInt(100) < writePerc;
            }
         }
         if (doPut) {  //xact can write
            if (allowBlindWrites) { //xact can choose what it wants
               keyToAccess = r.nextInt(numKeys);
            } else { //xact has to write something it has already read
               keyToAccess = lastRead;
            }
         } else {    //xact reads
            keyToAccess = r.nextInt(numKeys);
            lastRead = keyToAccess;
         }
         value = doPut ? generateRandomString(size) : "";
         ops[i] = new XactOp(keyToAccess, value, doPut);
         toDo--;
         if (doPut) {
            toDoWrite--;
         } else {
            toDoRead--;
         }
      }
      return ops;
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

      for (int i = 0; i < numR; i++) {
         keyToAccess = r.nextInt(numKeys);
         key = keyGen.generateKey(nodeIndex, threadIndex, keyToAccess);
         ops[i] = new XactOp(key, "", false);
      }
      return ops;
   }

   protected final xactClass computeClazz() {

      CacheWrapper cache = params.getCache();
      if (cache.isPassiveReplication()) {
         if (!cache.isCoordinator())
            return xactClass.RO;
         if (params.isMasterOnlyWrites())
            return xactClass.WR;
      }
      if ((1 + params.getRandom().nextInt(100)) > params.getWritePercentage())
         return xactClass.RO;
      return xactClass.WR;

   }

   protected final String generateRandomString(int size) {
      // each char is 2 bytes
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < size / 2; i++) sb.append((char) (64 + params.getRandom().nextInt(26)));
      return sb.toString();
   }

   protected abstract S generateXact(P params);

}
