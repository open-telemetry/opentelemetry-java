/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.contrib.metrics.system;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.metrics.AsynchronousInstrument.Callback;
import io.opentelemetry.metrics.AsynchronousInstrument.DoubleResult;
import io.opentelemetry.metrics.AsynchronousInstrument.LongResult;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

/** Add me. */
public final class SystemMetrics {
  private static final String TYPE_LABEL_KEY = "type";

  private final IntervalMetricReader intervalReader;

  private SystemMetrics(
      Meter meter, MetricExporter exporter, Map<String, String> labels, int interval) {

    registerObservers(meter, labels);
    intervalReader =
        IntervalMetricReader.builder()
            .setExportIntervalMillis(interval)
            .setMetricExporter(exporter)
            .build();
  }

  public void shutdown() {
    intervalReader.shutdown();
  }

  void registerObservers(Meter meter, Map<String, String> labels) {
    SystemInfo systemInfo = new SystemInfo();
    OperatingSystem osInfo = systemInfo.getOperatingSystem();

    final HardwareAbstractionLayer hal = systemInfo.getHardware();
    final OSProcess processInfo = osInfo.getProcess(osInfo.getProcessId());

    meter
        .longValueObserverBuilder("system.mem")
        .setDescription("System memory")
        .setUnit("bytes")
        .setConstantLabels(labels)
        .build()
        .setCallback(
            new Callback<LongResult>() {
              @Override
              public void update(LongResult r) {
                GlobalMemory mem = hal.getMemory();
                r.observe(mem.getTotal(), TYPE_LABEL_KEY, "total");
                r.observe(mem.getAvailable(), TYPE_LABEL_KEY, "available");
              }
            });

    meter
        .doubleValueObserverBuilder("system.cpu")
        .setDescription("System CPU")
        .setUnit("seconds")
        .setConstantLabels(labels)
        .build()
        .setCallback(
            new Callback<DoubleResult>() {
              @Override
              public void update(DoubleResult r) {
                long[] cpuTicks = hal.getProcessor().getSystemCpuLoadTicks();
                r.observe(cpuTicks[TickType.SYSTEM.getIndex()] * 1000, TYPE_LABEL_KEY, "system");
                r.observe(cpuTicks[TickType.USER.getIndex()] * 1000, TYPE_LABEL_KEY, "user");
                r.observe(cpuTicks[TickType.IDLE.getIndex()] * 1000, TYPE_LABEL_KEY, "idle");
              }
            });

    meter
        .longValueObserverBuilder("sys.net.bytes")
        .setDescription("System network bytes")
        .setUnit("bytes")
        .setConstantLabels(labels)
        .build()
        .setCallback(
            new Callback<LongResult>() {
              @Override
              public void update(LongResult r) {
                long recv = 0;
                long sent = 0;

                for (NetworkIF networkIf : hal.getNetworkIFs()) {
                  recv += networkIf.getBytesRecv();
                  sent += networkIf.getBytesSent();
                }

                r.observe(recv, TYPE_LABEL_KEY, "bytes_recv");
                r.observe(sent, TYPE_LABEL_KEY, "bytes_sent");
              }
            });

    meter
        .longValueObserverBuilder("runtime.java.mem")
        .setDescription("Runtime memory")
        .setUnit("bytes")
        .setConstantLabels(labels)
        .build()
        .setCallback(
            new Callback<LongResult>() {
              @Override
              public void update(LongResult r) {
                processInfo.updateAttributes();
                r.observe(processInfo.getResidentSetSize(), TYPE_LABEL_KEY, "rss");
                r.observe(processInfo.getVirtualSize(), TYPE_LABEL_KEY, "vms");
              }
            });

    meter
        .doubleValueObserverBuilder("runtime.java.cpu")
        .setDescription("Runtime CPU")
        .setUnit("seconds")
        .setConstantLabels(labels)
        .build()
        .setCallback(
            new Callback<DoubleResult>() {
              @Override
              public void update(DoubleResult r) {
                processInfo.updateAttributes();
                r.observe(processInfo.getUserTime() * 1000, TYPE_LABEL_KEY, "user");
                r.observe(processInfo.getKernelTime() * 1000, TYPE_LABEL_KEY, "system");
              }
            });

    meter
        .longValueObserverBuilder("runtime.java.gc")
        .setDescription("Runtime GC")
        .setUnit("objects")
        .setConstantLabels(labels)
        .build()
        .setCallback(
            new Callback<LongResult>() {
              @Override
              public void update(LongResult r) {
                long gcCount = 0;
                for (final GarbageCollectorMXBean gcBean :
                    ManagementFactory.getGarbageCollectorMXBeans()) {
                  gcCount += gcBean.getCollectionCount();
                }

                r.observe(gcCount, TYPE_LABEL_KEY, "count");
              }
            });
  }

  /** Add me. */
  public static final class Builder {
    private Meter meter = OpenTelemetry.getMeterProvider().get(SystemMetrics.class.getName());
    private MetricExporter exporter;
    private Map<String, String> labels;
    private int interval;

    /** Add me. */
    public Builder setMeter(Meter meter) {
      this.meter = meter;
      return this;
    }

    /** Add me. */
    public Builder setExporter(MetricExporter exporter) {
      this.exporter = exporter;
      return this;
    }

    /** Add me. */
    public Builder setConstantLabels(Map<String, String> labels) {
      this.labels = Collections.unmodifiableMap(new TreeMap<>(labels));
      return this;
    }

    /** Add me. */
    public Builder setInterval(int interval) {
      Utils.checkArgument(interval > 0, "Interval must be positive");
      this.interval = interval;
      return this;
    }

    /** Add me. */
    public SystemMetrics build() {
      return new SystemMetrics(meter, exporter, labels, interval);
    }
  }
}
