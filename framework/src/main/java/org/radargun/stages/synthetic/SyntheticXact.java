package org.radargun.stages.synthetic;

import org.radargun.CacheWrapper;

import java.util.Arrays;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public abstract class SyntheticXact extends Xact {

   protected XactOp[] ops;


   public SyntheticXact(CacheWrapper wrapper) {
      super(wrapper);
   }

   public XactOp[] getOps() {
      return ops;
   }

   public void setOps(XactOp[] ops) {
      this.ops = ops;
   }

   @Override
   public String toString() {
      return "SyntheticXact{" +
            ", ops=" + Arrays.toString(ops) +
            '}';
   }
}