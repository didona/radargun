package org.radargun.stages;

import org.radargun.stressors.SyntheticPutGetStressor;
import org.radargun.stressors.TestPreparePutGetStressor;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class SyntheticPrepareTestBenchmarkStage extends SyntheticBenchmarkStage {

   private int numRemoteNodesToContact;

   private boolean onePutMultipleOwners;

   public void setOnePutMultipleOwners(String onePutMultipleOwners) {
      this.onePutMultipleOwners = Boolean.valueOf(onePutMultipleOwners);
   }

   public int getNumRemoteNodesToContact() {
      return numRemoteNodesToContact;
   }

   public void setNumRemoteNodesToContact(int numRemoteNodesToContact) {
      this.numRemoteNodesToContact = numRemoteNodesToContact;
   }

   protected SyntheticPutGetStressor buildPutGetStressor() {
      return new TestPreparePutGetStressor();
   }

   @Override
   protected void initPutGetStressor(SyntheticPutGetStressor putGetStressor) {
      super.initPutGetStressor(putGetStressor);
      ((TestPreparePutGetStressor) putGetStressor).setNumRemoteNodeToContact(numRemoteNodesToContact);
      ((TestPreparePutGetStressor) putGetStressor).setOnlyOneWriter(this.onlyOneWorker);
      ((TestPreparePutGetStressor) putGetStressor).setOnePutMultipleOwners(this.onePutMultipleOwners);
   }
}
