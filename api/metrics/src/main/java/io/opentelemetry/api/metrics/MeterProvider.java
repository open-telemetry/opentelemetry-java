/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.metrics.internal.NoopMeterProvider;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating named {@link Meter}s.
 *
 * <p>A MeterProvider represents a configured (or noop) Metric collection system that can be used to
 * instrument code.
 *
 * <p>The name <i>Provider</i> is for consistency with other languages and it is <b>NOT</b> loaded
 * using reflection.
 *
 * @see io.opentelemetry.api.metrics.Meter
 */
@ThreadSafe
public interface MeterProvider {
  /**
   * Gets or creates a named and versioned meter instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @param instrumentationVersion The version of the instrumentation library.
   * @param schemaUrl Specifies the Schema URL that should be recorded in the emitted metrics.
   * @return a tracer instance.
   */
  Meter get(String instrumentationName, String instrumentationVersion, String schemaUrl);

  /**
   * Creates a MeterBuilder for a named meter instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a MeterBuilder instance.
   * @since 1.4.0
   */
  default MeterBuilder meterBuilder(String instrumentationName) {
    return new DefaultMeterBuilder(this, instrumentationName);
  }

  public static MeterProvider noop() {
    return NoopMeterProvider.getInstance();
  }

  /** Default implementation of meter builder that delegates to `get` on MeterProvider. */
  static class DefaultMeterBuilder implements MeterBuilder {
    private final MeterProvider provider;
    private final String name;
    private String version = null;
    private String schemaUrl = null;

    DefaultMeterBuilder(MeterProvider provider, String name) {
      this.provider = provider;
      this.name = name;
    }

    @Override
    public MeterBuilder setSchemaUrl(String schemaUrl) {
      this.schemaUrl = schemaUrl;
      return this;
    }

    @Override
    public MeterBuilder setInstrumentationVersion(String instrumentationVersion) {
      this.version = instrumentationVersion;
      return this;
    }

    @Override
    public Meter build() {
      return provider.get(name, version, schemaUrl);
    }
  }
}
