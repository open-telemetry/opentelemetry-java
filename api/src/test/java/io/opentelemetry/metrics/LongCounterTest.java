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

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.common.Labels;
import io.opentelemetry.internal.StringUtils;
import io.opentelemetry.metrics.LongCounter.BoundLongCounter;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LongCounter}. */
@RunWith(JUnit4.class)
public class LongCounterTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Labels CONSTANT_LABELS = Labels.of("key", "value");

  private final Meter meter = OpenTelemetry.getMeter("LongCounterTest");

  @Test
  public void preventNull_Name() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    meter.longCounterBuilder(null);
  }

  @Test
  public void preventEmpty_Name() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.longCounterBuilder("").build();
  }

  @Test
  public void preventNonPrintableName() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.longCounterBuilder("\2").build();
  }

  @Test
  public void preventTooLongName() {
    char[] chars = new char[StringUtils.NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.longCounterBuilder(longName).build();
  }

  @Test
  public void preventNull_Description() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    meter.longCounterBuilder("metric").setDescription(null).build();
  }

  @Test
  public void preventNull_Unit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    meter.longCounterBuilder("metric").setUnit(null).build();
  }

  @Test
  public void preventNull_ConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    meter.longCounterBuilder("metric").setConstantLabels(null).build();
  }

  @Test
  public void add_PreventNullLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labels");
    meter.longCounterBuilder("metric").build().add(1, null);
  }

  @Test
  public void add_DoesNotThrow() {
    LongCounter longCounter =
        meter.longCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    longCounter.add(1, Labels.empty());
  }

  @Test
  public void add_PreventNegativeValue() {
    LongCounter longCounter =
        meter.longCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Counters can only increase");
    longCounter.add(-1, Labels.empty());
  }

  @Test
  public void bound_PreventNullLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labels");
    meter.longCounterBuilder("metric").build().bind(null);
  }

  @Test
  public void bound_DoesNotThrow() {
    LongCounter longCounter =
        meter
            .longCounterBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setConstantLabels(CONSTANT_LABELS)
            .build();
    BoundLongCounter bound = longCounter.bind(Labels.empty());
    bound.add(1);
    bound.unbind();
  }

  @Test
  public void bound_PreventNegativeValue() {
    LongCounter longCounter =
        meter
            .longCounterBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setConstantLabels(CONSTANT_LABELS)
            .build();
    BoundLongCounter bound = longCounter.bind(Labels.empty());
    try {
      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("Counters can only increase");
      bound.add(-1);
    } finally {
      bound.unbind();
    }
  }
}
