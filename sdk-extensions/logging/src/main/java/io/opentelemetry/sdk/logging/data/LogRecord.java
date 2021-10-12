/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logging.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class LogRecord implements LogData {

  LogRecord() {}

  public static LogRecordBuilder builder(
      Resource resource, InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return new LogRecordBuilder(resource, instrumentationLibraryInfo);
  }

  static LogRecord create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      long epochNanos,
      String traceId,
      String spanId,
      int flags,
      Severity severity,
      @Nullable String severityText,
      @Nullable String name,
      Body body,
      Attributes attributes) {
    return new AutoValue_LogRecord(
        resource,
        instrumentationLibraryInfo,
        epochNanos,
        traceId,
        spanId,
        flags,
        severity,
        severityText,
        name,
        body,
        attributes);
  }
}
