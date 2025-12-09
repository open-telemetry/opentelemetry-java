/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.incubator.logs.ExtendedLogger;
import io.opentelemetry.api.incubator.logs.LogEventBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
import javax.annotation.Nullable;

/** SDK implementation of {@link ExtendedLogger}. */
final class ExtendedSdkLogger extends SdkLogger implements ExtendedLogger {

  ExtendedSdkLogger(
      LoggerSharedState loggerSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      LoggerConfig loggerConfig) {
    super(loggerSharedState, instrumentationScopeInfo, loggerConfig);
  }

  @Override
  @SuppressWarnings("RedundantOverride")
  public boolean isEnabled(Severity severity, Context context) {
    return super.isEnabled(severity, context);
  }

  @Override
  public LogEventBuilder logBuilder(Severity severity, String eventName) {
    return new SdkLogEventBuilder(loggerSharedState, instrumentationScopeInfo, this);
  }

  @Override
  public void log(
      Severity severity,
      String eventName,
      Attributes attributes,
      @Nullable Value<?> body,
      @Nullable Throwable exception,
      Context context) {
    if (!isEnabled(severity, context)) {
      return;
    }
    // TODO: find way to reuse logic from SdkLogRecordBuilder#emit
    if (loggerSharedState.hasBeenShutdown()) {
      return;
    }

    LogLimits logLimits = loggerSharedState.getLogLimits();

    long now = loggerSharedState.getClock().now();
    AttributesMap attributesMap = null;
    if (!attributes.isEmpty()) {
      attributesMap =
          AttributesMap.create(
              logLimits.getMaxNumberOfAttributes(), logLimits.getMaxAttributeValueLength());
      attributes.forEach(attributesMap::put);
    }

    loggerSharedState
        .getLogRecordProcessor()
        .onEmit(
            context,
            SdkReadWriteLogRecord.create(
                logLimits,
                loggerSharedState.getResource(),
                instrumentationScopeInfo,
                now,
                now,
                Span.fromContext(context).getSpanContext(),
                severity,
                null,
                body,
                attributesMap,
                eventName));
  }

  @Override
  public ExtendedLogRecordBuilder logRecordBuilder() {
    return (ExtendedLogRecordBuilder) super.logRecordBuilder();
  }
}
