/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.metrics;

import openconsensus.common.ExperimentalApi;

/**
 * Class that holds the implementation instance for {@link MetricRegistry}.
 *
 * @since 0.1.0
 */
@ExperimentalApi
public abstract class MetricsComponent {
  /**
   * Returns the {@link MetricRegistry} with the provided implementation.
   *
   * @return the {@link MetricRegistry} implementation.
   * @since 0.1.0
   */
  public abstract MetricRegistry getMetricRegistry();
}
