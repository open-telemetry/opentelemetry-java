/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.logs.data.LogRecord;

/** A LogSink accepts logging records for transmission to an aggregator or log processing system. */
public interface LogSink {
  /**
   * Pass a record to the SDK for transmission to a logging exporter.
   *
   * @param record record to transmit
   */
  void offer(LogRecord record);
}
