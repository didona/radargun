package org.radargun.stressors;

import org.radargun.stages.synthetic.common.synth.SyntheticXactParams;
import org.radargun.stages.synthetic.runtimeDap.prepareTest.SyntheticXactFactoryTestPrepare_RunTimeDaP;
import org.radargun.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class TestPreparePutGetStressor extends SyntheticPutGetStressor {

   private int numRemoteNodeToContact;

   public void setNumRemoteNodeToContact(int numRemoteNodeToContact) {
      this.numRemoteNodeToContact = numRemoteNodeToContact;
   }

   @Override
   protected List<Stressor> executeOperations() throws Exception {
      List<Stressor> stressors = new ArrayList<Stressor>(numOfThreads);
      startPoint = new CountDownLatch(1);
      startTime = System.nanoTime();
      for (int threadIndex = 0; threadIndex < numOfThreads; threadIndex++) {
         Stressor stressor = new TestPrepareStressor(threadIndex, (KeyGenerator) Utils.instantiate(this.getKeyGeneratorClass()), nodeIndex, numberOfKeys);
         stressors.add(stressor);
         stressor.start();
      }
      log.info("Cache wrapper info is: " + cacheWrapper.getInfo());

      startPoint.countDown();
      log.info("Started " + stressors.size() + " stressor threads.");
      for (Stressor stressor : stressors) {
         stressor.join();
      }
      return stressors;
   }

   private class TestPrepareStressor extends SyntheticStressor {
      private TestPrepareStressor(int threadIndex, KeyGenerator perThreadKeyGen, int nodeIndex, int numKeys) {
         super(threadIndex, perThreadKeyGen, nodeIndex, numKeys);
      }

      protected void initFactory() {
         this.factory = new SyntheticXactFactoryTestPrepare_RunTimeDaP(buildParams());
      }

      protected SyntheticXactParams buildParams() {
         SyntheticXactParams ret = super.buildParams();
         ret.setNumNodes(cacheWrapper.getNumMembers());
         ret.setNumRemoteNodesToContact(numRemoteNodeToContact);
         return ret;
      }
   }
}
