package org.radargun.stages.synthetic.runtimeDap.prepareTest.oneOwnerMultiOp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radargun.stages.synthetic.common.XactOp;
import org.radargun.stages.synthetic.common.synth.SyntheticXactParams;
import org.radargun.stages.synthetic.runtimeDap.IteratorRuntimeDap_UP;

import java.util.Arrays;
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
 */
public class IteratorRunTimeDapControlledRemoteNodes_UP extends IteratorRuntimeDap_UP {

   public IteratorRunTimeDapControlledRemoteNodes_UP(SyntheticXactParams params, boolean[] b) {
      super(params, b);
      numKeys = (int) ((double) (numKeys - (numKeys % params.getNumNodes())) / ((double) params.getNumNodes()));
      initNodeList(params.getNumRemoteNodesToContact());
      if (params.getNumRemoteNodesToContact() > params.getNumNodes() - 1)
         throw new IllegalArgumentException("You must have remoteNodes >= numRemoteNodes you want to contact!");
      if (blindWriteAllowed && (params.getUpReads() != 0))
         throw new IllegalArgumentException("If you put blind writes, you only do Update xact with blind writes!");
      if (blindWriteAllowed && params.getUpPuts() < params.getNumRemoteNodesToContact())
         throw new IllegalArgumentException("With blind writes you onyl are supposed to do puts, so you must have numPuts >= numRemoteNodesToContact");
      if (!blindWriteAllowed && params.getUpReads() < params.getNumRemoteNodesToContact()) {
         throw new IllegalArgumentException("With NO blind writes you must have numReads per update xact >= remoteNodesToContact");
      }
      if (trace)
         log.trace(this);
   }

   private int[] remoteNodesToAccess;
   private int nextRemoteNodeToAccess = 0;
   private HashMap<Integer, Integer> keyToNode = new HashMap<Integer, Integer>();
   private final static Log log = LogFactory.getLog(IteratorRunTimeDapControlledRemoteNodes_UP.class);
   private final static boolean trace = log.isTraceEnabled();

   /**
    * Populate the list of remote nodes to contact
    *
    * @param nodesToContact
    */
   private void initNodeList(int nodesToContact) {

      this.remoteNodesToAccess = new int[nodesToContact];
      for (int i = 0; i < nodesToContact; i++) {
         remoteNodesToAccess[i] = uniformRandomRemoteNode(i);
      }
      if (trace)
         log.trace("Remote nodes I'll contact are " + Arrays.toString(remoteNodesToAccess));
   }

   /**
    * Check whether the node node has already been listed withing the first index ones
    *
    * @param index
    * @param node
    * @return
    */
   private boolean alreadyChosen(int index, int node) {
      for (int i = 0; i < index; i++)
         if (remoteNodesToAccess[i] == node)
            return true;
      return false;
   }

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
      while (node == nodeIndex || alreadyChosen(index, node));
      return node;
   }

   /**
    * If blindWrite is true, we ONLY do blind writes, i.e. the reads have to be 0!
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
         do {
            key = r.nextInt(numKeys);
         }
         while (readSet.contains(key));  //avoid repetitions: for simplicity, I do not use twice a key with the same id, regardless of the node!
         readSet.addLast(key);
         keyToNode.put(key, nextRemoteNodeToAccess);
         keyV = keyGen.generateKey(remoteNodesToAccess[nextRemoteNodeToAccess], threadIndex, key);
         toRet = new XactOp(keyV, null, false);    //add a read
         if (trace)
            log.trace("Going to read " + keyV + " from node " + remoteNodesToAccess[nextRemoteNodeToAccess]);
         nextRemoteNodeToAccess++;
         nextRemoteNodeToAccess = (nextRemoteNodeToAccess) % remoteNodesToAccess.length;

      } else {    //Put
         /*
         If I have blind writes, I *only* have blind writes, thus I write a key from the current remote node
          */
         if (blindWriteAllowed) {        //You can *only* have (distinct) blind writes
            do {
               key = r.nextInt(numKeys);
               if(trace)
                  log.trace("Drawn key "+key);
            }
            while (writeSet.contains(key));  //avoid repetitions among writes. For simplicity, use only once a key, regardless of the node contacted
            log.trace("Final key "+key);
            writeSet.add(key);
            keyV = keyGen.generateKey(remoteNodesToAccess[nextRemoteNodeToAccess], threadIndex, key);
            toRet = new XactOp(keyV,
                    generateRandomString(sizeOfAttribute), true);    //add a write op
            if (trace)
               log.trace("Going to blindly write " + keyV + " of node " + remoteNodesToAccess[nextRemoteNodeToAccess]);
            nextRemoteNodeToAccess++;
            nextRemoteNodeToAccess = (nextRemoteNodeToAccess) % remoteNodesToAccess.length;
         }
         /*
         If I do not have blind writes, I read from a key already read
          */
         else { //No blind writes: Take a value already read and increment         To have distinct writes, remember numWrites<=numReads in this case
            int keyToUse = readSet.get(indexNextWrite);
            int remoteNodeIndex = keyToNode.get(keyToUse);
            keyV = keyGen.generateKey(remoteNodeIndex, threadIndex, keyToUse);
            toRet = new XactOp(keyV,
                    generateRandomString(sizeOfAttribute), true);
            if (trace)
               log.trace("Going to write already read  " + keyV + " of node " + keyToNode.get(keyToUse));
            indexNextWrite++;
         }
      }
      currentOp++;
      return toRet;
   }

}
