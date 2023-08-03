/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.export;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.metrics.data.ScopeMetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@code SDKMetricProducer} is the interface that is used to make metric data available to the {@link
 * MetricReader}s. Implementations should be stateful, in that each call to {@link
 * #collectAllMetrics()} will return any metric generated since the last call was made.
 *
 * <p>Implementations must be thread-safe.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@ThreadSafe
public interface SDKMetricProducer extends MetricProducer {

  /** Cast the registration to a {@link SDKMetricProducer}. */
  static SDKMetricProducer asSDKMetricProducer(CollectionRegistration registration) {
    if (!(registration instanceof SDKMetricProducer)) {
      throw new IllegalArgumentException(
          "unrecognized CollectionRegistration, custom MetricReader implementations are not currently supported");
    }
    return (SDKMetricProducer) registration;
  }

  /** Return a noop {@link SDKMetricProducer}. */
  static SDKMetricProducer noop() {
    return NOOPSDKMetricProducer.getInstance();
  }

  /**
   * Returns the SDK's {@link Resource}.
   *
   * @return the SDK's {@link Resource}.
   */
  Resource getResource();

  /** A {@link MeterProvider} that does nothing. */
  class NOOPSDKMetricProducer implements SDKMetricProducer {
    private static final NOOPSDKMetricProducer INSTANCE = new NOOPSDKMetricProducer();
    static NOOPSDKMetricProducer getInstance() {
      return INSTANCE;
    }

    @Override
    Resource getResource() {
      return Resource.empty();
    }

    @Override
    Collection<ScopeMetricData> collectAllMetrics() {
      return Collections.emptyList();
    }
  }
}
