/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.logs.data.LogRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LogAdapter {

  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(LogAdapter.class.getName()));

  private LogAdapter() {}

  /** Converts the provided {@link LogRecord} to {@link ResourceLogs}. */
  public static List<ResourceLogs> toProtoResourceLogs(Collection<LogRecord> logRecords) {
    logger.log(Level.FINEST, "toProtoResourceLogs");
    List<ResourceLogs> resourceLogs = new ArrayList<>(logRecords.size());
    return resourceLogs;
  }
}
