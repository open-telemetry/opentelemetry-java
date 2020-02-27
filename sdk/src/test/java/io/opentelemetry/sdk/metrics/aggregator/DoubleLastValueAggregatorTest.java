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

package io.opentelemetry.sdk.metrics.aggregator;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Aggregator}. */
@RunWith(JUnit4.class)
public class DoubleLastValueAggregatorTest {
  @Test
  public void factoryAggregation() {
    AggregatorFactory factory = DoubleLastValueAggregator.getFactory();
    assertThat(factory.getAggregator()).isInstanceOf(DoubleLastValueAggregator.class);
  }

  @Test
  public void toPoint() {
    Aggregator aggregator = DoubleLastValueAggregator.getFactory().getAggregator();
    assertNullPoint(aggregator);
  }

  @Test
  public void multipleRecords() {
    Aggregator aggregator = DoubleLastValueAggregator.getFactory().getAggregator();
    aggregator.recordDouble(12.1);
    assertThat(getPoint(aggregator).getValue()).isWithin(1e-6).of(12.1);
    aggregator.recordDouble(13.1);
    aggregator.recordDouble(14.1);
    assertThat(getPoint(aggregator).getValue()).isWithin(1e-6).of(14.1);
  }

  @Test
  public void mergeAndReset() {
    Aggregator aggregator = DoubleLastValueAggregator.getFactory().getAggregator();
    aggregator.recordDouble(13.1);
    assertThat(getPoint(aggregator).getValue()).isWithin(1e-6).of(13.1);
    Aggregator mergedAggregator = DoubleLastValueAggregator.getFactory().getAggregator();
    aggregator.mergeToAndReset(mergedAggregator);
    assertNullPoint(aggregator);
    assertThat(getPoint(mergedAggregator).getValue()).isWithin(1e-6).of(13.1);
    aggregator.recordDouble(12.1);
    aggregator.mergeToAndReset(mergedAggregator);
    assertNullPoint(aggregator);
    assertThat(getPoint(mergedAggregator).getValue()).isWithin(1e-6).of(12.1);
  }

  private static DoublePoint getPoint(Aggregator aggregator) {
    Point point = aggregator.toPoint(12345, 12358, Collections.singletonMap("key", "value"));
    assertThat(point).isNotNull();
    assertThat(point.getStartEpochNanos()).isEqualTo(12345);
    assertThat(point.getEpochNanos()).isEqualTo(12358);
    assertThat(point.getLabels()).containsExactly("key", "value");
    assertThat(point).isInstanceOf(DoublePoint.class);
    return (DoublePoint) point;
  }

  private static void assertNullPoint(Aggregator aggregator) {
    Point point = aggregator.toPoint(12345, 12358, Collections.singletonMap("key", "value"));
    assertThat(point).isNull();
  }
}
