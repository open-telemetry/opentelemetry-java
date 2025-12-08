/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.slf4j;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Loopback;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;
import javax.annotation.Nullable;

public final class Slf4jBridgeProcessor implements LogRecordProcessor {
  private Slf4jBridgeProcessor() {}

  public static Slf4jBridgeProcessor create() {
    return new Slf4jBridgeProcessor();
  }

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    if (Loopback.isLoopback(context)) {
      return;
    }

    try (Scope scope = Loopback.withLoopback(context).makeCurrent()) {
      recordToSlf4j(
          logRecord.getInstrumentationScopeInfo().getName(),
          logRecord.getEventName(),
          logRecord.getBodyValue(),
          logRecord.getAttributes(),
          logRecord.getSeverity());
    }
  }

  @SuppressWarnings("CheckReturnValue")
  private static void recordToSlf4j(
      String scopeName,
      @Nullable String eventName,
      @Nullable Value<?> bodyValue,
      Attributes attributes,
      Severity severity) {
    Logger logger = LoggerFactory.getLogger(scopeName);
    Level level = toSlf4jLevel(severity);
    if (!logger.isEnabledForLevel(level)) {
      return;
    }
    LoggingEventBuilder builder = logger.atLevel(level);
    if (bodyValue != null) {
      builder.setMessage(bodyValue.asString());
    }
    attributes.forEach((key, value) -> builder.addKeyValue(key.getKey(), value));

    // append event_name last to take priority over attributes
    if (eventName != null) {
      builder.addKeyValue("event_name", eventName);
    }
    builder.log();
  }

  private static Level toSlf4jLevel(Severity severity) {
    switch (severity) {
      case TRACE:
      case TRACE2:
      case TRACE3:
      case TRACE4:
        return Level.TRACE;
      case DEBUG:
      case DEBUG2:
      case DEBUG3:
      case DEBUG4:
        return Level.DEBUG;
      case INFO:
      case INFO2:
      case INFO3:
      case INFO4:
        return Level.INFO;
      case WARN:
      case WARN2:
      case WARN3:
      case WARN4:
        return Level.WARN;
      case ERROR:
      case ERROR2:
      case ERROR3:
      case ERROR4:
      case FATAL:
      case FATAL2:
      case FATAL3:
      case FATAL4:
        return Level.ERROR;
      case UNDEFINED_SEVERITY_NUMBER:
        return Level.INFO;
    }
    throw new IllegalArgumentException("Unknown severity: " + severity);
  }
}
