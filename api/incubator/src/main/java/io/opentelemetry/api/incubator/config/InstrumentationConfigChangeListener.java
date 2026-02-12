/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import javax.annotation.Nullable;

/** Listener notified when instrumentation configuration changes. */
@FunctionalInterface
public interface InstrumentationConfigChangeListener {

  /**
   * Called when the effective config for one top-level instrumentation node changes (for example
   * {@code methods}, {@code kafka}, or {@code grpc}).
   *
   * <p>Both config arguments are scoped to {@code instrumentationName}.
   *
   * <p>{@code newConfig} is never null. If the node is unset or cleared, {@code newConfig} is
   * {@link DeclarativeConfigProperties#empty()}.
   *
   * @param instrumentationName the top-level instrumentation name that changed
   * @param previousConfig the previous effective configuration, or {@code null} if unavailable
   * @param newConfig the updated effective configuration for {@code instrumentationName}
   */
  void onChange(
      String instrumentationName,
      @Nullable DeclarativeConfigProperties previousConfig,
      DeclarativeConfigProperties newConfig);
}
