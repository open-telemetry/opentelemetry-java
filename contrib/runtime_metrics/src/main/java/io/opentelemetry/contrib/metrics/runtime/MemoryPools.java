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
import io.opentelemetry.metrics.LongObserver;
import io.opentelemetry.metrics.LongObserver.ResultLongObserver;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.Observer.Callback;
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
 *   jvm_memory_area{type="used",area="heap"} 2000000
 *   jvm_memory_area{type="committed",area="nonheap"} 200000
 *   jvm_memory_pool{type="used",pool="PS Eden Space"} 2000
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

  /** Constructs a new module that is capable to export metrics about "jvm_memory". */
  public MemoryPools() {
    this.memoryBean = ManagementFactory.getMemoryMXBean();
    this.poolBeans = ManagementFactory.getMemoryPoolMXBeans();
    this.meter = OpenTelemetry.getMeterProvider().get("jvm_memory");
  }

  /** Export only the "area" metric. */
  public void exportMemoryAreaMetric() {
    // TODO: Set this as non-monotonic.
    final LongObserver areaMetric =
        this.meter
            .longObserverBuilder("area")
            .setDescription("Bytes of a given JVM memory area.")
            .setUnit("By")
            .setMonotonic(false)
            .build();
    final String[] usedHeap = new String[] {TYPE_LABEL_KEY, USED, AREA_LABEL_KEY, HEAP};
    final String[] usedNonHeap = new String[] {TYPE_LABEL_KEY, USED, AREA_LABEL_KEY, NON_HEAP};
    final String[] committedHeap = new String[] {TYPE_LABEL_KEY, COMMITTED, AREA_LABEL_KEY, HEAP};
    final String[] committedNonHeap =
        new String[] {TYPE_LABEL_KEY, COMMITTED, AREA_LABEL_KEY, NON_HEAP};
    // TODO: Decide if max is needed or not. May be derived with some approximation from max(used).
    final String[] maxHeap = new String[] {TYPE_LABEL_KEY, MAX, AREA_LABEL_KEY, HEAP};
    final String[] maxNonHeap = new String[] {TYPE_LABEL_KEY, MAX, AREA_LABEL_KEY, NON_HEAP};
    areaMetric.setCallback(
        new LongObserver.Callback<ResultLongObserver>() {
          @Override
          public void update(ResultLongObserver resultLongObserver) {
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
    // TODO: Set this as non-monotonic.
    final LongObserver poolMetric =
        this.meter
            .longObserverBuilder("pool")
            .setDescription("Bytes of a given JVM memory pool.")
            .setUnit("By")
            .setMonotonic(false)
            .build();
    final List<String[]> usedLabelSets = new ArrayList<>(poolBeans.size());
    final List<String[]> committedLabelSets = new ArrayList<>(poolBeans.size());
    final List<String[]> maxLabelSets = new ArrayList<>(poolBeans.size());
    for (final MemoryPoolMXBean pool : poolBeans) {
      usedLabelSets.add(new String[] {TYPE_LABEL_KEY, USED, POOL_LABEL_KEY, pool.getName()});
      committedLabelSets.add(
          new String[] {TYPE_LABEL_KEY, COMMITTED, POOL_LABEL_KEY, pool.getName()});
      maxLabelSets.add(new String[] {TYPE_LABEL_KEY, MAX, POOL_LABEL_KEY, pool.getName()});
    }
    poolMetric.setCallback(
        new Callback<ResultLongObserver>() {
          @Override
          public void update(ResultLongObserver resultLongObserver) {
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
