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

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void preventNull_MeasureLong() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("measure");
    meter.newBatchRecorder().put((LongMeasure) null, 5L).record();
  }

  @Test
  public void preventNull_MeasureDouble() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("measure");
    meter.newBatchRecorder().put((DoubleMeasure) null, 5L).record();
  }

  @Test
  public void doesNotThrow() {
    BatchRecorder batchRecorder = meter.newBatchRecorder();
    batchRecorder.put(meter.longMeasureBuilder("longMeasure").build(), 44L);
    batchRecorder.put(meter.longMeasureBuilder("negativeLongMeasure").build(), -44L);
    batchRecorder.put(meter.doubleMeasureBuilder("doubleMeasure").build(), 77.556d);
    batchRecorder.put(meter.doubleMeasureBuilder("negativeDoubleMeasure").build(), -8787.774744d);
    batchRecorder.put(meter.longCounterBuilder("longCounter").build(), 44L);
    batchRecorder.put(meter.longCounterBuilder("negativeLongCounter").build(), -44L);
    batchRecorder.put(meter.doubleCounterBuilder("doubleCounter").build(), 77.556d);
    batchRecorder.put(meter.doubleCounterBuilder("negativeDoubleCounter").build(), -8787.774744d);
  }
}
