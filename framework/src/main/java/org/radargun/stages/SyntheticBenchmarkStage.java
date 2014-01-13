package org.radargun.stages;/*
 * INESC-ID, Instituto de Engenharia de Sistemas e Computadores Investigação e Desevolvimento em Lisboa
 * Copyright 2013 INESC-ID and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import org.radargun.stages.synthetic.common.XACT_RETRY;
import org.radargun.stressors.SyntheticPutGetStressor;

import java.util.Map;

/**
 * @author Diego Didona, didona@gsd.inesc-id.pt Date: 20/03/13
 */
public class SyntheticBenchmarkStage extends WebSessionBenchmarkStage {

   private int updateXactWrites = 1;
   private int readOnlyXactSize = 1;
   private int updateXactReads = 1;
   private boolean allowBlindWrites = false;
   private XACT_RETRY retryMode = XACT_RETRY.NO_RETRY;
   protected int readsBeforeFirstWrite = 1;
   private boolean masterOnlyWrites = false;
   private boolean precomputeRWset = false;
   private boolean sampleNTCBServiceTime = false;
   private long spinBetweenOps = 0L;

   protected SyntheticPutGetStressor buildPutGetStressor() {
      return new SyntheticPutGetStressor();
   }

   protected void initPutGetStressor(SyntheticPutGetStressor putGetStressor) {
      putGetStressor.setNodeIndex(getSlaveIndex());
      putGetStressor.setNumberOfAttributes(numberOfAttributes);
      putGetStressor.setNumOfThreads(numOfThreads);
      putGetStressor.setSizeOfAnAttribute(sizeOfAnAttribute);
      putGetStressor.setWritePercentage(writePercentage);
      putGetStressor.setKeyGeneratorClass(keyGeneratorClass);
      putGetStressor.setUseTransactions(useTransactions);
      putGetStressor.setCommitTransactions(commitTransactions);
      putGetStressor.setTransactionSize(transactionSize);
      putGetStressor.setDurationMillis(durationMillis);
      putGetStressor.setupdateXactWrites(updateXactWrites);
      putGetStressor.setReadOnlyXactSize(readOnlyXactSize);
      putGetStressor.setUpdateXactReads(updateXactReads);
      putGetStressor.setAllowBlindWrites(allowBlindWrites);
      putGetStressor.setStatsSamplingInterval(statsSamplingInterval);
      putGetStressor.setXact_retry(retryMode);
      putGetStressor.setReadsBeforeFirstWrite(readsBeforeFirstWrite);
      putGetStressor.setMasterOnlyWrites(masterOnlyWrites);
      putGetStressor.setSampleNTCBServiceTime(sampleNTCBServiceTime);
      putGetStressor.setPrecomputeRWset(precomputeRWset);
   }

   protected Map<String, String> doWork() {
      log.info("Starting " + getClass().getSimpleName() + ": " + this);
      SyntheticPutGetStressor putGetStressor = buildPutGetStressor();
      initPutGetStressor(putGetStressor);

      return putGetStressor.stress(cacheWrapper);
   }

   public void setReadsBeforeFirstWrite(int readsBeforeFirstWrite) {
      this.readsBeforeFirstWrite = readsBeforeFirstWrite;
   }

   public XACT_RETRY getRetryMode() {
      return retryMode;
   }

   public void setRetryMode(XACT_RETRY retryMode) {
      this.retryMode = retryMode;
   }

   public int getUpdateXactWrites() {
      return updateXactWrites;
   }

   public boolean isAllowBlindWrites() {
      return allowBlindWrites;
   }

   public int getReadOnlyXactSize() {
      return readOnlyXactSize;
   }

   public void setSampleNTCBServiceTime(String sampleNTCBServiceTime) {
      this.sampleNTCBServiceTime = Boolean.valueOf(sampleNTCBServiceTime);
   }


   public void setPrecomputeRWset(String precomputeRWset) {
      this.precomputeRWset = Boolean.valueOf(precomputeRWset);
   }

   public void setReadOnlyXactSize(int readOnlyXactSize) {
      this.readOnlyXactSize = readOnlyXactSize;
   }

   public int getUpdateXactReads() {
      return updateXactReads;
   }

   public void setUpdateXactReads(int updateXactReads) {
      this.updateXactReads = updateXactReads;
   }

   public void setUpdateXactWrites(int updateXactWrites) {
      this.updateXactWrites = updateXactWrites;
   }

   public void setAllowBlindWrites(boolean allowBlindWrites) {
      this.allowBlindWrites = allowBlindWrites;
   }

   public void setRetryMode(String retry) {
      this.retryMode = XACT_RETRY.valueOf(retry);
   }

   public boolean isMasterOnlyWrites() {
      return masterOnlyWrites;
   }

   public void setMasterOnlyWrites(String masterOnlyWrites) {
      this.masterOnlyWrites = Boolean.valueOf(masterOnlyWrites);
   }

   public void setSpinBetweenOps(long spinBetweenOps) {
      this.spinBetweenOps = spinBetweenOps;
   }

   @Override
   public String toString() {
      return "SyntheticBenchmarkStage{" +
              "updateXactWrites=" + updateXactWrites +
              ", readOnlyXactSize=" + readOnlyXactSize +
              ", updateXactReads=" + updateXactReads +
              '}';
   }
}
