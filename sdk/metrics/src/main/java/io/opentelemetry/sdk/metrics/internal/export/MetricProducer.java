/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.export;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@code MetricProducer} is the interface that is used to make metric data available to the {@link
 * MetricReader}s. Implementations should be stateful, in that each call to {@link
 * #collectAllMetrics()} will return any metric generated since the last call was made.
 *
 * <p>Implementations must be thread-safe.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@ThreadSafe
public interface MetricProducer extends CollectionRegistration {

  /** Cast the registration to a {@link MetricProducer}. */
  static MetricProducer asMetricProducer(CollectionRegistration registration) {
    if (!(registration instanceof MetricProducer)) {
      throw new IllegalArgumentException(
          "unrecognized CollectionRegistration, custom MetricReader implementations are not currently supported");
    }
    return (MetricProducer) registration;
  }

  /** Return a noop {@link MetricProducer}. */
  static MetricProducer noop() {
    return Collections::emptyList;
  }

  /**
   * Returns a collection of produced {@link MetricData}s to be exported. This will only be those
   * metrics that have been produced since the last time this method was called.
   *
   * @return a collection of produced {@link MetricData}s to be exported.
   */
  Collection<MetricData> collectAllMetrics();
}
