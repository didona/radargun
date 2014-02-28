package org.radargun.ycsb.transaction.diego;

import org.radargun.ycsb.YCSBStressor;
import org.radargun.ycsb.generators.UniformIntegerGenerator;

/**
 * // TODO: Document this
 *
 * @author diego
 * @since 4.0
 */
public class D_YCSBStressor extends YCSBStressor {
   private String workload;
   private int numOps;
   private UniformIntegerGenerator xactGenerator = new UniformIntegerGenerator(1, 100); //inclusive


   public D_YCSBStressor(String workload, int numOps) {
      this.numOps = numOps;
      this.workload = workload;
   }

   public String getWorkload() {
      return workload;
   }

   public void setWorkload(String workload) {
      this.workload = workload;
   }

   protected D_YCSBTransaction generateNextTransaction() {
      int ran = xactGenerator.nextInt();
      switch (D_Workload.valueOf(workload)) {
         case SCAN:
            return generate_SCAN(ran);
         case SHORT_RANGES:
            return generate_SHORT_RANGES(ran);
         case READ_MODIFY_WRITE:
            return generate_RMW(ran);
         case READ_MODIFY_WRITE_N:
            return generate_RMW_N(ran);
         case UPDATE_HEAVY:
            return generate_UPDATE_H(ran);
         case READ_DOMINATED:
            return generate_READ_DOMINATED(ran);
         case READ_DOMINATED_N:
            return generate_READ_DOMINATED_N(ran);
         case UPDATE_HEAVY_N:
            return generate_UPDATE_H_N(ran);
         default: {
            System.out.println(workload + " not supported");
            throw new UnsupportedOperationException(workload + " not supported");
         }
      }

   }

   private D_YCSBTransaction generate_SCAN(int i) {
      return new D_Read(this.ig);
   }

   private D_YCSBTransaction generate_SHORT_RANGES(int i) {
      return new D_ShortRange(this.ig, this.recordCount);
   }

   private D_YCSBTransaction generate_RMW(int i) {
      if (i <= D_Workload.READ_MODIFY_WRITE.readP)
         return new D_Read(this.ig);
      return new D_ReadModifyWrite(this.ig);
   }

   private D_YCSBTransaction generate_RMW_N(int i) {
      if (i <= D_Workload.READ_MODIFY_WRITE.readP)
         return new D_Read_N(this.ig,numOps);
      return new D_ReadModifyWrite_N(this.ig,numOps);
   }

   private D_YCSBTransaction generate_UPDATE_H(int i) {
      if (i <= D_Workload.UPDATE_HEAVY.readP)
         return new D_Read(this.ig);
      return new D_Update(this.ig);
   }

   private D_YCSBTransaction generate_UPDATE_H_N(int i) {
      if (i <= D_Workload.UPDATE_HEAVY.readP)
         return new D_Read_N(this.ig, numOps);
      return new D_Update_N(this.ig, numOps);
   }

   private D_YCSBTransaction generate_READ_DOMINATED(int i) {
      if (i <= D_Workload.READ_DOMINATED.readP)
         return new D_Read(this.ig);
      return new D_Update(this.ig);
   }

   private D_YCSBTransaction generate_READ_DOMINATED_N(int i) {
      if (i <= D_Workload.READ_DOMINATED.readP)
         return new D_Read_N(this.ig, this.numOps);
      return new D_Update_N(this.ig, this.numOps);
   }

   private D_YCSBTransaction generate_READ_LATEST(int i) {
      throw new UnsupportedOperationException(workload + " not supported yet");
   }

   private enum D_Workload {

      SCAN(100, 0), SHORT_RANGES(100, 0), READ_MODIFY_WRITE(95, 5), UPDATE_HEAVY(50, 50), READ_DOMINATED(95, 5), READ_LATEST(95, 5),
      SCAN_N(100, 0), SHORT_RANGES_N(100, 0), READ_MODIFY_WRITE_N(95, 5), UPDATE_HEAVY_N(50, 50), READ_DOMINATED_N(95, 5), READ_LATEST_N(95, 5);
      int readP, writeP;

      D_Workload(int r, int w) {
         this.readP = r;
         this.writeP = w;
      }


   }
}
