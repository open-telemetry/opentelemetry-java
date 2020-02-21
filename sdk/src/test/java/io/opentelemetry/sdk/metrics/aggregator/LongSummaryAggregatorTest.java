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

import io.opentelemetry.sdk.metrics.data.MetricData.LongSummaryPoint;
import java.util.Collections;
import org.junit.Test;

public class LongSummaryAggregatorTest {

  @Test
  public void testRecordings() {
    LongSummaryAggregator aggregator = new LongSummaryAggregator();

    assertThat(aggregator.toPoint(0, 100, Collections.<String, String>emptyMap()))
        .isEqualTo(
            LongSummaryPoint.create(
                0, 100, Collections.<String, String>emptyMap(), 0, 0, null, null));

    aggregator.recordLong(100);
    assertThat(aggregator.toPoint(0, 100, Collections.<String, String>emptyMap()))
        .isEqualTo(
            LongSummaryPoint.create(
                0, 100, Collections.<String, String>emptyMap(), 1, 100, 100L, 100L));

    aggregator.recordLong(50);
    assertThat(aggregator.toPoint(0, 100, Collections.<String, String>emptyMap()))
        .isEqualTo(
            LongSummaryPoint.create(
                0, 100, Collections.<String, String>emptyMap(), 2, 150, 50L, 100L));

    aggregator.recordLong(-75);
    assertThat(aggregator.toPoint(0, 100, Collections.<String, String>emptyMap()))
        .isEqualTo(
            LongSummaryPoint.create(
                0, 100, Collections.<String, String>emptyMap(), 3, 75, -75L, 100L));
  }

  @Test
  public void testMergeAndReset() {
    LongSummaryAggregator aggregator = new LongSummaryAggregator();

    aggregator.recordLong(100);
    LongSummaryAggregator mergedToAggregator = new LongSummaryAggregator();
    aggregator.mergeToAndReset(mergedToAggregator);

    assertThat(mergedToAggregator.toPoint(0, 100, Collections.<String, String>emptyMap()))
        .isEqualTo(
            LongSummaryPoint.create(
                0, 100, Collections.<String, String>emptyMap(), 1, 100, 100L, 100L));

    assertThat(aggregator.toPoint(0, 100, Collections.<String, String>emptyMap()))
        .isEqualTo(
            LongSummaryPoint.create(
                0, 100, Collections.<String, String>emptyMap(), 0, 0, null, null));
  }
}
