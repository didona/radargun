package org.radargun.tpcc;

import org.radargun.CacheWrapper;
import org.radargun.tpcc.domain.Customer;
import org.radargun.tpcc.domain.History;
import org.radargun.tpcc.domain.Item;
import org.radargun.tpcc.domain.Order;
import org.radargun.tpcc.domain.OrderLine;
import org.radargun.tpcc.domain.Stock;

import java.util.Date;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0 Every node tries to put everything, but then, if a datum is not local it is not inserted. Period.
 */
public class Diego_DeterministicPopulation extends TpccPopulation {


   public Diego_DeterministicPopulation(CacheWrapper wrapper, int numWarehouses, int slaveIndex, int numSlaves, long cLastMask, long olIdMask, long cIdMask, boolean populateLocalOnly) {
      super(wrapper, numWarehouses, slaveIndex, numSlaves, cLastMask, olIdMask, cIdMask, populateLocalOnly);
   }

   protected boolean splitWorkAmongNodes() {
      return false;
   }

   protected void populateWarehouses() {
      log.trace("Populate warehouses");
      if (this.numWarehouses > 0) {
         for (int warehouseId = 1; warehouseId <= this.numWarehouses; warehouseId++) {
            log.info("Populate Warehouse " + warehouseId);
            txAwarePut(createWarehouse(warehouseId));

            populateStock(warehouseId);

            populateDistricts(warehouseId);

            printMemoryInfo();
         }
      }
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

   @Override
   public String _c_last(int customerId) {
      long seed = customerId % 1000;

      seed = (seed == 0) ? 1000 : seed;

      seed--;

      //0<=seed<=999

      return deterministic_c_last(seed);
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


   private final static long customerDate = 1364752545391L;
   private final static long historyDate = customerDate + 1000000L;
   private final static String historyData = "aaaaaaaaaaaaaaaaaa"; //TODO: check compliance

   protected History createHistory(long customerId, long districtId, long warehouseId) {
      return new History(customerId,
                         districtId,
                         warehouseId,
                         districtId,
                         warehouseId,
                         new Date(historyDate), 10, historyData);
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


}
