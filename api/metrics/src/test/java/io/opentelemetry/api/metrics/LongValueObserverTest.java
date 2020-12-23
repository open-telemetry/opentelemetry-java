/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static io.opentelemetry.api.internal.StringUtils.METRIC_NAME_MAX_LENGTH;
import static io.opentelemetry.api.metrics.DefaultMeter.ERROR_MESSAGE_INVALID_NAME;
import static java.util.Arrays.fill;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongValueObserver}. */
class LongValueObserverTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = DefaultMeter.getInstance();

  @Test
  void preventNull_Name() {
    assertThatThrownBy(() -> meter.longValueObserverBuilder(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("name");
  }

  @Test
  void preventEmpty_Name() {
    assertThatThrownBy(() -> meter.longValueObserverBuilder("").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThatThrownBy(() -> meter.longValueObserverBuilder("\2").build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[METRIC_NAME_MAX_LENGTH + 1];
    fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThatThrownBy(() -> meter.longValueObserverBuilder(longName).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThatThrownBy(() -> meter.longValueObserverBuilder("metric").setDescription(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("description");
  }

  @Test
  void preventNull_Unit() {
    assertThatThrownBy(() -> meter.longValueObserverBuilder("metric").setUnit(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("unit");
  }

  @Test
  void preventNull_Callback() {
    assertThatThrownBy(() -> meter.longValueObserverBuilder("metric").setUpdater(null).build())
        .isInstanceOf(NullPointerException.class)
        .hasMessage("callback");
  }

  @Test
  void doesNotThrow() {
    meter
        .longValueObserverBuilder(NAME)
        .setDescription(DESCRIPTION)
        .setUnit(UNIT)
        .setUpdater(result -> {})
        .build();
  }
}
