/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.metrics.LongCounter.BoundLongCounter;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class LongCounterTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = OpenTelemetry.getGlobalMeter("LongCounterTest");

  @Test
  void preventNull_Name() {
    assertThrows(NullPointerException.class, () -> meter.longCounterBuilder(null), "name");
  }

  @Test
  void preventEmpty_Name() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.longCounterBuilder("").build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.longCounterBuilder("\2").build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[StringUtils.METRIC_NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.longCounterBuilder(longName).build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longCounterBuilder("metric").setDescription(null).build(),
        "description");
  }

  @Test
  void preventNull_Unit() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longCounterBuilder("metric").setUnit(null).build(),
        "unit");
  }

  @Test
  void add_PreventNullLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longCounterBuilder("metric").build().add(1, null),
        "labels");
  }

  @Test
  void add_DoesNotThrow() {
    LongCounter longCounter =
        meter.longCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    longCounter.add(1, Labels.empty());
    longCounter.add(1);
  }

  @Test
  void add_PreventNegativeValue() {
    LongCounter longCounter =
        meter.longCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    assertThrows(
        IllegalArgumentException.class,
        () -> longCounter.add(-1, Labels.empty()),
        "Counters can only increase");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longCounterBuilder("metric").build().bind(null),
        "labels");
  }

  @Test
  void bound_DoesNotThrow() {
    LongCounter longCounter =
        meter.longCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    BoundLongCounter bound = longCounter.bind(Labels.empty());
    bound.add(1);
    bound.unbind();
  }

  @Test
  void bound_PreventNegativeValue() {
    LongCounter longCounter =
        meter.longCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    BoundLongCounter bound = longCounter.bind(Labels.empty());
    try {
      assertThrows(
          IllegalArgumentException.class, () -> bound.add(-1), "Counters can only increase");
    } finally {
      bound.unbind();
    }
  }
}
