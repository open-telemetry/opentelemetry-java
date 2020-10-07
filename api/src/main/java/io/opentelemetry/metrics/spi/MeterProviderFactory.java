/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.metrics.spi;

import io.opentelemetry.metrics.MeterProvider;
import javax.annotation.concurrent.ThreadSafe;

/**
 * MeterProviderFactory is a service provider for {@link MeterProvider}. Fully qualified class name
 * of the implementation should be registered in {@code
 * META-INF/services/io.opentelemetry.metrics.spi.MeterProviderFactory}. <br>
 * <br>
 * A specific implementation can be selected by a system property {@code
 * io.opentelemetry.metrics.spi.MeterProviderFactory} with value of fully qualified class name.
 *
 * @see io.opentelemetry.OpenTelemetry
 */
@ThreadSafe
public interface MeterProviderFactory {

  /**
   * Creates a new meter registry instance.
   *
   * @return a meter factory instance.
   * @since 0.1.0
   */
  MeterProvider create();
}
