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

import io.opentelemetry.distributedcontext.Attribute;
import io.opentelemetry.distributedcontext.AttributeKey;
import io.opentelemetry.distributedcontext.AttributeValue;
import io.opentelemetry.distributedcontext.DistributedContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DefaultMeter}. */
@RunWith(JUnit4.class)
public final class DefaultMeterTest {
  private static final Attribute ATTRIBUTE =
      Attribute.create(
          AttributeKey.create("key"),
          AttributeValue.create("value"),
          Attribute.METADATA_UNLIMITED_PROPAGATION);

  private static final Meter defaultMeter = DefaultMeter.getInstance();

  private static final Measure MEASURE =
      defaultMeter
          .measureBuilder("my measure")
          .setDescription("description")
          .setType(Measure.Type.DOUBLE)
          .setUnit("1")
          .build();

  private final DistributedContext distContext =
      new DistributedContext() {

        @Override
        public Iterator<Attribute> getIterator() {
          return Collections.singleton(ATTRIBUTE).iterator();
        }

        @Nullable
        @Override
        public AttributeValue getAttributeValue(AttributeKey attrKey) {
          return AttributeValue.create("value");
        }
      };

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void noopAddLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    defaultMeter.gaugeLongBuilder(null);
  }

  @Test
  public void noopAddDoubleGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    defaultMeter.gaugeDoubleBuilder(null);
  }

  @Test
  public void noopAddDoubleCumulative_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    defaultMeter.counterDoubleBuilder(null);
  }

  @Test
  public void noopAddLongCumulative_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    defaultMeter.counterLongBuilder(null);
  }

  // The NoopStatsRecorder should do nothing, so this test just checks that record doesn't throw an
  // exception.
  @Test
  public void noopStatsRecorder_Record() {
    List<Measurement> measurements = Collections.singletonList(MEASURE.createDoubleMeasurement(5));
    defaultMeter.record(measurements, distContext);
  }

  // The NoopStatsRecorder should do nothing, so this test just checks that record doesn't throw an
  // exception.
  @Test
  public void noopStatsRecorder_RecordWithCurrentContext() {
    List<Measurement> measurements = Collections.singletonList(MEASURE.createDoubleMeasurement(6));
    defaultMeter.record(measurements);
  }

  @Test
  public void noopStatsRecorder_Record_DisallowNulldistContext() {
    List<Measurement> measurements = Collections.singletonList(MEASURE.createDoubleMeasurement(6));
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("distContext");
    defaultMeter.record(measurements, null);
  }
}
