/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.metrics.DoubleCounter.BoundDoubleCounter;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DoubleCounterTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void preventNull_Name() {
    assertThrows(NullPointerException.class, () -> meter.doubleCounterBuilder(null), "name");
  }

  @Test
  void preventEmpty_Name() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleCounterBuilder("").build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleCounterBuilder("\2").build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[StringUtils.METRIC_NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleCounterBuilder(longName).build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleCounterBuilder("metric").setDescription(null).build(),
        "description");
  }

  @Test
  void preventNull_Unit() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleCounterBuilder("metric").setUnit(null).build(),
        "unit");
  }

  @Test
  void add_preventNullLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleCounterBuilder("metric").build().add(1.0, null),
        "labels");
  }

  @Test
  void add_DoesNotThrow() {
    DoubleCounter doubleCounter =
        meter.doubleCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    doubleCounter.add(1.0, Labels.empty());
    doubleCounter.add(1.0);
  }

  @Test
  void add_PreventNegativeValue() {
    DoubleCounter doubleCounter =
        meter.doubleCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    assertThrows(
        IllegalArgumentException.class,
        () -> doubleCounter.add(-1.0, Labels.empty()),
        "Counters can only increase");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleCounterBuilder("metric").build().bind(null),
        "labels");
  }

  @Test
  void bound_DoesNotThrow() {
    DoubleCounter doubleCounter =
        meter.doubleCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    BoundDoubleCounter bound = doubleCounter.bind(Labels.empty());
    bound.add(1.0);
    bound.unbind();
  }

  @Test
  void bound_PreventNegativeValue() {
    DoubleCounter doubleCounter =
        meter.doubleCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    BoundDoubleCounter bound = doubleCounter.bind(Labels.empty());
    try {
      assertThrows(
          IllegalArgumentException.class, () -> bound.add(-1.0), "Counters can only increase");
    } finally {
      bound.unbind();
    }
  }
}
