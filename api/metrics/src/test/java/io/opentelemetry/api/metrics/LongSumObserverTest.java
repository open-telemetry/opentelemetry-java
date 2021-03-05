/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.metrics.internal.MetricsStringUtils;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class LongSumObserverTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void preventNull_Name() {
    assertThatThrownBy(() -> meter.longSumObserverBuilder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }

  @Test
  void preventEmpty_Name() {
    assertThatThrownBy(() -> meter.longSumObserverBuilder("").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThatThrownBy(() -> meter.longSumObserverBuilder("\2").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[MetricsStringUtils.METRIC_NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThatThrownBy(() -> meter.longSumObserverBuilder(longName).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThatThrownBy(() -> meter.longSumObserverBuilder("metric").setDescription(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("description");
  }

  @Test
  void preventNull_Unit() {
    assertThatThrownBy(() -> meter.longSumObserverBuilder("metric").setUnit(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
  }

  @Test
  void preventNull_Callback() {
    assertThatThrownBy(() -> meter.longSumObserverBuilder("metric").setUpdater(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("callback");
  }

  @Test
  void doesNotThrow() {
    meter
        .longSumObserverBuilder(NAME)
        .setDescription(DESCRIPTION)
        .setUnit(UNIT)
        .setUpdater(result -> {})
        .build();
  }
}
