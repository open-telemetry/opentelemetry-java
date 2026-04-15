/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.function.Function;

/**
 * A {@link ScopeConfigurator} computes configuration for a given {@link InstrumentationScopeInfo}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@FunctionalInterface
public interface ScopeConfigurator<T> extends Function<InstrumentationScopeInfo, T> {

  /** Create a new builder. */
  static <T> ScopeConfiguratorBuilder<T> builder() {
    return new ScopeConfiguratorBuilder<>(null);
  }

  /**
   * Convert this {@link ScopeConfigurator} to a builder. Additional added matchers only apply when
   * {@link #apply(Object)} returns {@code null}. If this configurator contains {@link
   * ScopeConfiguratorBuilder#setDefault(Object)}, additional matchers are never applied.
   */
  default ScopeConfiguratorBuilder<T> toBuilder() {
    return new ScopeConfiguratorBuilder<>(this);
  }
}
