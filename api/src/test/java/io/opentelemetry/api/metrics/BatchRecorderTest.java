/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BatchRecorderTest {
  private static final Meter meter = Meter.getDefault();

  @Test
  void testNewBatchRecorder_badLabelSet() {
    assertThrows(IllegalArgumentException.class, () -> meter.newBatchRecorder("key"), "key/value");
  }

  @Test
  void preventNull_MeasureLong() {
    assertThrows(
        NullPointerException.class,
        () -> meter.newBatchRecorder().put((LongValueRecorder) null, 5L).record(),
        "valueRecorder");
  }

  @Test
  void preventNull_MeasureDouble() {
    assertThrows(
        NullPointerException.class,
        () -> meter.newBatchRecorder().put((DoubleValueRecorder) null, 5L).record(),
        "valueRecorder");
  }

  @Test
  void preventNull_LongCounter() {
    assertThrows(
        NullPointerException.class,
        () -> meter.newBatchRecorder().put((LongCounter) null, 5L).record(),
        "counter");
  }

  @Test
  void preventNull_DoubleCounter() {
    assertThrows(
        NullPointerException.class,
        () -> meter.newBatchRecorder().put((DoubleCounter) null, 5L).record(),
        "counter");
  }

  @Test
  void preventNull_LongUpDownCounter() {
    assertThrows(
        NullPointerException.class,
        () -> meter.newBatchRecorder().put((LongUpDownCounter) null, 5L).record(),
        "upDownCounter");
  }

  @Test
  void preventNull_DoubleUpDownCounter() {
    assertThrows(
        NullPointerException.class,
        () -> meter.newBatchRecorder().put((DoubleUpDownCounter) null, 5L).record(),
        "upDownCounter");
  }

  @Test
  void doesNotThrow() {
    BatchRecorder batchRecorder = meter.newBatchRecorder();
    batchRecorder.put(meter.longValueRecorderBuilder("longValueRecorder").build(), 44L);
    batchRecorder.put(meter.longValueRecorderBuilder("negativeLongValueRecorder").build(), -44L);
    batchRecorder.put(meter.doubleValueRecorderBuilder("doubleValueRecorder").build(), 77.556d);
    batchRecorder.put(
        meter.doubleValueRecorderBuilder("negativeDoubleValueRecorder").build(), -77.556d);
    batchRecorder.put(meter.longCounterBuilder("longCounter").build(), 44L);
    batchRecorder.put(meter.doubleCounterBuilder("doubleCounter").build(), 77.556d);
    batchRecorder.put(meter.longUpDownCounterBuilder("longUpDownCounter").build(), -44L);
    batchRecorder.put(meter.doubleUpDownCounterBuilder("doubleUpDownCounter").build(), -77.556d);
    batchRecorder.record();
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
