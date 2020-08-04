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

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.common.Labels;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link NoopAggregator}. */
class NoopAggregatorTest {
  @Test
  void factoryAggregation() {
    AggregatorFactory factory = NoopAggregator.getFactory();
    assertThat(factory.getAggregator()).isInstanceOf(NoopAggregator.class);
  }

  @Test
  void noopOperations() {
    Aggregator aggregator = NoopAggregator.getFactory().getAggregator();
    aggregator.recordLong(12);
    aggregator.recordDouble(12.1);
    aggregator.mergeToAndReset(aggregator);
    assertThat(aggregator.toPoint(1, 2, Labels.empty())).isNull();
  }
}
