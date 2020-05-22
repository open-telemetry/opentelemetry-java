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
import io.opentelemetry.metrics.LongValueRecorder.BoundLongValueRecorder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link LongValueRecorder}. */
@RunWith(JUnit4.class)
public final class LongValueRecorderTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Map<String, String> CONSTANT_LABELS =
      Collections.singletonMap("key", "value");

  private final Meter meter = OpenTelemetry.getMeter("LongValueRecorderTest");

  @Test
  public void preventNull_Name() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    meter.longValueRecorderBuilder(null);
  }

  @Test
  public void preventEmpty_Name() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.longValueRecorderBuilder("").build();
  }

  @Test
  public void preventNonPrintableMeasureName() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.longValueRecorderBuilder("\2").build();
  }

  @Test
  public void preventTooLongName() {
    char[] chars = new char[256];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.longValueRecorderBuilder(longName).build();
  }

  @Test
  public void preventNull_Description() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    meter.longValueRecorderBuilder("metric").setDescription(null).build();
  }

  @Test
  public void preventNull_Unit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    meter.longValueRecorderBuilder("metric").setUnit(null).build();
  }

  @Test
  public void preventNull_ConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    meter.longValueRecorderBuilder("metric").setConstantLabels(null).build();
  }

  @Test
  public void recordDoesNotThrow() {
    LongValueRecorder longValueRecorder =
        meter
            .longValueRecorderBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setConstantLabels(CONSTANT_LABELS)
            .build();
    longValueRecorder.record(5);
    longValueRecorder.record(-5);
  }

  @Test
  public void boundDoesNotThrow() {
    LongValueRecorder longValueRecorder =
        meter
            .longValueRecorderBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setConstantLabels(CONSTANT_LABELS)
            .build();
    BoundLongValueRecorder bound = longValueRecorder.bind();
    bound.record(5);
    bound.record(-5);
    bound.unbind();
  }
}
