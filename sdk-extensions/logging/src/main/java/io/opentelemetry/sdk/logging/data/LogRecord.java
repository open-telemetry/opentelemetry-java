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

/**
 * A LogRecord is an implementation of the <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/logs/data-model.md">
 * OpenTelemetry logging model</a>.
 */
@AutoValue
public abstract class LogRecord {

  public static LogRecordBuilder builder() {
    return new LogRecordBuilder();
  }

  static LogRecord create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      long timeUnixNano,
      String traceId,
      String spanId,
      int flags,
      Severity severity,
      String severityText,
      String name,
      String body,
      Attributes attributes) {
    return new AutoValue_LogRecord(
        resource,
        instrumentationLibraryInfo,
        timeUnixNano,
        traceId,
        spanId,
        flags,
        severity,
        severityText,
        name,
        body,
        attributes);
  }

  public abstract Resource getResource();

  public abstract InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  public abstract long getTimeUnixNano();

  public abstract String getTraceId();

  public abstract String getSpanId();

  public abstract int getFlags();

  public abstract Severity getSeverity();

  @Nullable
  public abstract String getSeverityText();

  @Nullable
  public abstract String getName();

  public abstract String getBody();

  public abstract Attributes getAttributes();

  public enum Severity {
    UNDEFINED_SEVERITY_NUMBER(0),
    TRACE(1),
    TRACE2(2),
    TRACE3(3),
    TRACE4(4),
    DEBUG(5),
    DEBUG2(6),
    DEBUG3(7),
    DEBUG4(8),
    INFO(9),
    INFO2(10),
    INFO3(11),
    INFO4(12),
    WARN(13),
    WARN2(14),
    WARN3(15),
    WARN4(16),
    ERROR(17),
    ERROR2(18),
    ERROR3(19),
    ERROR4(20),
    FATAL(21),
    FATAL2(22),
    FATAL3(23),
    FATAL4(24),
    ;

    private final int severityNumber;

    Severity(int severityNumber) {
      this.severityNumber = severityNumber;
    }

    public int getSeverityNumber() {
      return severityNumber;
    }
  }
}
