/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import java.util.Map;

/**
 * An interface for accessing declarative configuration data.
 *
 * @deprecated Use {@link io.opentelemetry.api.config.DeclarativeConfigProperties} instead.
 */
@Deprecated
public interface DeclarativeConfigProperties
    extends io.opentelemetry.api.config.DeclarativeConfigProperties {

  /**
   * Return an empty {@link DeclarativeConfigProperties} instance.
   *
   * @deprecated Use {@link io.opentelemetry.api.config.DeclarativeConfigProperties#empty()}
   *     instead.
   */
  @Deprecated
  static io.opentelemetry.api.config.DeclarativeConfigProperties empty() {
    return io.opentelemetry.api.config.DeclarativeConfigProperties.empty();
  }

  /**
   * Return a map representation of the {@code declarativeConfigProperties}.
   *
   * @deprecated Use {@link
   *     io.opentelemetry.api.config.DeclarativeConfigProperties#toMap(io.opentelemetry.api.config.DeclarativeConfigProperties)}
   *     instead.
   */
  @Deprecated
  static Map<String, Object> toMap(
      io.opentelemetry.api.config.DeclarativeConfigProperties declarativeConfigProperties) {
    return io.opentelemetry.api.config.DeclarativeConfigProperties.toMap(
        declarativeConfigProperties);
  }
}
