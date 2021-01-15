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

class LongUpDownCounterTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void preventNull_Name() {
    assertThatThrownBy(() -> meter.longUpDownCounterBuilder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }

  @Test
  void preventEmpty_Name() {
    assertThatThrownBy(() -> meter.longUpDownCounterBuilder("").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThatThrownBy(() -> meter.longUpDownCounterBuilder("\2").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[StringUtils.METRIC_NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThatThrownBy(() -> meter.longUpDownCounterBuilder(longName).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThatThrownBy(() -> meter.longUpDownCounterBuilder("metric").setDescription(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("description");
  }

  @Test
  void preventNull_Unit() {
    assertThatThrownBy(() -> meter.longUpDownCounterBuilder("metric").setUnit(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
  }

  @Test
  void add_PreventNullLabels() {
    assertThatThrownBy(() -> meter.longUpDownCounterBuilder("metric").build().add(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void add_DoesNotThrow() {
    LongUpDownCounter longUpDownCounter =
        meter.longUpDownCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    longUpDownCounter.add(1, Labels.empty());
    longUpDownCounter.add(-1, Labels.empty());
    longUpDownCounter.add(1);
    longUpDownCounter.add(-1);
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(() -> meter.longUpDownCounterBuilder("metric").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void bound_DoesNotThrow() {
    LongUpDownCounter longUpDownCounter =
        meter.longUpDownCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    BoundLongUpDownCounter bound = longUpDownCounter.bind(Labels.empty());
    bound.add(1);
    bound.add(-1);
    bound.unbind();
  }
}
