/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static io.opentelemetry.api.metrics.DefaultMeter.ERROR_MESSAGE_INVALID_NAME;
import static java.util.Arrays.fill;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongValueRecorder.BoundLongValueRecorder;
import org.junit.jupiter.api.Test;

/** Tests for {@link LongValueRecorder}. */
public final class LongValueRecorderTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = OpenTelemetry.getGlobalMeter("LongValueRecorderTest");

  @Test
  void preventNull_Name() {
    assertThrows(NullPointerException.class, () -> meter.longValueRecorderBuilder(null), "name");
  }

  @Test
  void preventEmpty_Name() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.longValueRecorderBuilder("").build(),
        ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableMeasureName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.longValueRecorderBuilder("\2").build(),
        ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[256];
    fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.longValueRecorderBuilder(longName).build(),
        ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longValueRecorderBuilder("metric").setDescription(null).build(),
        "description");
  }

  @Test
  void preventNull_Unit() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longValueRecorderBuilder("metric").setUnit(null).build(),
        "unit");
  }

  @Test
  void record_PreventNullLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longValueRecorderBuilder("metric").build().record(1, null),
        "labels");
  }

  @Test
  void record_DoesNotThrow() {
    LongValueRecorder longValueRecorder =
        meter.longValueRecorderBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    longValueRecorder.record(5, Labels.empty());
    longValueRecorder.record(-5, Labels.empty());
    longValueRecorder.record(5);
    longValueRecorder.record(-5);
  }

  @Test
  void bound_PreventNullLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longValueRecorderBuilder("metric").build().bind(null),
        "labels");
  }

  @Test
  void bound_DoesNotThrow() {
    LongValueRecorder longValueRecorder =
        meter.longValueRecorderBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    BoundLongValueRecorder bound = longValueRecorder.bind(Labels.empty());
    bound.record(5);
    bound.record(-5);
    bound.unbind();
  }
}
