/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.baggage.spi;

import io.opentelemetry.baggage.BaggageManager;
import javax.annotation.concurrent.ThreadSafe;

/**
 * BaggageManagerFactory is a service provider for {@link BaggageManager}. Fully qualified class
 * name of the implementation should be registered in {@code
 * META-INF/services/io.opentelemetry.baggage.spi.BaggageManagerFactory}. <br>
 * <br>
 * A specific implementation can be selected by a system property {@code
 * io.opentelemetry.baggage.spi.BaggageManagerFactory} with value of fully qualified class name.
 *
 * @see io.opentelemetry.OpenTelemetry
 */
@ThreadSafe
public interface BaggageManagerFactory {

  /**
   * Creates a new {@code BaggageManager} instance.
   *
   * @return a {@code BaggageManager} instance.
   * @since 0.9.0
   */
  BaggageManager create();
}
