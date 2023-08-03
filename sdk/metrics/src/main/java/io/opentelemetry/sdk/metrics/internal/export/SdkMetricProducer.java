/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.export;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@code SdkMetricProducer} is the interface that is used to make metric data available to the
 * {@link MetricReader}s. Implementations should be stateful, in that each call to {@link
 * #collectAllMetrics()} will return any metric generated since the last call was made.
 *
 * <p>Implementations must be thread-safe.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@ThreadSafe
public interface SdkMetricProducer extends CollectionRegistration {

  /** Cast the registration to a {@link SdkMetricProducer}. */
  static SdkMetricProducer asSdkMetricProducer(CollectionRegistration registration) {
    if (!(registration instanceof SdkMetricProducer)) {
      throw new IllegalArgumentException(
          "unrecognized CollectionRegistration, custom MetricReader implementations are not currently supported");
    }
    return (SdkMetricProducer) registration;
  }

  /** Return a noop {@link SdkMetricProducer}. */
  static SdkMetricProducer noop() {
    return NoopSdkMetricProducer.getInstance();
  }

  /**
   * Returns a collection of produced {@link MetricData}s to be exported. This will only be those
   * metrics that have been produced since the last time this method was called.
   *
   * @return a collection of produced {@link MetricData}s to be exported.
   */
  Collection<MetricData> collectAllMetrics();

  /**
   * Returns the SDK's {@link Resource}.
   *
   * @return the SDK's {@link Resource}.
   */
  Resource getResource();

  /** A {@link SdkMetricProducer} that does nothing. */
  public class NoopSdkMetricProducer implements SdkMetricProducer {
    private static final NoopSdkMetricProducer INSTANCE = new NoopSdkMetricProducer();

    static NoopSdkMetricProducer getInstance() {
      return INSTANCE;
    }

    @Override
    public Resource getResource() {
      return Resource.empty();
    }

    @Override
    public Collection<MetricData> collectAllMetrics() {
      return Collections.emptyList();
    }
  }
}
