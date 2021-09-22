/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.http;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class HttpStatusException extends RuntimeException {

  private static final long serialVersionUID = -7885345017862610987L;

  public HttpStatusException(String message) {
    super(message);
  }
}
