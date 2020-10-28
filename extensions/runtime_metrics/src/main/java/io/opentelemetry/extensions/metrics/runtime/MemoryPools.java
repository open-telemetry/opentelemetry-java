/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extensions.metrics.runtime;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.AsynchronousInstrument;
import io.opentelemetry.api.metrics.AsynchronousInstrument.LongResult;
import io.opentelemetry.api.metrics.LongUpDownSumObserver;
import io.opentelemetry.api.metrics.Meter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports metrics about JVM memory areas.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * new MemoryPools().exportAll();
 * }</pre>
 *
 * <p>Example metrics being exported: Component
 *
 * <pre>
 *   runtime.jvm.memory.area{type="used",area="heap"} 2000000
 *   runtime.jvm.memory.area{type="committed",area="nonheap"} 200000
 *   runtime.jvm.memory.area{type="used",pool="PS Eden Space"} 2000
 * </pre>
 */
public final class MemoryPools {
  private static final String TYPE_LABEL_KEY = "type";
  private static final String AREA_LABEL_KEY = "area";
  private static final String POOL_LABEL_KEY = "pool";
  private static final String USED = "used";
  private static final String COMMITTED = "committed";
  private static final String MAX = "max";
  private static final String HEAP = "heap";
  private static final String NON_HEAP = "non_heap";

  private final MemoryMXBean memoryBean;
  private final List<MemoryPoolMXBean> poolBeans;
  private final Meter meter;

  /** Constructs a new module that will export metrics in the "runtime.jvm.memory" namespace. */
  public MemoryPools() {
    this.memoryBean = ManagementFactory.getMemoryMXBean();
    this.poolBeans = ManagementFactory.getMemoryPoolMXBeans();
    this.meter = OpenTelemetry.getGlobalMeter("io.opentelemetry.extensions.metrics.runtime.memory");
  }

  /** Export only the "area" metric. */
  public void exportMemoryAreaMetric() {
    final LongUpDownSumObserver areaMetric =
        this.meter
            .longUpDownSumObserverBuilder("runtime.jvm.memory.area")
            .setDescription("Bytes of a given JVM memory area.")
            .setUnit("By")
            .build();
    final Labels usedHeap = Labels.of(TYPE_LABEL_KEY, USED, AREA_LABEL_KEY, HEAP);
    final Labels usedNonHeap = Labels.of(TYPE_LABEL_KEY, USED, AREA_LABEL_KEY, NON_HEAP);
    final Labels committedHeap = Labels.of(TYPE_LABEL_KEY, COMMITTED, AREA_LABEL_KEY, HEAP);
    final Labels committedNonHeap = Labels.of(TYPE_LABEL_KEY, COMMITTED, AREA_LABEL_KEY, NON_HEAP);
    // TODO: Decide if max is needed or not. May be derived with some approximation from max(used).
    final Labels maxHeap = Labels.of(TYPE_LABEL_KEY, MAX, AREA_LABEL_KEY, HEAP);
    final Labels maxNonHeap = Labels.of(TYPE_LABEL_KEY, MAX, AREA_LABEL_KEY, NON_HEAP);
    areaMetric.setCallback(
        new AsynchronousInstrument.Callback<LongResult>() {
          @Override
          public void update(LongResult resultLongObserver) {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            resultLongObserver.observe(heapUsage.getUsed(), usedHeap);
            resultLongObserver.observe(nonHeapUsage.getUsed(), usedNonHeap);
            resultLongObserver.observe(heapUsage.getUsed(), committedHeap);
            resultLongObserver.observe(nonHeapUsage.getUsed(), committedNonHeap);
            resultLongObserver.observe(heapUsage.getUsed(), maxHeap);
            resultLongObserver.observe(nonHeapUsage.getUsed(), maxNonHeap);
          }
        });
  }

  /** Export only the "pool" metric. */
  public void exportMemoryPoolMetric() {
    final LongUpDownSumObserver poolMetric =
        this.meter
            .longUpDownSumObserverBuilder("runtime.jvm.memory.pool")
            .setDescription("Bytes of a given JVM memory pool.")
            .setUnit("By")
            .build();
    final List<Labels> usedLabelSets = new ArrayList<>(poolBeans.size());
    final List<Labels> committedLabelSets = new ArrayList<>(poolBeans.size());
    final List<Labels> maxLabelSets = new ArrayList<>(poolBeans.size());
    for (final MemoryPoolMXBean pool : poolBeans) {
      usedLabelSets.add(Labels.of(TYPE_LABEL_KEY, USED, POOL_LABEL_KEY, pool.getName()));
      committedLabelSets.add(Labels.of(TYPE_LABEL_KEY, COMMITTED, POOL_LABEL_KEY, pool.getName()));
      maxLabelSets.add(Labels.of(TYPE_LABEL_KEY, MAX, POOL_LABEL_KEY, pool.getName()));
    }
    poolMetric.setCallback(
        new AsynchronousInstrument.Callback<LongResult>() {
          @Override
          public void update(LongResult resultLongObserver) {
            for (int i = 0; i < poolBeans.size(); i++) {
              MemoryUsage poolUsage = poolBeans.get(i).getUsage();
              resultLongObserver.observe(poolUsage.getUsed(), usedLabelSets.get(i));
              resultLongObserver.observe(poolUsage.getCommitted(), committedLabelSets.get(i));
              // TODO: Decide if max is needed or not. May be derived with some approximation from
              //  max(used).
              resultLongObserver.observe(poolUsage.getMax(), maxLabelSets.get(i));
            }
          }
        });
  }

  /** Export all metrics generated by this module. */
  public void exportAll() {
    exportMemoryAreaMetric();
    exportMemoryPoolMetric();
  }
}
