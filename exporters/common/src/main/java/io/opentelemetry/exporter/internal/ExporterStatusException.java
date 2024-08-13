/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import javax.annotation.Nullable;

/**
 * An exception that records a status code from a HTTP or GRPC exporter, along with a root cause
 * throwable.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ExporterStatusException extends Exception {

  private static final long serialVersionUID = -4885282244234214070L;

  private final int statusCode;

  public ExporterStatusException(int statusCode, @Nullable Throwable cause) {
    super(cause);
    this.statusCode = statusCode;
  }

  /** Returns the GRPC or HTTP status code. */
  public int getStatusCode() {
    return statusCode;
  }
}
