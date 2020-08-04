/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.metrics;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.Labels;
import io.opentelemetry.internal.StringUtils;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class DoubleUpDownSumObserverTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Labels CONSTANT_LABELS = Labels.of("key", "value");

  private final Meter meter = OpenTelemetry.getMeter("DoubleUpDownSumObserverTest");

  @Test
  void preventNull_Name() {
    assertThrows(
        NullPointerException.class, () -> meter.doubleUpDownSumObserverBuilder(null), "name");
  }

  @Test
  void preventEmpty_Name() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleUpDownSumObserverBuilder("").build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThrows(
        IllegalArgumentException.class, () -> meter.doubleUpDownSumObserverBuilder("\2").build());
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[StringUtils.NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.doubleUpDownSumObserverBuilder(longName).build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleUpDownSumObserverBuilder("metric").setDescription(null).build(),
        "description");
  }

  @Test
  void preventNull_Unit() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleUpDownSumObserverBuilder("metric").setUnit(null).build(),
        "unit");
  }

  @Test
  void preventNull_ConstantLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.doubleUpDownSumObserverBuilder("metric").setConstantLabels(null).build(),
        "constantLabels");
  }

  @Test
  void preventNull_Callback() {
    DoubleUpDownSumObserver doubleUpDownSumObserver =
        meter.doubleUpDownSumObserverBuilder("metric").build();
    assertThrows(
        NullPointerException.class, () -> doubleUpDownSumObserver.setCallback(null), "callback");
  }

  @Test
  void doesNotThrow() {
    DoubleUpDownSumObserver doubleUpDownSumObserver =
        meter
            .doubleUpDownSumObserverBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setConstantLabels(CONSTANT_LABELS)
            .build();
    doubleUpDownSumObserver.setCallback(result -> {});
  }
}
