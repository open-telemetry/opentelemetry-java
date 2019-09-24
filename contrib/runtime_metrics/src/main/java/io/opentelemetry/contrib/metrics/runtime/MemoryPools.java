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
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.Observer.Callback;
import io.opentelemetry.metrics.Observer.Handle;
import io.opentelemetry.metrics.ObserverLong;
import io.opentelemetry.metrics.ObserverLong.Result;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
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
    // TODO: Set component to "jvm_memory" when available.
    this.meter = OpenTelemetry.getMeter();
  }

  /** Export only the "area" metric. */
  public void exportMemoryAreaMetric() {
    // TODO: Set this as non-monotonic.
    final ObserverLong areaMetric =
        this.meter
            .observerLongBuilder("area")
            .setDescription("Bytes of a given JVM memory area.")
            .setUnit("By")
            .setLabelKeys(Arrays.asList(TYPE_LABEL_KEY, AREA_LABEL_KEY))
            .setMonotonic(false)
            .build();
    final Handle usedHeap = areaMetric.getHandle(Arrays.asList(USED, HEAP));
    final Handle usedNonHeap = areaMetric.getHandle(Arrays.asList(USED, NON_HEAP));
    final Handle committedHeap = areaMetric.getHandle(Arrays.asList(COMMITTED, HEAP));
    final Handle committedNonHeap = areaMetric.getHandle(Arrays.asList(COMMITTED, NON_HEAP));
    // TODO: Decide if max is needed or not. May be derived with some approximation from max(used).
    final Handle maxHeap = areaMetric.getHandle(Arrays.asList(MAX, HEAP));
    final Handle maxNonHeap = areaMetric.getHandle(Arrays.asList(MAX, NON_HEAP));
    areaMetric.setCallback(
        new ObserverLong.Callback<Result>() {
          @Override
          public void update(Result result) {
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            result.put(usedHeap, heapUsage.getUsed());
            result.put(usedNonHeap, nonHeapUsage.getUsed());
            result.put(committedHeap, heapUsage.getUsed());
            result.put(committedNonHeap, nonHeapUsage.getUsed());
            result.put(maxHeap, heapUsage.getUsed());
            result.put(maxNonHeap, nonHeapUsage.getUsed());
          }
        });
  }

  /** Export only the "pool" metric. */
  public void exportMemoryPoolMetric() {
    // TODO: Set this as non-monotonic.
    final ObserverLong poolMetric =
        this.meter
            .observerLongBuilder("pool")
            .setDescription("Bytes of a given JVM memory pool.")
            .setUnit("By")
            .setLabelKeys(Arrays.asList(TYPE_LABEL_KEY, POOL_LABEL_KEY))
            .setMonotonic(false)
            .build();
    final List<Handle> usedHandles = new ArrayList<>(poolBeans.size());
    final List<Handle> committedHandles = new ArrayList<>(poolBeans.size());
    final List<Handle> maxHandles = new ArrayList<>(poolBeans.size());
    for (final MemoryPoolMXBean pool : poolBeans) {
      usedHandles.add(poolMetric.getHandle(Arrays.asList(USED, pool.getName())));
      committedHandles.add(poolMetric.getHandle(Arrays.asList(COMMITTED, pool.getName())));
      maxHandles.add(poolMetric.getHandle(Arrays.asList(MAX, pool.getName())));
    }
    poolMetric.setCallback(
        new Callback<Result>() {
          @Override
          public void update(Result result) {
            for (int i = 0; i < poolBeans.size(); i++) {
              MemoryUsage poolUsage = poolBeans.get(i).getUsage();
              result.put(usedHandles.get(i), poolUsage.getUsed());
              result.put(committedHandles.get(i), poolUsage.getCommitted());
              // TODO: Decide if max is needed or not. May be derived with some approximation from
              //  max(used).
              result.put(maxHandles.get(i), poolUsage.getMax());
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
