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

/** Tests for {@link MeasureBatchRecorder}. */
@RunWith(JUnit4.class)
public class MeasureBatchRecorderTest {
  private static final Meter meter = DefaultMeter.getInstance();

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void preventNull_MeasureLong() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("measure");
    meter.recordMeasureBatch().put((MeasureLong) null, 5L).record();
  }

  @Test
  public void preventNull_MeasureDouble() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("measure");
    meter.recordMeasureBatch().put((MeasureDouble) null, 5L).record();
  }

  @Test
  public void preventNull_DistributedContext() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("distContext");
    meter.recordMeasureBatch().setDistributedContext(null).record();
  }
}
