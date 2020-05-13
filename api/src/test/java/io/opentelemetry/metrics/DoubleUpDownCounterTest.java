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

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.internal.StringUtils;
import io.opentelemetry.metrics.DoubleUpDownCounter.BoundDoubleUpDownCounter;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DoubleCounter}. */
@RunWith(JUnit4.class)
public class DoubleUpDownCounterTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";

  private final Meter meter = OpenTelemetry.getMeter("counter_double_test");

  @Test
  public void preventNonPrintableName() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.doubleUpDownCounterBuilder("\2").build();
  }

  @Test
  public void preventTooLongName() {
    char[] chars = new char[StringUtils.NAME_MAX_LENGTH + 1];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.doubleUpDownCounterBuilder(longName).build();
  }

  @Test
  public void preventNull_Description() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    meter.doubleUpDownCounterBuilder("metric").setDescription(null).build();
  }

  @Test
  public void preventNull_Unit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    meter.doubleUpDownCounterBuilder("metric").setUnit(null).build();
  }

  @Test
  public void preventNull_ConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    meter.doubleUpDownCounterBuilder("metric").setConstantLabels(null).build();
  }

  @Test
  public void noopBind_WithBadLabelSet() {
    DoubleUpDownCounter doubleUpDownCounter =
        meter.doubleUpDownCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("key/value");
    doubleUpDownCounter.bind("key");
  }

  @Test
  public void addDoesNotThrow() {
    DoubleUpDownCounter doubleUpDownCounter =
        meter.doubleUpDownCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    doubleUpDownCounter.add(1.0);
    doubleUpDownCounter.add(-1.0);
  }

  @Test
  public void boundDoesNotThrow() {
    DoubleUpDownCounter doubleUpDownCounter =
        meter.doubleUpDownCounterBuilder(NAME).setDescription(DESCRIPTION).setUnit(UNIT).build();
    BoundDoubleUpDownCounter bound = doubleUpDownCounter.bind();
    bound.add(1.0);
    bound.add(-1.0);
    bound.unbind();
  }
}
