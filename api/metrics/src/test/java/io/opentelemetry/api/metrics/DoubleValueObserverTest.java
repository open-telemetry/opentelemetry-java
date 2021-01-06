/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.internal.StringUtils;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DoubleValueObserverTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void preventNull_Name() {
    assertThatThrownBy(() -> meter.doubleValueObserverBuilder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }

  @Test
  void preventEmpty_Name() {
    assertThatThrownBy(() -> meter.doubleValueObserverBuilder("").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThatThrownBy(() -> meter.doubleValueObserverBuilder("\2").build())
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[StringUtils.METRIC_NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThatThrownBy(() -> meter.doubleValueObserverBuilder(longName).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThatThrownBy(
            () -> meter.doubleValueObserverBuilder("metric").setDescription(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("description");
  }

  @Test
  void preventNull_Unit() {
    assertThatThrownBy(() -> meter.doubleValueObserverBuilder("metric").setUnit(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
  }

  @Test
  void preventNull_Callback() {
    assertThatThrownBy(() -> meter.doubleValueObserverBuilder("metric").setUpdater(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("callback");
  }

  @Test
  void doesNotThrow() {
    meter
        .doubleValueObserverBuilder(NAME)
        .setDescription(DESCRIPTION)
        .setUnit(UNIT)
        .setUpdater(result -> {})
        .build();
  }
}
