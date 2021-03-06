package org.radargun.stages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radargun.state.MasterState;
import org.radargun.utils.CacheSizeValues;
import org.radargun.utils.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Stage that generates
 *
 * @author Mircea.Markus@jboss.com
 */
public class CsvReportGenerationStage extends AbstractMasterStage {

   private static Log log = LogFactory.getLog(CsvReportGenerationStage.class);

   private String targetDir = "reports";
   private String separator = ",";

   private File outputFile;
   private FileWriter fileWriter;

   public boolean execute() {
      Map<Integer, Map<String, Object>> results = (Map<Integer, Map<String, Object>>) masterState.get("results");
      if (results == null) {
         log.error("Could not find reports('results') on the master. Master's state is  " + masterState);
         return false;
      }


      try {
         int size = results.size();
         if (size == 0) {
            log.warn("Nothing to report!");
            return false;
         }

         for (int i = 0; i < size; i++) {
            log.trace("Result for slave " + i + " is\n" + results.get(i).toString());
         }
         log.info("Going to join the cacheSizeValues with the final report");
         joinCacheSizes(results, (List<CacheSizeValues>) masterState.get("CacheSizeResults"));
         masterState.remove("CacheSizeResults");
         /*
         DIEGO
         After having produced the csv with the cache sizes, we have to clear the relevant map entry
         in the master state!
          */
         prepareOutputFile(results.size());
         writeData(results);
      } catch (Exception e) {
         log.error(e);
         return false;
      }
      return true;
   }

   private void joinCacheSizes(Map<Integer, Map<String, Object>> results, List<CacheSizeValues> cacheSizeValues) {
      if (cacheSizeValues == null || cacheSizeValues.size() == 0) {
         log.info("Cache size values not collected. Skip join to results");
         return;
      }

      for (Map.Entry<Integer, Map<String, Object>> entry : results.entrySet()) {
         int slaveIdx = entry.getKey();
         Map<String, Object> slaveResults = entry.getValue();

         try {
            for (CacheSizeValues values : cacheSizeValues) {
               slaveResults.put(values.getStatName(), values.getCacheSize(slaveIdx));
            }
         } catch (Exception e) {
            log.warn("Exception occurs while join the cache size to results for slave index " + slaveIdx, e);
         }
      }
   }

   private void writeData(Map<Integer, Map<String, Object>> results) throws Exception {

      openFile();

      List<String> headerRow = new ArrayList<String>();
      Map<String, Object> aReport = results.get(0);
      headerRow.add("SLAVE_INDEX");
      headerRow.addAll(aReport.keySet());
      writeRowToFile(headerRow);

      List<Integer> slaveIndexes = new ArrayList<Integer>(results.keySet());
      Collections.sort(slaveIndexes);

      List<String> dataRow = new ArrayList<String>();
      for (Integer i : slaveIndexes) {
         Map<String, Object> reportPerSlave = results.get(i);
         if (reportPerSlave == null)
            throw new IllegalStateException("Missing report for slave index: " + i);
         dataRow.add(String.valueOf(i));//add the slave index first
         for (int j = 1; j < headerRow.size(); j++) {
            String header = headerRow.get(j);
            Object data = reportPerSlave.get(header);
            if (data == null)
               throw new IllegalStateException("Missing data for header: " + header + " from slave " + i + ". Report for this slave is: " + reportPerSlave);
            dataRow.add(String.valueOf(data));
         }
         writeRowToFile(dataRow);
         dataRow.clear();
      }

      closeFile();
   }

   private void closeFile() throws IOException {
      fileWriter.close();
   }

   private void openFile() throws IOException {
      fileWriter = new FileWriter(outputFile);
   }

   private void writeRowToFile(List<String> row) throws IOException {
      for (int i = 0; i < row.size(); i++) {
         fileWriter.write(row.get(i));
         if (i == row.size() - 1) {
            fileWriter.write('\n');
         } else {
            fileWriter.write(separator);
         }
      }
   }

   private void prepareOutputFile(int clusterSize) throws IOException {
      File parentDir;
      if (targetDir == null) {
         log.trace("Defaulting to local dir");
         parentDir = new File(".");
      } else {
         parentDir = new File(targetDir);
         if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
               log.warn("Issues creating parent dir " + parentDir);
            }
         }
      }
      assert parentDir.exists() && parentDir.isDirectory();

      String actualFileName = this.reportFileName(masterState);

      outputFile = Utils.createOrReplaceFile(parentDir, actualFileName);
   }

   public void setTargetDir(String targetDir) {
      this.targetDir = targetDir;
   }


   protected String reportFileName(MasterState masterState){
      return masterState.nameOfTheCurrentBenchmark() + "_" + masterState.configNameOfTheCurrentBenchmark() + "_" + masterState.getSlavesCountForCurrentStage() + ".csv";
   }
}
