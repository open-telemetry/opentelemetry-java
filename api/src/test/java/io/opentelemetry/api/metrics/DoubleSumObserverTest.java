/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.internal.StringUtils;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DoubleSumObserverTest {
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Meter meter = OpenTelemetry.getGlobalMeter("DoubleSumObserverTest");

  @Test
  void preventNull_Name() {
    assertThrows(NullPointerException.class, () -> meter.doubleSumObserverBuilder(null), "name");
  }

  @Test
  void preventEmpty_Name() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleSumObserverBuilder("").build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThrows(
        IllegalArgumentException.class, () -> meter.doubleSumObserverBuilder("\2").build());
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[StringUtils.METRIC_NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleSumObserverBuilder(longName).build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleSumObserverBuilder("metric").setDescription(null).build(),
        "description");
  }

  @Test
  void preventNull_Unit() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleSumObserverBuilder("metric").setUnit(null).build(),
        "unit");
  }

  @Test
  void preventNull_Callback() {
    DoubleSumObserver doubleSumObserver = meter.doubleSumObserverBuilder("metric").build();
    assertThrows(NullPointerException.class, () -> doubleSumObserver.setCallback(null), "callback");
  }

  @Test
  void doesNotThrow() {
    DoubleSumObserver doubleSumObserver =
        meter.doubleSumObserverBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    doubleSumObserver.setCallback(result -> {});
  }
}
