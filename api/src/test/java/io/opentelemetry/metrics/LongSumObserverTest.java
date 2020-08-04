/*
 * Copyright 2019, OpenTelemetry Authors
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

class LongSumObserverTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Labels CONSTANT_LABELS = Labels.of("key", "value");

  private final Meter meter = OpenTelemetry.getMeter("LongSumObserverTest");

  @Test
  void preventNull_Name() {
    assertThrows(NullPointerException.class, () -> meter.longSumObserverBuilder(null), "name");
  }

  @Test
  void preventEmpty_Name() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.longSumObserverBuilder("").build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.longSumObserverBuilder("\2").build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[StringUtils.NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.longSumObserverBuilder(longName).build(),
        DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longSumObserverBuilder("metric").setDescription(null).build(),
        "description");
  }

  @Test
  void preventNull_Unit() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longSumObserverBuilder("metric").setUnit(null).build(),
        "unit");
  }

  @Test
  void preventNull_ConstantLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longSumObserverBuilder("metric").setConstantLabels(null).build(),
        "constantLabels");
  }

  @Test
  void preventNull_Callback() {
    LongSumObserver longSumObserver = meter.longSumObserverBuilder("metric").build();
    assertThrows(NullPointerException.class, () -> longSumObserver.setCallback(null), "callback");
  }

  @Test
  void doesNotThrow() {
    LongSumObserver longSumObserver =
        meter
            .longSumObserverBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setConstantLabels(CONSTANT_LABELS)
            .build();
    longSumObserver.setCallback(result -> {});
  }
}
