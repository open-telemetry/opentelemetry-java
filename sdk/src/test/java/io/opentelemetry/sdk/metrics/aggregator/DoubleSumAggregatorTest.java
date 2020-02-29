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

/** Unit tests for {@link DoubleSumAggregator}. */
@RunWith(JUnit4.class)
public class DoubleSumAggregatorTest {
  @Test
  public void factoryAggregation() {
    AggregatorFactory factory = DoubleSumAggregator.getFactory();
    assertThat(factory.getAggregator()).isInstanceOf(DoubleSumAggregator.class);
  }

  @Test
  public void toPoint() {
    Aggregator aggregator = DoubleSumAggregator.getFactory().getAggregator();
    assertThat(getPoint(aggregator).getValue()).isWithin(1e-6).of(0);
  }

  @Test
  public void multipleRecords() {
    Aggregator aggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    assertThat(getPoint(aggregator).getValue()).isWithin(1e-6).of(12.1 * 5);
  }

  @Test
  public void multipleRecords_WithNegatives() {
    Aggregator aggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(-23.2);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(12.3);
    aggregator.recordDouble(-11.3);
    assertThat(getPoint(aggregator).getValue()).isWithin(1e-6).of(14.1);
  }

  @Test
  public void mergeAndReset() {
    Aggregator aggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.recordDouble(13.1);
    aggregator.recordDouble(12.1);
    assertThat(getPoint(aggregator).getValue()).isWithin(1e-6).of(25.2);
    Aggregator mergedAggregator = DoubleSumAggregator.getFactory().getAggregator();
    aggregator.mergeToAndReset(mergedAggregator);
    assertThat(getPoint(aggregator).getValue()).isWithin(1e-6).of(0);
    assertThat(getPoint(mergedAggregator).getValue()).isWithin(1e-6).of(25.2);
    aggregator.recordDouble(12.1);
    aggregator.recordDouble(-25.2);
    aggregator.mergeToAndReset(mergedAggregator);
    assertThat(getPoint(aggregator).getValue()).isWithin(1e-6).of(0);
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
}
