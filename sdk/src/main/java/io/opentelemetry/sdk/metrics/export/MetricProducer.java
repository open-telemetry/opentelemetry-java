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

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@code MetricProducer} is the interface that all "pull based" metric libraries should implement
 * in order to make data available to the OpenTelemetry exporters.
 *
 * @since 0.3.0
 */
@ThreadSafe
public interface MetricProducer {
  /**
   * Returns a collection of produced {@link MetricData}s to be exported.
   *
   * @return a collection of produced {@link MetricData}s to be exported.
   * @since 0.17
   */
  Collection<MetricData> getAllMetrics();
}
