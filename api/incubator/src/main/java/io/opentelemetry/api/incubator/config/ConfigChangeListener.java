/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import javax.annotation.Nullable;

/** Listener notified when declarative configuration changes. */
@FunctionalInterface
public interface ConfigChangeListener {

  /**
   * Called when the watched path changes.
   *
   * <p>{@code path} is the changed declarative configuration path, for example {@code
   * .instrumentation/development.general.http} or {@code
   * .instrumentation/development.java.methods}.
   *
   * <p>If the watched node is unset, cleared, or no longer resolves to a mapping node, {@code
   * newConfig} is null.
   *
   * @param path the declarative configuration path that changed
   * @param newConfig the updated configuration for the changed path
   */
  void onChange(String path, @Nullable DeclarativeConfigProperties newConfig);
}
