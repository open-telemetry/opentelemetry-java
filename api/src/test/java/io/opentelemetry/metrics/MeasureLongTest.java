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

import io.opentelemetry.distributedcontext.DefaultDistributedContextManager;
import io.opentelemetry.distributedcontext.DistributedContextManager;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MeasureLong}. */
@RunWith(JUnit4.class)
public final class MeasureLongTest {
  private static final Meter meter = DefaultMeter.getInstance();
  private static final DistributedContextManager distContextManager =
      DefaultDistributedContextManager.getInstance();

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void preventTooLongMeasureName() {
    char[] chars = new char[256];
    Arrays.fill(chars, 'a');
    String longName = String.valueOf(chars);
    thrown.expect(IllegalArgumentException.class);
    meter.measureDoubleBuilder(longName).build();
  }

  @Test
  public void preventNonPrintableMeasureName() {
    thrown.expect(IllegalArgumentException.class);
    meter.measureDoubleBuilder("\2").build();
  }

  @Test
  public void preventNull_Description() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("description");
    meter.measureLongBuilder("metric").setDescription(null).build();
  }

  @Test
  public void preventNull_Unit() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("unit");
    meter.measureLongBuilder("metric").setUnit(null).build();
  }

  @Test
  public void preventNull_ConstantLabels() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("constantLabels");
    meter.measureLongBuilder("metric").setConstantLabels(null).build();
  }

  @Test
  public void preventNegativeValue() {
    MeasureLong myMeasure = meter.measureLongBuilder("MyMeasure").build();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Unsupported negative values");
    myMeasure.record(-5);
  }

  @Test
  public void preventNegativeValue_RecordWithContext() {
    MeasureLong myMeasure = meter.measureLongBuilder("MyMeasure").build();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Unsupported negative values");
    myMeasure.record(-5, distContextManager.getCurrentContext());
  }

  @Test
  public void preventNullDistContext_RecordWithContext() {
    MeasureLong myMeasure = meter.measureLongBuilder("MyMeasure").build();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("distContext");
    myMeasure.record(5, null);
  }
}
