/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.config;

/** An exception that is thrown if the user-provided configuration is invalid. */
public final class ConfigurationException extends RuntimeException {

  private static final long serialVersionUID = -6548754499741039091L;

  /** Create a new configuration exception with specified {@code message} and without a cause. */
  public ConfigurationException(String message) {
    super(message);
  }

  /** Create a new configuration exception with specified {@code message} and {@code cause}. */
  public ConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
