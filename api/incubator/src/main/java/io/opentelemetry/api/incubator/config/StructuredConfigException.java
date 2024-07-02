/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

/** An exception that is thrown if the user-provided file configuration is invalid. */
public final class StructuredConfigException extends RuntimeException {

  private static final long serialVersionUID = 3036584181551130522L;

  /** Create a new configuration exception with specified {@code message} and without a cause. */
  public StructuredConfigException(String message) {
    super(message);
  }

  /** Create a new configuration exception with specified {@code message} and {@code cause}. */
  public StructuredConfigException(String message, Throwable cause) {
    super(message, cause);
  }
}
