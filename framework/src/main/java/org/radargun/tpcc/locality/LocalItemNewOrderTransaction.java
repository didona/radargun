package org.radargun.tpcc.locality;

import org.radargun.tpcc.TpccTools;
import org.radargun.tpcc.domain.Item;
import org.radargun.tpcc.transaction.NewOrderTransaction;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class LocalItemNewOrderTransaction extends NewOrderTransaction {

   public LocalItemNewOrderTransaction(TpccTools tpccTools, int threadId, int warehouseID) {
      super(tpccTools, threadId, warehouseID);
   }

   protected Item createItem(long id, long w_id) {
      LocalItem i = new LocalItem();
      i.setI_id(id);
      i.setW_id((int) w_id);
      return i;
   }
}
