/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.spi.trace.TracerProviderFactory;

/** Builder class for {@link DefaultOpenTelemetry}. */
public final class DefaultOpenTelemetryBuilder {
  private ContextPropagators propagators = ContextPropagators.noop();
  private TracerProvider tracerProvider;

  /**
   * Package protected to disallow direct initialization.
   *
   * @see DefaultOpenTelemetry#builder()
   */
  DefaultOpenTelemetryBuilder() {}

  /**
   * Sets the {@link TracerProvider} to use.
   *
   * @param tracerProvider the {@link TracerProvider} to use.
   * @return this.
   */
  public DefaultOpenTelemetryBuilder setTracerProvider(TracerProvider tracerProvider) {
    requireNonNull(tracerProvider, "tracerProvider");
    this.tracerProvider = tracerProvider;
    return this;
  }

  /**
   * Sets the {@link ContextPropagators} to use.
   *
   * @param propagators the {@link ContextPropagators} to use.
   * @return this.
   */
  public DefaultOpenTelemetryBuilder setPropagators(ContextPropagators propagators) {
    requireNonNull(propagators, "propagators");
    this.propagators = propagators;
    return this;
  }

  /**
   * Returns a new {@link OpenTelemetry} based on the configuration passed in this builder.
   *
   * @return a new {@link OpenTelemetry}.
   */
  public OpenTelemetry build() {
    TracerProvider tracerProvider = this.tracerProvider;
    if (tracerProvider == null) {
      TracerProviderFactory tracerProviderFactory = Utils.loadSpi(TracerProviderFactory.class);
      if (tracerProviderFactory != null) {
        tracerProvider = tracerProviderFactory.create();
      } else {
        tracerProvider = TracerProvider.getDefault();
      }
    }

    return new DefaultOpenTelemetry(tracerProvider, propagators);
  }
}
