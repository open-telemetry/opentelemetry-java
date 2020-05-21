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
  public void testNewBatchRecorder_badLabelSet() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("key/value");
    meter.newBatchRecorder("key");
  }

  @Test
  public void preventNull_MeasureLong() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("valueRecorder");
    meter.newBatchRecorder().put((LongValueRecorder) null, 5L).record();
  }

  @Test
  public void preventNull_MeasureDouble() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("valueRecorder");
    meter.newBatchRecorder().put((DoubleValueRecorder) null, 5L).record();
  }

  @Test
  public void preventNull_LongCounter() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("counter");
    meter.newBatchRecorder().put((LongCounter) null, 5L).record();
  }

  @Test
  public void preventNull_DoubleCounter() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("counter");
    meter.newBatchRecorder().put((DoubleCounter) null, 5L).record();
  }

  @Test
  public void preventNull_LongUpDownCounter() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("upDownCounter");
    meter.newBatchRecorder().put((LongUpDownCounter) null, 5L).record();
  }

  @Test
  public void preventNull_DoubleUpDownCounter() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("upDownCounter");
    meter.newBatchRecorder().put((DoubleUpDownCounter) null, 5L).record();
  }

  @Test
  public void doesNotThrow() {
    BatchRecorder batchRecorder = meter.newBatchRecorder();
    batchRecorder.put(meter.longValueRecorderBuilder("longValueRecorder").build(), 44L);
    batchRecorder.put(meter.longValueRecorderBuilder("negativeLongValueRecorder").build(), -44L);
    batchRecorder.put(meter.doubleValueRecorderBuilder("doubleValueRecorder").build(), 77.556d);
    batchRecorder.put(
        meter.doubleValueRecorderBuilder("negativeDoubleValueRecorder").build(), -77.556d);
    batchRecorder.put(meter.longCounterBuilder("longCounter").build(), 44L);
    batchRecorder.put(meter.doubleCounterBuilder("doubleCounter").build(), 77.556d);
    batchRecorder.put(meter.longUpDownCounterBuilder("longUpDownCounter").build(), -44L);
    batchRecorder.put(meter.doubleUpDownCounterBuilder("doubleUpDownCounter").build(), -77.556d);
    batchRecorder.record();
  }

  @Test
  public void negativeValue_DoubleCounter() {
    BatchRecorder batchRecorder = meter.newBatchRecorder();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Counters can only increase");
    batchRecorder.put(meter.doubleCounterBuilder("doubleCounter").build(), -77.556d);
  }

  @Test
  public void negativeValue_LongCounter() {
    BatchRecorder batchRecorder = meter.newBatchRecorder();
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Counters can only increase");
    batchRecorder.put(meter.longCounterBuilder("longCounter").build(), -44L);
  }
}
