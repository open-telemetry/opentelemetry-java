/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import static io.opentelemetry.api.incubator.config.DeclarativeConfigProperties.empty;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for accessing declarative configuration.
 *
 * <p>The name <i>Provider</i> is for consistency with other languages and it is <b>NOT</b> loaded
 * using reflection.
 *
 * <p>See {@link InstrumentationConfigUtil} for convenience methods for extracting config from
 * {@link ConfigProvider}.
 */
@ThreadSafe
public interface ConfigProvider {

  /**
   * Returns the {@link DeclarativeConfigProperties} corresponding to <a
   * href="https://github.com/open-telemetry/opentelemetry-configuration/blob/main/schema/instrumentation.json">instrumentation
   * config</a>, or {@link DeclarativeConfigProperties#empty()} if unavailable.
   *
   * @return the instrumentation {@link DeclarativeConfigProperties}
   */
  DeclarativeConfigProperties getInstrumentationConfig();

  /**
   * Returns the {@link DeclarativeConfigProperties} for a specific instrumentation by name. If no
   * configuration is available for the given name, an empty {@link DeclarativeConfigProperties} is
   * returned.
   *
   * @param name the name of the instrumentation
   * @return the {@link DeclarativeConfigProperties} for the given instrumentation name
   */
  default DeclarativeConfigProperties getInstrumentationConfig(String name) {
    DeclarativeConfigProperties config = getInstrumentationConfig();
    return config == null ? empty() : config.get("java").get(name);
  }

  /**
   * Returns the {@link DeclarativeConfigProperties} for general instrumentation configuration. If
   * the general configuration is not available, an empty {@link DeclarativeConfigProperties} is
   * returned.
   *
   * @return the {@link DeclarativeConfigProperties} for the general instrumentation configuration
   */
  default DeclarativeConfigProperties getGeneralInstrumentationConfig() {
    DeclarativeConfigProperties config = getInstrumentationConfig();
    return config == null ? empty() : config.get("general");
  }

  /** Returns a no-op {@link ConfigProvider}. */
  static ConfigProvider noop() {
    return DeclarativeConfigProperties::empty;
  }
}
