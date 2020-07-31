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

import static io.opentelemetry.internal.StringUtils.NAME_MAX_LENGTH;
import static io.opentelemetry.metrics.DefaultMeter.ERROR_MESSAGE_INVALID_NAME;
import static java.util.Arrays.fill;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.Labels;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongValueObserver}. */
class LongValueObserverTest {

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Labels CONSTANT_LABELS = Labels.of("key", "value");

  private final Meter meter = OpenTelemetry.getMeter("LongValueObserverTest");

  @Test
  void preventNull_Name() {
    assertThrows(NullPointerException.class, () -> meter.longValueObserverBuilder(null), "name");
  }

  @Test
  void preventEmpty_Name() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.longValueObserverBuilder("").build(),
        ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNonPrintableName() {
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.longValueObserverBuilder("\2").build(),
        ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventTooLongName() {
    char[] chars = new char[NAME_MAX_LENGTH + 1];
    fill(chars, 'a');
    String longName = String.valueOf(chars);
    assertThrows(
        IllegalArgumentException.class,
        () -> meter.longValueObserverBuilder(longName).build(),
        ERROR_MESSAGE_INVALID_NAME);
  }

  @Test
  void preventNull_Description() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longValueObserverBuilder("metric").setDescription(null).build(),
        "description");
  }

  @Test
  void preventNull_Unit() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longValueObserverBuilder("metric").setUnit(null).build(),
        "unit");
  }

  @Test
  void preventNull_ConstantLabels() {
    assertThrows(
        NullPointerException.class,
        () -> meter.longValueObserverBuilder("metric").setConstantLabels(null).build(),
        "constantLabels");
  }

  @Test
  void preventNull_Callback() {
    LongValueObserver longValueObserver = meter.longValueObserverBuilder("metric").build();
    assertThrows(NullPointerException.class, () -> longValueObserver.setCallback(null), "callback");
  }

  @Test
  void doesNotThrow() {
    LongValueObserver longValueObserver =
        meter
            .longValueObserverBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setConstantLabels(CONSTANT_LABELS)
            .build();
    longValueObserver.setCallback(result -> {});
  }
}
