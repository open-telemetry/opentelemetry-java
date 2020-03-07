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

import io.opentelemetry.metrics.spi.MetricsProvider;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class DefaultMetricsProvider implements MetricsProvider {
  private static final MetricsProvider instance = new DefaultMetricsProvider();

  /**
   * Returns a {@code MetricsProvider} singleton that is the default implementation for {@link
   * MetricsProvider}.
   *
   * @return a {@code MetricsProvider} singleton that is the default implementation for {@link
   *     MetricsProvider}.
   */
  public static MetricsProvider getInstance() {
    return instance;
  }

  @Override
  public MeterProvider create() {
    return DefaultMeterProvider.getInstance();
  }

  private DefaultMetricsProvider() {}
}
