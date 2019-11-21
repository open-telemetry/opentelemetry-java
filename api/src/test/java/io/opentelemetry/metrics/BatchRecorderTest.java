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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link BatchRecorder}. */
@RunWith(JUnit4.class)
public class BatchRecorderTest {
  private static final Meter meter = DefaultMeter.getInstance();
  private static final MeasureLong measureLong = meter.measureLongBuilder("measure_long").build();
  private static final MeasureDouble measureDouble =
      meter.measureDoubleBuilder("measure_double").build();

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void preventNull_MeasureLong() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("measure");
    meter.newMeasureBatchRecorder().put((MeasureLong) null, 5L).record();
  }

  @Test
  public void preventNegativeValues_MeasureLong() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Unsupported negative values");
    meter.newMeasureBatchRecorder().put(measureLong, -5L).record();
  }

  @Test
  public void preventNull_MeasureDouble() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("measure");
    meter.newMeasureBatchRecorder().put((MeasureDouble) null, 5L).record();
  }

  @Test
  public void preventNegativeValues_MeasureDouble() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Unsupported negative values");
    meter.newMeasureBatchRecorder().put(measureDouble, -5.0).record();
  }

  @Test
  public void doesNotThrow() {
    meter.newMeasureBatchRecorder().put(measureLong, 5).put(measureDouble, 3.5).record();
  }
}
