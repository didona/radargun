package org.radargun.tpcc.locality;

import org.radargun.CacheWrapper;
import org.radargun.tpcc.domain.Item;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class LocalItem extends Item {

   private int w_id;

   public LocalItem(long i_id, long i_im_id, String i_name, double i_price, String i_data, int w_id) {
      super(i_id, i_im_id, i_name, i_price, i_data);
      this.w_id = w_id;
   }

   public LocalItem() {
      super();
   }

   public int getW_id() {
      return w_id;
   }

   public void setW_id(int w_id) {
      this.w_id = w_id;
   }

   protected String getKey() {
      return super.getKey() + "_" + w_id;
   }

   @Override
   public boolean load(CacheWrapper wrapper) throws Throwable {

      LocalItem loaded = (LocalItem) wrapper.get(null, this.getKey());

      if (loaded == null) return false;

      this.i_data = loaded.i_data;
      this.i_im_id = loaded.i_im_id;
      this.i_name = loaded.i_name;
      this.i_price = loaded.i_price;
      this.w_id = loaded.w_id;

      return true;
   }

   @Override
   public boolean equals(Object o) {
      return super.equals(o) && this.w_id == ((LocalItem) o).w_id;
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return 31 * result + (int) (w_id ^ ((long) w_id >>> 32));
   }
}
