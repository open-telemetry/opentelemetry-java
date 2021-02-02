/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.internal.MetricsStringUtils;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class LongCounterTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void preventNull_Name() {
    assertThatThrownBy(() -> meter.longCounterBuilder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }

  @Test
  void preventEmpty_Name() {
    assertThatThrownBy(() -> meter.longCounterBuilder("").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThatThrownBy(() -> meter.longCounterBuilder("\2").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[MetricsStringUtils.METRIC_NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThatThrownBy(() -> meter.longCounterBuilder(longName).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThatThrownBy(() -> meter.longCounterBuilder("metric").setDescription(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("description");
  }

  @Test
  void preventNull_Unit() {
    assertThatThrownBy(() -> meter.longCounterBuilder("metric").setUnit(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
  }

  @Test
  void add_PreventNullLabels() {
    assertThatThrownBy(() -> meter.longCounterBuilder("metric").build().add(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
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
    assertThatThrownBy(() -> longCounter.add(-1, Labels.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Counters can only increase");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(() -> meter.longCounterBuilder("metric").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
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
      assertThatThrownBy(() -> bound.add(-1))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Counters can only increase");
    } finally {
      bound.unbind();
    }
  }
}
