package org.radargun.stages.synthetic;

import java.util.Arrays;
import java.util.Iterator;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public abstract class SyntheticXact extends Xact {

   protected XactOp[] ops;
   protected long spinBetweenOps = 0L;
   protected SyntheticXactParams params;


   public SyntheticXact(SyntheticXactParams p) {
      super(p);
      this.params = p;
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

   @Override
   public void executeLocally() throws Exception {
      Iterator<XactOp> it = iterator();
      XactOp op;
      long spin = params.getSpinBetweenOps();
      final boolean isSpin = spin > 0;
      while (it.hasNext()) {
         op = it.next();
         if (op.isPut())
            cache.put(null, op.getKey(), op.getValue());
         else
            cache.get(null, op.getKey());
         if (isSpin)
            doSpin(spin);

      }
   }

   private void doSpin(long spin) {
      for (long l = 0; l < spin; l++) ;
   }

   protected abstract Iterator<XactOp> iterator();
}