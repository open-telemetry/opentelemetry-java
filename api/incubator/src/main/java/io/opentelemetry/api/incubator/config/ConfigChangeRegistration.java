/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

/** Registration handle returned by {@link ConfigProvider#addConfigChangeListener}. */
@FunctionalInterface
public interface ConfigChangeRegistration {

  /**
   * Unregister the listener associated with this registration.
   *
   * <p>Subsequent calls have no effect.
   */
  void close();
}
