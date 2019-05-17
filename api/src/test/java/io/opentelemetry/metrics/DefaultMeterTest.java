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

import io.opentelemetry.tags.Tag;
import io.opentelemetry.tags.TagKey;
import io.opentelemetry.tags.TagMap;
import io.opentelemetry.tags.TagValue;
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
  private static final Tag TAG =
      Tag.create(
          TagKey.create("key"), TagValue.create("value"), Tag.METADATA_UNLIMITED_PROPAGATION);

  private static final Meter meter = DefaultMeter.getInstance();

  private static final Measure MEASURE =
      meter
          .measureBuilder("my measure")
          .setDescription("description")
          .setType(Measure.Type.DOUBLE)
          .setUnit("1")
          .build();

  private final TagMap tagMap =
      new TagMap() {

        @Override
        public Iterator<Tag> getIterator() {
          return Collections.singleton(TAG).iterator();
        }

        @Nullable
        @Override
        public TagValue getTagValue(TagKey tagKey) {
          return TagValue.create("value");
        }
      };

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void noopAddLongGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    meter.gaugeLongBuilder(null);
  }

  @Test
  public void noopAddDoubleGauge_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    meter.gaugeDoubleBuilder(null);
  }

  @Test
  public void noopAddDoubleCumulative_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    meter.counterDoubleBuilder(null);
  }

  @Test
  public void noopAddLongCumulative_NullName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("name");
    meter.counterLongBuilder(null);
  }

  // The NoopStatsRecorder should do nothing, so this test just checks that record doesn't throw an
  // exception.
  @Test
  public void noopStatsRecorder_Record() {
    List<Measurement> measurements = Collections.singletonList(MEASURE.createDoubleMeasurement(5));
    meter.record(measurements, tagMap);
  }

  // The NoopStatsRecorder should do nothing, so this test just checks that record doesn't throw an
  // exception.
  @Test
  public void noopStatsRecorder_RecordWithCurrentContext() {
    List<Measurement> measurements = Collections.singletonList(MEASURE.createDoubleMeasurement(6));
    meter.record(measurements);
  }

  @Test
  public void noopStatsRecorder_Record_DisallowNulltagMap() {
    List<Measurement> measurements = Collections.singletonList(MEASURE.createDoubleMeasurement(6));
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("tags");
    meter.record(measurements, null);
  }
}
