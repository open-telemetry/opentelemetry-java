/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.internal.StringUtils;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DoubleCounterTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void preventNull_Name() {
    assertThatThrownBy(() -> meter.doubleCounterBuilder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }

  @Test
  void preventEmpty_Name() {
    assertThatThrownBy(() -> meter.doubleCounterBuilder("").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThatThrownBy(() -> meter.doubleCounterBuilder("\2").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[StringUtils.METRIC_NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThatThrownBy(() -> meter.doubleCounterBuilder(longName).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThatThrownBy(() -> meter.doubleCounterBuilder("metric").setDescription(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("description");
  }

  @Test
  void preventNull_Unit() {
    assertThatThrownBy(() -> meter.doubleCounterBuilder("metric").setUnit(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
  }

  @Test
  void add_preventNullLabels() {
    assertThatThrownBy(() -> meter.doubleCounterBuilder("metric").build().add(1.0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
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
    assertThatThrownBy(() -> doubleCounter.add(-1.0, Labels.empty()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Counters can only increase");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(() -> meter.doubleCounterBuilder("metric").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
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
      assertThatThrownBy(() -> bound.add(-1.0))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Counters can only increase");
    } finally {
      bound.unbind();
    }
  }
}
