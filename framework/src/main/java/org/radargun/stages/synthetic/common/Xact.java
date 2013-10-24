package org.radargun.stages.synthetic.common;

import org.radargun.CacheWrapper;
import org.radargun.stages.synthetic.common.synth.SyntheticXactParams;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public abstract class Xact {
   private long initResponseTime;
   private long initServiceTime;
   private xactClass clazz;
   protected CacheWrapper cache;

   private boolean isCommit;  //we track only commit-abort without considering also xact that can abort because of application logic (and might be not restarted, then)

   public Xact(SyntheticXactParams p) {
      initResponseTime = System.nanoTime();
      initServiceTime = initResponseTime;
      cache = p.getCache();
   }

   public void setCache(CacheWrapper wrapper) {
      this.cache = wrapper;
   }

   public long getInitResponseTime() {
      return initResponseTime;
   }

   public void setInitResponseTime(long initResponseTime) {
      this.initResponseTime = initResponseTime;
   }

   public long getInitServiceTime() {
      return initServiceTime;
   }

   public void setInitServiceTime(long initServiceTime) {
      this.initServiceTime = initServiceTime;
   }

   public CacheWrapper getCache() {
      return cache;
   }


   public xactClass getClazz() {
      return clazz;
   }

   public void setClazz(xactClass clazz) {
      this.clazz = clazz;
   }

   public boolean isCommit() {
      return isCommit;
   }

   public void setCommit(boolean commit) {
      isCommit = commit;
   }

   @Override
   public String toString() {
      return "Xact{" +
            "initResponseTime=" + initResponseTime +
            ", initServiceTime=" + initServiceTime +
            ", clazz=" + clazz +
            ", cache=" + cache +
            ", isCommit=" + isCommit +
            '}';
   }

   public abstract void executeLocally() throws Exception;
}
