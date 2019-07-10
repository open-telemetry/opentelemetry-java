/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.contrib.metrics.runtime;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.metrics.GaugeLong;
import io.opentelemetry.metrics.LabelKey;
import io.opentelemetry.metrics.LabelValue;
import io.opentelemetry.metrics.Meter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Arrays;
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
 *   jvm_memory_area{type="used",area="heap"} 2000000
 *   jvm_memory_area{type="committed",area="nonheap"} 200000
 *   jvm_memory_pool{type="used",pool="PS Eden Space"} 2000
 * </pre>
 */
public final class MemoryPools {
  private static final LabelKey TYPE = LabelKey.create("type", "");
  private static final LabelKey AREA = LabelKey.create("area", "");
  private static final LabelKey POOL = LabelKey.create("pool", "");
  private static final LabelValue USED = LabelValue.create("used");
  private static final LabelValue COMMITTED = LabelValue.create("committed");
  private static final LabelValue MAX = LabelValue.create("max");
  private static final LabelValue HEAP = LabelValue.create("heap");
  private static final LabelValue NON_HEAP = LabelValue.create("non_heap");

  private final MemoryMXBean memoryBean;
  private final List<MemoryPoolMXBean> poolBeans;
  private final Meter meter;

  /** Constructs a new module that is capable to export metrics about "jvm_memory". */
  public MemoryPools() {
    this.memoryBean = ManagementFactory.getMemoryMXBean();
    this.poolBeans = ManagementFactory.getMemoryPoolMXBeans();
    this.meter = OpenTelemetry.getMeter();
  }

  /** Export only the "area" metric. */
  public void exportMemoryAreaMetric() {
    // TODO: If support a callback per MetricRegistry (collection) this can be transformed into 3
    //  metrics and avoid using type, but type is nice that you can see what percent of the used
    //  memory is committed (this can also be achieved by displaying the two metrics in the same
    //  chart).
    final GaugeLong areaMetric =
        this.meter
            .gaugeLongBuilder("area")
            .setDescription("Bytes of a given JVM memory area.")
            .setUnit("By")
            .setLabelKeys(Arrays.asList(TYPE, AREA))
            .setComponent("jvm_memory")
            .build();
    final GaugeLong.TimeSeries usedHeap =
        areaMetric.getOrCreateTimeSeries(Arrays.asList(USED, HEAP));
    final GaugeLong.TimeSeries usedNonHeap =
        areaMetric.getOrCreateTimeSeries(Arrays.asList(USED, NON_HEAP));
    final GaugeLong.TimeSeries committedHeap =
        areaMetric.getOrCreateTimeSeries(Arrays.asList(COMMITTED, HEAP));
    final GaugeLong.TimeSeries committedNonHeap =
        areaMetric.getOrCreateTimeSeries(Arrays.asList(COMMITTED, NON_HEAP));
    // TODO: Decide if max is needed or not. May be derived with some approximation from max(used).
    final GaugeLong.TimeSeries maxHeap = areaMetric.getOrCreateTimeSeries(Arrays.asList(MAX, HEAP));
    final GaugeLong.TimeSeries maxNonHeap =
        areaMetric.getOrCreateTimeSeries(Arrays.asList(MAX, NON_HEAP));
    areaMetric.setCallback(
        new Runnable() {
          @Override
          public void run() {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            usedHeap.set(heapUsage.getUsed());
            usedNonHeap.set(nonHeapUsage.getUsed());
            committedHeap.set(heapUsage.getUsed());
            committedNonHeap.set(nonHeapUsage.getUsed());
            maxHeap.set(heapUsage.getUsed());
            maxNonHeap.set(nonHeapUsage.getUsed());
          }
        });
  }

  /** Export only the "pool" metric. */
  public void exportMemoryPoolMetric() {
    final GaugeLong poolMetric =
        this.meter
            .gaugeLongBuilder("pool")
            .setDescription("Bytes of a given JVM memory pool.")
            .setUnit("By")
            .setLabelKeys(Arrays.asList(TYPE, POOL))
            .setComponent("jvm_memory")
            .build();
    poolMetric.setCallback(
        new Runnable() {
          @Override
          public void run() {
            for (final MemoryPoolMXBean pool : poolBeans) {
              MemoryUsage poolUsage = pool.getUsage();
              LabelValue poolName = LabelValue.create(pool.getName());
              poolMetric
                  .getOrCreateTimeSeries(Arrays.asList(USED, poolName))
                  .set(poolUsage.getUsed());
              poolMetric
                  .getOrCreateTimeSeries(Arrays.asList(COMMITTED, poolName))
                  .set(poolUsage.getUsed());
              // TODO: Decide if max is needed or not. May be derived with some approximation from
              //  max(used).
              poolMetric
                  .getOrCreateTimeSeries(Arrays.asList(MAX, poolName))
                  .set(poolUsage.getUsed());
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
