/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

/** An exception that is thrown if the user-provided configuration is invalid. */
final class ConfigurationException extends RuntimeException {

  private static final long serialVersionUID = 4717640118051490483L;

  ConfigurationException(String message) {
    super(message);
  }
}
