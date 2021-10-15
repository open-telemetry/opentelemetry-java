/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.logs.data.LogBuilder;
import io.opentelemetry.sdk.logs.data.LogData;

/** A LogSink accepts logging records for transmission to an aggregator or log processing system. */
public interface LogSink {

  /**
   * Create a log builder. {@link LogBuilder#build()} can be passed to {@link #offer(LogData)}.
   *
   * @return the builder
   */
  LogBuilder builder();

  /**
   * Pass the {@link LogData} to the sink.
   *
   * @param logData the log
   */
  void offer(LogData logData);
}
