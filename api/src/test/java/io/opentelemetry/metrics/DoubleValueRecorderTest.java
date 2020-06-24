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
import io.opentelemetry.metrics.DoubleValueRecorder.BoundDoubleValueRecorder;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link DoubleValueRecorder}. */
@RunWith(JUnit4.class)
public final class DoubleValueRecorderTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String UNIT = "1";
  private static final Labels CONSTANT_LABELS = Labels.of("key", "value");

  private final Meter meter = OpenTelemetry.getMeter("DoubleValueRecorderTest");

  @Test
  public void preventNull_Name() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    meter.doubleValueRecorderBuilder(null);
  }

  @Test
  public void preventEmpty_Name() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.doubleValueRecorderBuilder("").build();
  }

  @Test
  public void preventNonPrintableName() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.doubleValueRecorderBuilder("\2").build();
  }

  @Test
  public void preventTooLongName() {
    char[] chars = new char[256];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(DefaultMeter.ERROR_MESSAGE_INVALID_NAME);
    meter.doubleValueRecorderBuilder(longName).build();
  }

  @Test
  public void preventNull_Description() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    meter.doubleValueRecorderBuilder("metric").setDescription(null).build();
  }

  @Test
  public void preventNull_Unit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    meter.doubleValueRecorderBuilder("metric").setUnit(null).build();
  }

  @Test
  public void preventNull_ConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    meter.doubleValueRecorderBuilder("metric").setConstantLabels(null).build();
  }

  @Test
  public void record_PreventNullLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labels");
    meter.doubleValueRecorderBuilder("metric").build().record(1.0, null);
  }

  @Test
  public void record_DoesNotThrow() {
    DoubleValueRecorder doubleValueRecorder =
        meter
            .doubleValueRecorderBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setConstantLabels(CONSTANT_LABELS)
            .build();
    doubleValueRecorder.record(5.0, Labels.empty());
    doubleValueRecorder.record(-5.0, Labels.empty());
  }

  @Test
  public void bound_PreventNullLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("labels");
    meter.doubleValueRecorderBuilder("metric").build().bind(null);
  }

  @Test
  public void bound_DoesNotThrow() {
    DoubleValueRecorder doubleValueRecorder =
        meter
            .doubleValueRecorderBuilder(NAME)
            .setDescription(DESCRIPTION)
            .setUnit(UNIT)
            .setConstantLabels(CONSTANT_LABELS)
            .build();
    BoundDoubleValueRecorder bound = doubleValueRecorder.bind(Labels.empty());
    bound.record(5.0);
    bound.record(-5.0);
    bound.unbind();
  }
}
