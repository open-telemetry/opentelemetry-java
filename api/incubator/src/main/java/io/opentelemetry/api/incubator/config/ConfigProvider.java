/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

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
    return getInstrumentationConfig().get("java").get(name);
  }

  /**
   * Returns the {@link DeclarativeConfigProperties} for general instrumentation configuration.
   *
   * <pre>{@code
   * instrumentation/development:
   *   general:
   * }</pre>
   *
   * <p>If the general configuration is not available, an empty {@link DeclarativeConfigProperties}
   * is returned.
   *
   * @return the {@link DeclarativeConfigProperties} for the general instrumentation configuration
   */
  default DeclarativeConfigProperties getGeneralInstrumentationConfig() {
    return getInstrumentationConfig().get("general");
  }

  /**
   * Registers a {@link ConfigChangeListener} for changes to a specific declarative configuration
   * path.
   *
   * <p>Example paths include {@code .instrumentation/development.general.http} and {@code
   * .instrumentation/development.java.methods}.
   *
   * <p>When a watched path changes, {@link ConfigChangeListener#onChange(String,
   * DeclarativeConfigProperties)} is invoked with the changed path and updated configuration for
   * that path.
   *
   * <p>The default implementation performs no registration and returns a no-op handle.
   *
   * @param path the declarative configuration path to watch
   * @param listener the listener to notify when the watched path changes
   * @return a {@link ConfigChangeRegistration} that can be closed to unregister the listener
   */
  default ConfigChangeRegistration addConfigChangeListener(
      String path, ConfigChangeListener listener) {
    return () -> {};
  }

  /** Returns a no-op {@link ConfigProvider}. */
  static ConfigProvider noop() {
    return DeclarativeConfigProperties::empty;
  }
}
