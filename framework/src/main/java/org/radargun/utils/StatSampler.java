package org.radargun.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * Author: Diego Didona
 * Email: didona@gsd.inesc-id.pt
 * Websiste: www.cloudtm.eu
 * Date: 21/05/12
 */
public class StatSampler {

   private final CpuStat cpu;
   private final MemoryStat memory;
   private final Timer timer;
   private long interval;

   private final LinkedList<Long> usedMemories = new LinkedList<Long>();
   private final LinkedList<Double> usedCpu = new LinkedList<Double>();

   private static final Log log = LogFactory.getLog(StatSampler.class);

   public StatSampler(long interval) {
      this.interval = interval;
      cpu = new ProcCpuStat();
      memory = new MemoryStat();
      timer = new Timer();
      this.timer.schedule(new Collector(), interval, interval);
   }

   public double getAvgCpuUsage() {
      double cpu = 0D, samples = 0;
      for (Double d : usedCpu) {
         if (!Double.isNaN(d)) {
            cpu += d;
            samples++;
         }
      }
      if (samples > 0)
         return cpu / samples;
      return 0;
   }

   public double getAvgMemUsage() {
      double mem = 0D, samples = 0, temp;
      for (Long l : usedMemories) {
         temp = (double) l;
         if (!Double.isNaN(temp)) {
            mem += l;
            samples++;
         }
      }
      if (samples > 0)
         return mem / samples;
      return 0;
   }

   /**
    * cancels the current timer task
    */
   public final void cancel() {
      if(log.isTraceEnabled()) log.trace("Cancel timer task");
      timer.cancel();
   }

   /**
    * start a timer task.
    */
   public final void start() {
      if(log.isTraceEnabled()) log.trace("Start timer task");
      timer.schedule(new Collector(), 0, interval);
   }

   /**
    * reset the samples
    */
   public final void reset() {
      if(log.isTraceEnabled()) log.trace("Reset samples collected");
      usedMemories.clear();
      usedCpu.clear();
   }

   public final List<Long> getMemoryUsageHistory() {
      return Collections.unmodifiableList(usedMemories);
   }

   public final List<Double> getCpuUsageHistory() {
      return Collections.unmodifiableList(usedCpu);
   }

   @Override
   public String toString() {
      return "StatSampler{" +
              "interval=" + interval +
              ", usedMemories=" + usedMemories +
              ", usedCpu=" + usedCpu +
              '}';
   }

   private class Collector extends TimerTask {
      @Override
      public void run() {
         double cpuValue = cpu.getCpuUsageAndReset();
         long memValue = memory.getUsedMemory();
         if(log.isTraceEnabled()) log.trace("Collecting memory and cpu usage. Memory usage is " + memValue + " and CPU usage is " + cpuValue);
         if (!Double.isNaN(cpuValue))
            usedCpu.addLast(cpuValue);
         usedMemories.addLast(memValue);

      }
   }
}
