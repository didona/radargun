package org.radargun.tpcc;

import org.radargun.CacheWrapper;
import org.radargun.tpcc.domain.Customer;
import org.radargun.tpcc.domain.CustomerLookup;
import org.radargun.tpcc.domain.District;
import org.radargun.tpcc.domain.Item;
import org.radargun.tpcc.domain.NewOrder;
import org.radargun.tpcc.domain.Order;
import org.radargun.tpcc.domain.OrderLine;
import org.radargun.tpcc.domain.Stock;
import org.radargun.tpcc.domain.Warehouse;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Sebastiano Peluso
 */
public class DeterministicThreadParallelTpccPopulation extends ThreadParallelTpccPopulation {

   private boolean populateOnlyLocalWarehouses = false;


   public DeterministicThreadParallelTpccPopulation(CacheWrapper wrapper, int numWarehouses, int slaveIndex, int numSlaves, long cLastMask, long olIdMask, long cIdMask, int elementsPerBlock) {
      super(wrapper, numWarehouses, slaveIndex, numSlaves, cLastMask, olIdMask, cIdMask, 1, elementsPerBlock);
   }

   public DeterministicThreadParallelTpccPopulation(CacheWrapper wrapper, int numWarehouses, int slaveIndex, int numSlaves, long cLastMask, long olIdMask, long cIdMask, int elementsPerBlock, boolean populateOnlyLocalWarehouses) {
      super(wrapper, numWarehouses, slaveIndex, numSlaves, cLastMask, olIdMask, cIdMask, 1, elementsPerBlock);
      this.populateOnlyLocalWarehouses = populateOnlyLocalWarehouses;
   }

   /*
    public DeterministicThreadParallelTpccPopulation(CacheWrapper wrapper, int numWarehouses, int slaveIndex, int numSlaves, long cLastMask, long olIdMask, long cIdMask, int elementsPerBlock, ThreadTpccToolsManager threadTpccToolsManager) {
        super(wrapper, numWarehouses, slaveIndex, numSlaves, cLastMask, olIdMask, cIdMask, 1, elementsPerBlock, threadTpccToolsManager);
    }
    */

   protected boolean txAwarePut(DomainObject domainObject) {
      try {
         domainObject.storeToPopulate(wrapper, slaveIndex, true);
      } catch (Throwable throwable) {
         return false;
      }

      return true;
   }


   protected Warehouse createWarehouse(int warehouseId) {
      return new Warehouse(warehouseId,
                           "aaaaaaa",
                           "aaaaaaaaaaa",
                           "aaaaaaaaaaa",
                           "aaaaaaaaaaa",
                           "aa",
                           "aaaa11111",
                           0.1234D,
                           TpccTools.WAREHOUSE_YTD);
   }


   protected Item createItem(long itemId) {

      long price = itemId % 100;
      price = (price == 0) ? 100 : price;

      //1<=price<=100
      return new Item(itemId,
                      (itemId % 10000L),
                      "aaaaaaaaaaaaaaa",
                      price,
                      "aaaaaaaaaaaaaaaaaaaaaaaaaaa");
   }


   protected Stock createStock(long stockId, int warehouseId) {

      long quantity = stockId % 91;
      quantity = (quantity == 0) ? 91 : quantity;

      quantity += 9;

      //10<=quantity<=100
      return new Stock(stockId,
                       warehouseId,
                       quantity,
                       "aaaaaaaaaaaaaaaaaaaaaaaa",
                       "aaaaaaaaaaaaaaaaaaaaaaaa",
                       "aaaaaaaaaaaaaaaaaaaaaaaa",
                       "aaaaaaaaaaaaaaaaaaaaaaaa",
                       "aaaaaaaaaaaaaaaaaaaaaaaa",
                       "aaaaaaaaaaaaaaaaaaaaaaaa",
                       "aaaaaaaaaaaaaaaaaaaaaaaa",
                       "aaaaaaaaaaaaaaaaaaaaaaaa",
                       "aaaaaaaaaaaaaaaaaaaaaaaa",
                       "aaaaaaaaaaaaaaaaaaaaaaaa",
                       0,
                       0,
                       0,
                       "aaaaaaaaaaaaaaaaaaaaaaaaaaa");
   }


   protected District createDistrict(int districtId, int warehouseId) {
      return new District(warehouseId,
                          districtId,
                          "aaaaaaa",
                          "aaaaaaaaaaa",
                          "aaaaaaaaaaa",
                          "aaaaaaaaaaa",
                          "aa",
                          "aaaa11111",
                          0.1234D,
                          TpccTools.WAREHOUSE_YTD,
                          3001);
   }


   protected Customer createCustomer(int warehouseId, long districtId, long customerId, String customerLastName) {

      //log.trace("New Customer: WID "+warehouseId+" DID "+districtId+" CID "+customerId);

      return new Customer(warehouseId,
                          districtId,
                          customerId,
                          "aaaaaaaaa",
                          "OE",
                          customerLastName,
                          "aaaaaaaaaaa",
                          "aaaaaaaaaaa",
                          "aaaaaaaaaaa",
                          "aa",
                          "aaaa11111",
                          "aaaaaaaaaaaaaaaa",
                          new Date(1364752545391L),
                          (customerId % 1000 == 1) ? "BC" : "GC",
                          500000.0,
                          0.4,
                          -10.0, 10.0, 1, 0,
                          "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
   }


   protected Order createOrder(long orderId, long districtId, long warehouseId, Date aDate, int o_ol_cnt,
                               int seqAlea) {

      long customer = orderId % (TpccTools.NB_MAX_CUSTOMER - 1);
      customer = (customer == 0) ? (TpccTools.NB_MAX_CUSTOMER - 1) : customer;

      //1<=customer<=TpccTools.NB_MAX_CUSTOMER-1

      return new Order(orderId,
                       districtId,
                       warehouseId,
                       customer,
                       aDate,
                       (orderId < TpccTools.LIMIT_ORDER) ? 5 : 0,
                       6,
                       1);
   }


   public String deterministic_c_last(long seed) {

      String c_last = "";
      long number = seed;

      if (number < 0) number = 0;

      if (number > 999) number = 999;

      String alea = String.valueOf(number);

      while (alea.length() < 3) {
         alea = "0" + alea;
      }
      for (int i = 0; i < 3; i++) {
         c_last += TpccTools.C_LAST[Integer.parseInt(alea.substring(i, i + 1))];
      }
      return c_last;
   }


   protected void populateWarehouses() {
      log.info("Populate Warehouses");
      if (this.numWarehouses > 0) {

         performMultiThreadPopulation(1, this.numWarehouses, new ThreadCreator() {
            @Override
            public Thread createThread(int threadIdx, long lowerBound, long upperBound) {
               return new PopulateWarehouseThread(threadIdx, lowerBound, upperBound);
            }
         });
         log.info("Warehouses object populated");
         Warehouse warehouseToBePopulated;
         for (int i = 1; i <= this.numWarehouses; i++) {

            warehouseToBePopulated = createWarehouse(i);

            if (isLocalWarehouse(warehouseToBePopulated)) {

               log.info(" WAREHOUSE " + i);

               populateStock(i);

               populateDistricts(i);

            }

         }
      }


   }

   private boolean isLocalWarehouse(Warehouse warehouseToBePopulated) {
      if (!populateOnlyLocalWarehouses)
         log.info("Populating stocks and districts for all warehouses: there are NO local warehouses, but only local objects.");
      return populateOnlyLocalWarehouses && warehouseToBePopulated.isLocalToNode(wrapper);
   }


   @Override
   /**
    * Populate ALL items on EACH node: of course a node will actually insert only the items
    * that are local to it
    */
   protected void populateItem() {
      log.trace("Populating Items");

      long init_id_item = 1;
      long num_of_items = TpccTools.NB_MAX_ITEM;

      performMultiThreadPopulation(init_id_item, num_of_items, new ThreadCreator() {
         @Override
         public Thread createThread(int threadIdx, long lowerBound, long upperBound) {
            return new PopulateItemThread(threadIdx, lowerBound, upperBound);
         }
      });
   }

   @Override
   protected void populateStock(final int warehouseId) {
      if (warehouseId < 0) {
         log.warn("Trying to populate Stock for a negative warehouse ID. skipping...");
         return;
      }
      log.trace("Populating Stock for warehouse " + warehouseId);

      long init_id_item = 1;
      long num_of_items = TpccTools.NB_MAX_ITEM;

      performMultiThreadPopulation(init_id_item, num_of_items, new ThreadCreator() {
         @Override
         public Thread createThread(int threadIdx, long lowerBound, long upperBound) {
            return new PopulateStockThread(threadIdx, lowerBound, upperBound, warehouseId);
         }
      });
   }


   @Override
   protected void populateDistricts(final int warehouseId) {
      if (warehouseId < 0) {
         log.warn("Trying to populate Districts for a negative warehouse ID. skipping...");
         return;
      }
      log.trace("Populating Districts for warehouse " + warehouseId);

      long init_id_item = 1;
      long num_of_items = TpccTools.NB_MAX_DISTRICT;


      performMultiThreadPopulation(init_id_item, num_of_items, new ThreadCreator() {
         @Override
         public Thread createThread(int threadIdx, long lowerBound, long upperBound) {
            return new PopulateDistrictThread(threadIdx, lowerBound, upperBound, warehouseId);
         }
      });

      for (int i = 1; i <= TpccTools.NB_MAX_DISTRICT; i++) {

         populateCustomers(warehouseId, i);
         populateOrders(warehouseId, i);
      }
   }


   protected void populateHistory(int id_customer, int id_wharehouse, int id_district) {

      //This is empty.

   }


   @Override
   protected void populateCustomers(final int warehouseId, final int districtId) {
      if (warehouseId < 0 || districtId < 0) {
         log.warn("Trying to populate Customer with a negative warehouse or district ID. skipping...");
         return;
      }

      log.trace("Populating Customers for warehouse " + warehouseId + " and district " + districtId);

      final ConcurrentHashMap<CustomerLookupQuadruple, LinkedBlockingQueue<Long>> lookupContentionAvoidance =
            new ConcurrentHashMap<CustomerLookupQuadruple, LinkedBlockingQueue<Long>>();

      performMultiThreadPopulation(1, TpccTools.NB_MAX_CUSTOMER, new ThreadCreator() {
         @Override
         public Thread createThread(int threadIdx, long lowerBound, long upperBound) {
            return new DeterministicPopulateCustomerThread(threadIdx, lowerBound, upperBound, warehouseId, districtId, lookupContentionAvoidance);
         }
      });


      deterministicPopulateCustomerLookup(lookupContentionAvoidance);

   }

   protected void deterministicPopulateCustomerLookup(final ConcurrentHashMap<CustomerLookupQuadruple, LinkedBlockingQueue<Long>> map) {
      log.trace("Populating customer lookup ");


      performMultiThreadPopulation(0, 1, new ThreadCreator() {
         @Override
         public Thread createThread(int threadIdx, long lowerBound, long upperBound) {
            return new DeterministicPopulateCustomerLookupThread(threadIdx, lowerBound, upperBound, map);
         }
      });
   }


   protected void populateOrderLines(int id_wharehouse, int id_district, int id_order, int o_ol_cnt, Date aDate) {
      //log.info("populate order line");
      for (int i = 0; i < o_ol_cnt; i++) {

         double amount;
         Date delivery_date;

         if (id_order >= TpccTools.LIMIT_ORDER) {
            amount = 20.22;
            delivery_date = null;
         } else {
            amount = 0.0;
            delivery_date = aDate;
         }


         OrderLine newOrderLine = new OrderLine(id_order,
                                                id_district,
                                                id_wharehouse,
                                                i,
                                                50000,
                                                id_wharehouse,
                                                delivery_date,
                                                5,
                                                amount,
                                                "aaaaaaaaaaaaa");


         boolean successful = false;
         while (!successful) {
            try {
               newOrderLine.storeToPopulate(wrapper, -1, true);
               successful = true;
            } catch (Throwable e) {
               log.warn(e);
            }
         }

      }
   }


   protected void populateNewOrder(int id_wharehouse, int id_district, int id_order) {
      //log.info("populate new order");

      NewOrder newNewOrder = new NewOrder(id_order, id_district, id_wharehouse);


      boolean successful = false;
      while (!successful) {
         try {
            newNewOrder.storeToPopulate(wrapper, -1, true);
            successful = true;
         } catch (Throwable e) {
            log.warn(e);
         }
      }

   }


   protected class DeterministicPopulateCustomerThread extends Thread {
      private long lowerBound;
      private long upperBound;
      private int warehouseId;
      private int districtId;
      private ConcurrentHashMap<CustomerLookupQuadruple, LinkedBlockingQueue<Long>> lookupContentionAvoidance;
      private final int threadIdx;

      @Override
      public String toString() {
         return "PopulateCustomerThread{" +
               "lowerBound=" + lowerBound +
               ", upperBound=" + upperBound +
               ", warehouseId=" + warehouseId +
               ", districtId=" + districtId +
               '}';
      }

      @SuppressWarnings("unchecked")
      public DeterministicPopulateCustomerThread(int threadIdx, long lowerBound, long upperBound, int warehouseId, int districtId,
                                                 ConcurrentHashMap<CustomerLookupQuadruple, LinkedBlockingQueue<Long>> c) {
         this.lowerBound = lowerBound;
         this.upperBound = upperBound;
         this.districtId = districtId;
         this.warehouseId = warehouseId;
         this.lookupContentionAvoidance = c;
         this.threadIdx = threadIdx;
      }

      public void run() {
         tpccTools.set(threadTpccToolsManager.getTpccTools(threadIdx));
         logStart(toString());

         long remainder = (upperBound - lowerBound) % elementsPerBlock;
         long numBatches = (upperBound - lowerBound - remainder) / elementsPerBlock;
         long base = lowerBound;

         for (int batch = 1; batch <= numBatches; batch++) {
            logBatch(toString(), batch, numBatches);
            executeTransaction(base, base + elementsPerBlock);
            base += elementsPerBlock;
         }

         logRemainder(toString());
         executeTransaction(base, upperBound + 1);

         logFinish(toString());
      }

      private void executeTransaction(long start, long end) {
         logCustomerPopulation(warehouseId, districtId, start, end - 1);

         long seed = 0;
         do {
            startTransactionIfNeeded();

            LinkedBlockingQueue<Long> newLookupList;
            LinkedBlockingQueue<Long> lookupList;

            for (long customerId = start; customerId < end; customerId++) {

               seed = customerId % 1000;

               seed = (seed == 0) ? 1000 : seed;

               seed--;

               //0<=seed<=999

               String c_last = deterministic_c_last(seed);

               if (!txAwarePut(createCustomer(warehouseId, districtId, customerId, c_last))) {
                  break; // rollback tx
               }


               CustomerLookupQuadruple clt = new CustomerLookupQuadruple(c_last, warehouseId, districtId, customerId);

               if (!this.lookupContentionAvoidance.containsKey(clt)) {

                  newLookupList = new LinkedBlockingQueue<Long>();

                  lookupList = this.lookupContentionAvoidance.putIfAbsent(clt, newLookupList);

                  if (lookupList == null) {
                     lookupList = newLookupList;
                  }
               } else {
                  lookupList = this.lookupContentionAvoidance.get(clt);
               }

               lookupList.add(new Long(customerId));

            }
         } while (!endTransactionIfNeeded());
      }
   }


   protected class DeterministicPopulateCustomerLookupThread extends Thread {
      private ConcurrentHashMap<CustomerLookupQuadruple, LinkedBlockingQueue<Long>> map;
      private long lowerBound;
      private long upperBound;
      private final int threadIdx;

      @Override
      public String toString() {
         return "DeterministicPopulateCustomerLookupThread{" +
               "lowerBound=" + lowerBound +
               ", upperBound=" + upperBound +
               '}';
      }

      @SuppressWarnings("unchecked")
      public DeterministicPopulateCustomerLookupThread(int threadIdx, long l, long u, ConcurrentHashMap<CustomerLookupQuadruple, LinkedBlockingQueue<Long>> map) {
         this.map = map;
         this.lowerBound = l;
         this.upperBound = u;
         this.threadIdx = threadIdx;
      }

      public void run() {
         tpccTools.set(threadTpccToolsManager.getTpccTools(threadIdx));
         logStart(toString());


         executeTransaction();

         logFinish(toString());
      }

      private void executeTransaction() {
         logCustomerLookupPopulation(0, 1);
         do {
            startTransactionIfNeeded();
            Set<CustomerLookupQuadruple> keys = map.keySet();

            CustomerLookup customerLookup;
            LinkedBlockingQueue<Long> ids;
            for (CustomerLookupQuadruple k : keys) {

               customerLookup = new CustomerLookup(k.getC_last(), k.getWarehouseId(), k.getDistrictId());
               ids = map.get(k);

               for (Long l : ids) {

                  customerLookup.addId(l);

               }


               if (!txAwarePut(customerLookup)) {
                  break; //rollback tx
               }


            }


         } while (!endTransactionIfNeeded());
      }
   }


   protected class PopulateWarehouseThread extends Thread {

      private long lowerBound;
      private long upperBound;
      private final int threadIdx;

      @Override
      public String toString() {
         return "PopulateWarehouseThread{" +
               "lowerBound=" + lowerBound +
               ", upperBound=" + upperBound +
               '}';
      }

      public PopulateWarehouseThread(int threadIdx, long low, long up) {
         this.lowerBound = low;
         this.upperBound = up;
         this.threadIdx = threadIdx;
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
         logWarehousePopulation(start, end - 1);
         do {
            startTransactionIfNeeded();
            for (long warehouseId = start; warehouseId < end; warehouseId++) {
               if (!txAwarePut(createWarehouse((int) warehouseId))) {
                  break; //rollback tx;
               }
            }
         } while (!endTransactionIfNeeded());
      }

      protected void logWarehousePopulation(long initID, long finishID) {
         log.debug("Populate Warehouse from " + initID + " to " + finishID);
      }
   }

   protected class PopulateDistrictThread extends Thread {
      private long lowerBound;
      private long upperBound;
      private int warehouseId;
      private final int threadIdx;

      @Override
      public String toString() {
         return "PopulateDistrictThread{" +
               "lowerBound=" + lowerBound +
               ", upperBound=" + upperBound +
               ", warehouseId=" + warehouseId +
               '}';
      }

      public PopulateDistrictThread(int threadIdx, long low, long up, int warehouseId) {
         this.lowerBound = low;
         this.upperBound = up;
         this.warehouseId = warehouseId;
         this.threadIdx = threadIdx;
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
         logDistrictPopulation(warehouseId, start, end - 1);
         do {
            startTransactionIfNeeded();
            for (int districtId = (int) start; districtId < end; districtId++) {
               if (!txAwarePut(createDistrict(districtId, warehouseId))) {
                  break;
               }
            }
         } while (!endTransactionIfNeeded());
      }
   }

}
