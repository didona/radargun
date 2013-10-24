package org.radargun.stages.synthetic.runtimeDap;

import org.radargun.stages.synthetic.common.synth.SyntheticXactParams;
import org.radargun.stages.synthetic.common.XactOp;
import org.radargun.stressors.KeyGenerator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */

public class IteratorRuntimeDap_RO implements Iterator<XactOp> {


   private SyntheticXactParams params;
   private int toRead;
   private KeyGenerator keyGen;
   private Random r;
   private int numKeys;
   private int nodeIndex;
   private int threadIndex;
   private Set<Integer> readSet;

   private int current = 0;

   @Override
   public boolean hasNext() {
      return current < toRead;
   }

   @Override
   public XactOp next() {
      boolean okRead = false;
      Object key;
      int keyToAccess;
      do {
         keyToAccess = r.nextInt(numKeys);
         if (!readSet.contains(keyToAccess))
            okRead = true;
      }
      while (!okRead);

      key = keyGen.generateKey(nodeIndex, threadIndex, keyToAccess);
      readSet.add(keyToAccess);
      current++;
      return new XactOp(key, "", false);
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException("Remove is not supported");
   }

   public IteratorRuntimeDap_RO(SyntheticXactParams params) {
      this.params = params;
      toRead = params.getROGets();
      keyGen = params.getKeyGenerator();
      r = params.getRandom();
      numKeys = params.getNumKeys();
      nodeIndex = params.getNodeIndex();
      threadIndex = params.getThreadIndex();
      readSet = new HashSet<Integer>();
   }
}
