/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.metrics.DoubleUpDownCounter.BoundDoubleUpDownCounter;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DoubleUpDownCounterTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = OpenTelemetry.getGlobalMeter("DoubleUpDownCounterTest");

  @Test
  void preventNull_Name() {
    assertThrows(NullPointerException.class, () -> meter.doubleUpDownCounterBuilder(null), "name");
  }

  @Test
  void preventEmpty_Name() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleUpDownCounterBuilder("").build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleUpDownCounterBuilder("\2").build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[StringUtils.METRIC_NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleUpDownCounterBuilder(longName).build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleUpDownCounterBuilder("metric").setDescription(null).build(),
        "description");
  }

  @Test
  void preventNull_Unit() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleUpDownCounterBuilder("metric").setUnit(null).build(),
        "unit");
  }

  @Test
  void add_preventNullLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleUpDownCounterBuilder("metric").build().bind(null),
        "labels");
  }

  @Test
  void add_DoesNotThrow() {
    DoubleUpDownCounter doubleUpDownCounter =
        meter.doubleUpDownCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    doubleUpDownCounter.add(1.0, Labels.empty());
    doubleUpDownCounter.add(-1.0, Labels.empty());
    doubleUpDownCounter.add(1.0);
    doubleUpDownCounter.add(-1.0);
  }

  @Test
  void bound_PreventNullLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleUpDownCounterBuilder("metric").build().bind(null),
        "labels");
  }

  @Test
  void bound_DoesNotThrow() {
    DoubleUpDownCounter doubleUpDownCounter =
        meter.doubleUpDownCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    BoundDoubleUpDownCounter bound = doubleUpDownCounter.bind(Labels.empty());
    bound.add(1.0);
    bound.add(-1.0);
    bound.unbind();
  }
}
