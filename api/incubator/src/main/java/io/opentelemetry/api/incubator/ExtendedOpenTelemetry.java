/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;

/** Extension to {@link OpenTelemetry} with experimental APIs. */
public interface ExtendedOpenTelemetry extends OpenTelemetry {
  /** Returns the {@link ConfigProvider} for this {@link OpenTelemetry}. */
  default ConfigProvider getConfigProvider() {
    return ConfigProvider.noop();
  }

  /**
   * Returns the {@link DeclarativeConfigProperties} for a specific instrumentation by name. If no
   * configuration is available for the given name, an empty {@link DeclarativeConfigProperties} is
   * returned.
   *
   * <p>For example, {@code getInstrumentationConfig("foo")} returns the node:
   *
   * <pre>{@code
   * instrumentation/development:
   *   java:
   *     foo:
   * }</pre>
   *
   * @param name the name of the instrumentation
   * @return the {@link DeclarativeConfigProperties} for the given instrumentation name
   */
  default DeclarativeConfigProperties getInstrumentationConfig(String name) {
    return getConfigProvider().getInstrumentationConfig(name);
  }

  /**
   * Returns the {@link DeclarativeConfigProperties} for general instrumentation configuration:
   *
   * <pre>{@code
   * instrumentation/development:
   *   general:
   * }</pre>
   *
   * If the general configuration is not available, an empty {@link DeclarativeConfigProperties} is
   * returned.
   *
   * @return the {@link DeclarativeConfigProperties} for the general instrumentation configuration
   */
  default DeclarativeConfigProperties getGeneralInstrumentationConfig() {
    return getConfigProvider().getGeneralInstrumentationConfig();
  }
}
