/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BatchRecorderTest {
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void testNewBatchRecorder_WrongNumberOfLabels() {
    assertThatThrownBy(() -> meter.newBatchRecorder("key"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("key/value");
  }

  @Test
  void testNewBatchRecorder_NullLabelKey() {
    assertThatThrownBy(() -> meter.newBatchRecorder(null, "value"))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("null keys");
  }

  @Test
  void preventNull_MeasureLong() {
    assertThatThrownBy(() -> meter.newBatchRecorder().put((LongValueRecorder) null, 5L).record())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("valueRecorder");
  }

  @Test
  void preventNull_MeasureDouble() {
    assertThatThrownBy(() -> meter.newBatchRecorder().put((DoubleValueRecorder) null, 5L).record())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("valueRecorder");
  }

  @Test
  void preventNull_LongCounter() {
    assertThatThrownBy(() -> meter.newBatchRecorder().put((LongCounter) null, 5L).record())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("counter");
  }

  @Test
  void preventNull_DoubleCounter() {
    assertThatThrownBy(() -> meter.newBatchRecorder().put((DoubleCounter) null, 5L).record())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("counter");
  }

  @Test
  void preventNull_LongUpDownCounter() {
    assertThatThrownBy(() -> meter.newBatchRecorder().put((LongUpDownCounter) null, 5L).record())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("upDownCounter");
  }

  @Test
  void preventNull_DoubleUpDownCounter() {
    assertThatThrownBy(() -> meter.newBatchRecorder().put((DoubleUpDownCounter) null, 5L).record())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("upDownCounter");
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
    assertThatThrownBy(
            () -> batchRecorder.put(meter.doubleCounterBuilder("doubleCounter").build(), -77.556d))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Counters can only increase");
  }

  @Test
  void negativeValue_LongCounter() {
    BatchRecorder batchRecorder = meter.newBatchRecorder();
    assertThatThrownBy(
            () -> batchRecorder.put(meter.longCounterBuilder("longCounter").build(), -44L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Counters can only increase");
  }
}
