/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import io.opentelemetry.api.logs.Logger;

/** Extended {@link Logger} with experimental APIs. */
public interface ExtendedLogger extends Logger {

  /**
   * Returns {@code true} if the logger is enabled.
   *
   * <p>This allows callers to avoid unnecessary compute when nothing is consuming the data. Because
   * the response is subject to change over the application, callers should call this before each
   * call to {@link #logRecordBuilder()}.
   */
  default boolean isEnabled() {
    return true;
  }

  @Override
  ExtendedLogRecordBuilder logRecordBuilder();
}
