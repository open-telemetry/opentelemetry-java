/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.DoubleValueRecorder.BoundDoubleValueRecorder;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DoubleValueRecorderTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void preventNull_Name() {
    assertThrows(NullPointerException.class, () -> meter.doubleValueRecorderBuilder(null), "name");
  }

  @Test
  void preventEmpty_Name() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleValueRecorderBuilder("").build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleValueRecorderBuilder("\2").build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[256];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleValueRecorderBuilder(longName).build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleValueRecorderBuilder("metric").setDescription(null).build(),
        "description");
  }

  @Test
  void preventNull_Unit() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleValueRecorderBuilder("metric").setUnit(null).build(),
        "unit");
  }

  @Test
  void record_PreventNullLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleValueRecorderBuilder("metric").build().record(1.0, null),
        "labels");
  }

  @Test
  void record_DoesNotThrow() {
    DoubleValueRecorder doubleValueRecorder =
        meter.doubleValueRecorderBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    doubleValueRecorder.record(5.0, Labels.empty());
    doubleValueRecorder.record(-5.0, Labels.empty());
    doubleValueRecorder.record(5.0);
    doubleValueRecorder.record(-5.0);
  }

  @Test
  void bound_PreventNullLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleValueRecorderBuilder("metric").build().bind(null),
        "labels");
  }

  @Test
  void bound_DoesNotThrow() {
    DoubleValueRecorder doubleValueRecorder =
        meter.doubleValueRecorderBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    BoundDoubleValueRecorder bound = doubleValueRecorder.bind(Labels.empty());
    bound.record(5.0);
    bound.record(-5.0);
    bound.unbind();
  }
}
