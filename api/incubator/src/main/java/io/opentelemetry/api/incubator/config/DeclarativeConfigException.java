/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

/**
 * An exception that is thrown when errors occur with declarative configuration.
 *
 * @deprecated Use {@link io.opentelemetry.api.config.DeclarativeConfigException} instead.
 */
@Deprecated
public class DeclarativeConfigException
    extends io.opentelemetry.api.config.DeclarativeConfigException {

  private static final long serialVersionUID = 3036584181551130522L;

  /** Create a new configuration exception with specified {@code message} and without a cause. */
  public DeclarativeConfigException(String message) {
    super(message);
  }

  /** Create a new configuration exception with specified {@code message} and {@code cause}. */
  public DeclarativeConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
