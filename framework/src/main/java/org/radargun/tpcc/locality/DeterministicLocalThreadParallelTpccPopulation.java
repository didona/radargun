package org.radargun.tpcc.locality;

import org.radargun.CacheWrapper;
import org.radargun.tpcc.DeterministicThreadParallelTpccPopulation;
import org.radargun.tpcc.TpccTools;
import org.radargun.tpcc.domain.Item;

/**
 * This class inherits the  beauty of the deterministicThreadParallelTpccPopulation but also populates the items
 * in such a way that they are local to a given warehouse
 *
 * @author diego
 * @since 4.0
 */
public class DeterministicLocalThreadParallelTpccPopulation extends DeterministicThreadParallelTpccPopulation {


   public DeterministicLocalThreadParallelTpccPopulation(CacheWrapper wrapper, int numWarehouses, int slaveIndex, int numSlaves, long cLastMask, long olIdMask, long cIdMask, int elementsPerBlock) {
      super(wrapper, numWarehouses, slaveIndex, numSlaves, cLastMask, olIdMask, cIdMask, elementsPerBlock);
   }

   private int[] localWarehouseIds() {
      throw new RuntimeException("To implement");
   }


   protected Item createItem(long itemId, int wid) {

      long price = itemId % 100;
      price = (price == 0) ? 100 : price;

      //1<=price<=100
      return new LocalItem(itemId,
              (itemId % 10000L),
              "aaaaaaaaaaaaaaa",
              price,
              "aaaaaaaaaaaaaaaaaaaaaaaaaaa", wid);
   }


   @Override
   protected void populateItem() {
      log.trace("Populating Items");

      long init_id_item = 1;
      long num_of_items = TpccTools.NB_MAX_ITEM;
      int[] wids = localWarehouseIds();

      //A node has to populate a number of local warehouses with all its items

      for (final int w : wids) {
         log.trace("Populating items for warehouse " + w);
         performMultiThreadPopulation(init_id_item, num_of_items, new ThreadCreator() {
            @Override
            public Thread createThread(int threadIdx, long lowerBound, long upperBound) {
               return new PopulateLocalItemThread(threadIdx, lowerBound, upperBound, w);
            }
         });
      }
   }


   protected class PopulateLocalItemThread extends PopulateItemThread {
      private int w_id;

      public PopulateLocalItemThread(int threadIdx, long low, long up, int w_id) {
         super(threadIdx, low, up);
         this.w_id = w_id;
      }

      public void run() {
         tpccTools.set(threadTpccToolsManager.getTpccTools(threadIdx));
         logStart(toString());

         long remainder = (upperBound - lowerBound) % elementsPerBlock;
         long numBatches = (upperBound - lowerBound - remainder) / elementsPerBlock;
         long base = lowerBound;

         for (long batch = 1; batch <= numBatches; batch++) {
            logBatch(toString(), batch, numBatches);
            executeTransaction(base, base + elementsPerBlock);
            base += elementsPerBlock;
         }

         logRemainder(toString());
         executeTransaction(base, upperBound + 1);

         logFinish(toString());
      }

      private void executeTransaction(long start, long end) {
         logItemsPopulation(start, end - 1);
         do {
            startTransactionIfNeeded();
            for (long itemId = start; itemId < end; itemId++) {
               if (!txAwarePut(createItem(itemId, w_id))) {
                  break; //rollback tx;
               }
            }
         } while (!endTransactionIfNeeded());
      }
   }
}
