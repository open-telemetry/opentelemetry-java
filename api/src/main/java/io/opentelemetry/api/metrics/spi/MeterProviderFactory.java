/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics.spi;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import javax.annotation.concurrent.ThreadSafe;

/**
 * MeterProviderFactory is a service provider for {@link MeterProvider}. Fully qualified class name
 * of the implementation should be registered in {@code
 * META-INF/services/io.opentelemetry.metrics.spi.MeterProviderFactory}. <br>
 * <br>
 * A specific implementation can be selected by a system property {@code
 * io.opentelemetry.metrics.spi.MeterProviderFactory} with value of fully qualified class name.
 *
 * @see OpenTelemetry
 */
@ThreadSafe
public interface MeterProviderFactory {

  /**
   * Creates a new meter registry instance.
   *
   * @return a meter factory instance.
   */
  MeterProvider create();
}
