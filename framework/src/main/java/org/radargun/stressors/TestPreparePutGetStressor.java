package org.radargun.stressors;

import org.radargun.stages.synthetic.common.synth.SyntheticXactParams;
import org.radargun.stages.synthetic.runtimeDap.prepareTest.MultiOwnerSingleOp.SyntheticXactFactoryMultiOwnerSingleOp_RunTimeDaP;
import org.radargun.stages.synthetic.runtimeDap.prepareTest.oneOwnerMultiOp.SyntheticXactFactoryTestPrepare_RunTimeDaP;
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
   private boolean onlyOneWriter;
   private boolean onePutMultipleOwners;


   public void setNumRemoteNodeToContact(int numRemoteNodeToContact) {
      this.numRemoteNodeToContact = numRemoteNodeToContact;
   }

   public void setOnePutMultipleOwners(boolean onePutMultipleOwners) {
      this.onePutMultipleOwners = onePutMultipleOwners;
   }

   public void setOnlyOneWriter(boolean onlyOneWriter) {
      this.onlyOneWriter = onlyOneWriter;
   }

   @Override
   protected List<Stressor> executeOperations() throws Exception {
      List<Stressor> stressors = new ArrayList<Stressor>(numOfThreads);
      startPoint = new CountDownLatch(1);
      startTime = System.nanoTime();
      for (int threadIndex = 0; threadIndex < numOfThreads; threadIndex++) {
         TestPrepareStressor stressor = new TestPrepareStressor(threadIndex, (KeyGenerator) Utils.instantiate(this.getKeyGeneratorClass()), nodeIndex, numberOfKeys);
         stressor.initFactory();
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
         this.perThreadKeyGen = new TestPrepareContentionStringKeyGenerator(cacheWrapper.getNumMembers(), numKeys);
         if (log.isTraceEnabled())
            log.trace(perThreadKeyGen);
      }

      protected void runInternal() {
         if (onlyOneWriter && !cacheWrapper.isCoordinator())
            return;
         super.runInternal();
      }

      protected void initFactory() {
         if (onePutMultipleOwners)
            this.factory = new SyntheticXactFactoryMultiOwnerSingleOp_RunTimeDaP(buildParams());
         else
            this.factory = new SyntheticXactFactoryTestPrepare_RunTimeDaP(buildParams());
         if (traceE)
            log.trace("Factory " + factory);
      }

      protected SyntheticXactParams buildParams() {
         SyntheticXactParams ret = super.buildParams();
         ret.setNumNodes(cacheWrapper.getNumMembers());
         ret.setNumRemoteNodesToContact(numRemoteNodeToContact);
         return ret;
      }
   }
}
