/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@code MetricProducer} is the interface that is used to make metric data available to the
 * OpenTelemetry exporters. Implementations should be stateful, in that each call to {@link
 * #collectAllMetrics()} will return any metric generated since the last call was made.
 *
 * <p>Implementations must be thread-safe.
 *
 * @since 0.3.0
 */
@ThreadSafe
public interface MetricProducer {
  /**
   * Returns a collection of produced {@link MetricData}s to be exported. This will only be those
   * metrics that have been produced since the last time this method was called.
   *
   * @return a collection of produced {@link MetricData}s to be exported.
   * @since 0.17
   */
  Collection<MetricData> collectAllMetrics();
}
