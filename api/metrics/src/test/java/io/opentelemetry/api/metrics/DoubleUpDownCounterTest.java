/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.api.metrics.internal.MetricsStringUtils;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DoubleUpDownCounterTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void preventNull_Name() {
    assertThatThrownBy(() -> meter.doubleUpDownCounterBuilder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }

  @Test
  void preventEmpty_Name() {
    assertThatThrownBy(() -> meter.doubleUpDownCounterBuilder("").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThatThrownBy(() -> meter.doubleUpDownCounterBuilder("\2").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[MetricsStringUtils.METRIC_NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThatThrownBy(() -> meter.doubleUpDownCounterBuilder(longName).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThatThrownBy(
            () -> meter.doubleUpDownCounterBuilder("metric").setDescription(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("description");
  }

  @Test
  void preventNull_Unit() {
    assertThatThrownBy(() -> meter.doubleUpDownCounterBuilder("metric").setUnit(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
  }

  @Test
  void add_preventNullLabels() {
    assertThatThrownBy(() -> meter.doubleUpDownCounterBuilder("metric").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
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
    assertThatThrownBy(() -> meter.doubleUpDownCounterBuilder("metric").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
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
