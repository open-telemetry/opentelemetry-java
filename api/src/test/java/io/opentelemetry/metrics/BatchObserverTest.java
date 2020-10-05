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

package io.opentelemetry.metrics;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.metrics.BatchObserver.BatchObserverFunction;
import org.junit.jupiter.api.Test;

class BatchObserverTest {
  private static final Meter meter = DefaultMeter.getInstance();
  private static final BatchObserverFunction function = result -> {};
  private static final BatchObserver observer = meter.newBatchObserver("test", function);

  @Test
  void testNewBatchObserver_badFunction() {
    assertThrows(NullPointerException.class, () -> meter.newBatchObserver("key", null), "function");
  }

  @Test
  void preventNull_SumLong() {
    assertThrows(NullPointerException.class, () -> observer.longSumObserverBuilder(null), "name");
  }

  @Test
  void preventNull_SumDouble() {
    assertThrows(NullPointerException.class, () -> observer.doubleSumObserverBuilder(null), "name");
  }

  @Test
  void preventNull_UpDownLong() {
    assertThrows(
        NullPointerException.class, () -> observer.longUpDownSumObserverBuilder(null), "name");
  }

  @Test
  void preventNull_UpDownDouble() {
    assertThrows(
        NullPointerException.class, () -> observer.doubleUpDownSumObserverBuilder(null), "name");
  }

  @Test
  void preventNull_ValueLong() {
    assertThrows(NullPointerException.class, () -> observer.longValueObserverBuilder(null), "name");
  }

  @Test
  void preventNull_ValueDouble() {
    assertThrows(
        NullPointerException.class, () -> observer.doubleValueObserverBuilder(null), "name");
  }

  @Test
  void doesNotThrow() {
    observer.longValueObserverBuilder("longValueObserver").build().observation(44L);
    observer.longValueObserverBuilder("negativeLongValueObserver").build().observation(-44L);
    observer.doubleValueObserverBuilder("doubleValueObserver").build().observation(77.556d);
    observer
        .doubleValueObserverBuilder("negativeDoubleValueObserver")
        .build()
        .observation(-77.556d);

    observer.longUpDownSumObserverBuilder("longUpDownObserver").build().observation(44L);
    observer.longUpDownSumObserverBuilder("negativeLongUpDownObserver").build().observation(-44L);
    observer.doubleUpDownSumObserverBuilder("doubleUpDownObserver").build().observation(77.556d);
    observer
        .doubleUpDownSumObserverBuilder("negativeDoubleUpDownObserver")
        .build()
        .observation(-77.556d);

    observer.longSumObserverBuilder("longSumObserver").build().observation(44L);
    observer.longSumObserverBuilder("negativeLongSumObserver").build().observation(-44L);
    observer.doubleSumObserverBuilder("doubleSumObserver").build().observation(77.556d);
    observer.doubleSumObserverBuilder("negativeDoubleSumObserver").build().observation(-77.556d);
  }

  @Test
  void negativeValue_DoubleCounter() {
    BatchRecorder batchRecorder = meter.newBatchRecorder();
    assertThrows(
        IllegalArgumentException.class,
        () -> batchRecorder.put(meter.doubleCounterBuilder("doubleCounter").build(), -77.556d),
        "Counters can only increase");
  }

  @Test
  void negativeValue_LongCounter() {
    BatchRecorder batchRecorder = meter.newBatchRecorder();
    assertThrows(
        IllegalArgumentException.class,
        () -> batchRecorder.put(meter.longCounterBuilder("longCounter").build(), -44L),
        "Counters can only increase");
  }
}
