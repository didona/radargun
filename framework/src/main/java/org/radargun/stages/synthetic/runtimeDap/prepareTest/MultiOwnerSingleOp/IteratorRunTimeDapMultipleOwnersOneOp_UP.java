package org.radargun.stages.synthetic.runtimeDap.prepareTest.MultiOwnerSingleOp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radargun.stages.synthetic.common.XactOp;
import org.radargun.stages.synthetic.common.synth.SyntheticXactParams;
import org.radargun.stages.synthetic.runtimeDap.IteratorRuntimeDap_UP;
import org.radargun.stressors.ContentionStringKeyGenerator;

import java.util.HashMap;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */

/**
 * If blindWrites  == true, then it is supposed that you *only* do update xacts with blind writes
 * in order to focus *only* on prepare
 * <p/>
 * This is supposed to do 1 single write, touching N distinct remote nodes and not touching the local node
 */
public class IteratorRunTimeDapMultipleOwnersOneOp_UP extends IteratorRuntimeDap_UP {

   public IteratorRunTimeDapMultipleOwnersOneOp_UP(SyntheticXactParams params, boolean[] b) {
      super(params, b);
      numKeys = (int) ((double) (numKeys - (numKeys % params.getNumNodes())) / ((double) params.getNumNodes()));
      if (!blindWriteAllowed ||
              params.getNumRemoteNodesToContact() > params.getNumNodes() - 1 ||
              (params.getUpReads() != 0) ||
              params.getUpPuts() > 1 || params.getReadsBeforeFirstWrite() > 0 ||
              !(params.getKeyGenerator() instanceof ContentionStringKeyGenerator)
              )
         throw new RuntimeException("THIS works with the following:" +
                 "i) 100% update" +
                 "ii) xact with only one blind write (i.e., no gets, firstReadBeforeWrite==0)" +
                 "iii) replication degree equal to the numbers of remote nodes you want to contact " +
                 "iv) the string generator has to be the string contention");

      if (trace)
         log.trace(this);
   }


   private HashMap<Integer, Integer> keyToNode = new HashMap<Integer, Integer>();
   private final static Log log = LogFactory.getLog(IteratorRunTimeDapMultipleOwnersOneOp_UP.class);
   private final static boolean trace = log.isTraceEnabled();


   /**
    * Choose randomly a remote node to contact as index-th remote nodes (circular)
    *
    * @param index
    * @return
    */
   private int uniformRandomRemoteNode(int index) {
      int node;
      do {
         node = r.nextInt(params.getNumNodes());
      }
      while (node == nodeIndex);
      return node;
   }

   /**
    * Write one key that belongs to the selected node (and other N-1) but *not* to the local one
    *
    * @return
    */
   public XactOp next() {
      XactOp toRet;
      int key;
      Object keyV;
      /*
      If I read it means I am in no-blindwrite mode, so I read a new key from the current remote node and increment
       */
      if (!rwB[currentOp]) {  //Read
         throw new RuntimeException("I told you No read operations!");

      } else {    //Put
         /*
         Do the blind write
          */
         if (blindWriteAllowed) {        //You can *only* have (distinct) blind writes
            do {
               key = r.nextInt(numKeys);
               keyV = keyGen.generateKey(nodeIndex, threadIndex, key);
               if (trace)
                  log.trace("Drawn key " + key);
            }
            while (params.getCache().isKeyLocal(keyV));  //no local write
            log.trace("Final key " + key);
            writeSet.add(key);
            toRet = new XactOp(keyV,
                    generateRandomString(sizeOfAttribute), true);    //add a write op
         }
         /*
         I only want one blind write
         */
         else {
            throw new RuntimeException("I told you only one blind write!");
         }
      }
      return toRet;
   }

}
