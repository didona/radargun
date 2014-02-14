package org.radargun.ycsb.generators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Random;

/**
 * // TODO: Document this
 *
 * @author Diego Didona
 * @since 4.0
 * <p/>
 * A distributed zipfian generator: given a number of groups G, each having k keys,
 * it generates a number given by uniform(0,G-1) * k + zipf(0, k-1)
 * In this way I have that in the system I have G elements ranked j, with j [0, k-1], i.e.,
 * I have G overlapped zipfian distributions. This implies that the skew in each group is higher because
 * the number of keys is lower than the total, but with an adequate number of groups, these hot spots
 * are likely to be equally distributed to nodes in the system
 */
public class DZipfianGenerator extends ZipfianGenerator {
   private Random r;
   private int groups;
   private final static Log log = LogFactory.getLog(DZipfianGenerator.class);

   public DZipfianGenerator(long _items, double zipf, long _groups) {
      super(_items / _groups, zipf);//this sets the item variable
      groups = (int) _groups;
      r = new Random();
      if (log.isTraceEnabled())
         log.trace("Creating a DZipfianGenerator for " + groups + " groups and " + _items + " keys " + " itemsPerGroup " + items);
   }

   @Override
   public long nextLong(long itemcount) {
      return r.nextInt(groups) * items + super.nextLong(itemcount);
   }


}
