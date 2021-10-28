/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/** Builder for {@link LogRecord}. */
public interface LogRecordBuilder {

  /** Set the epoch timestamp using the timestamp and unit. */
  LogRecordBuilder setEpoch(long timestamp, TimeUnit unit);

  /** Set the epoch timestamp using the instant. */
  LogRecordBuilder setEpoch(Instant instant);

  /** Set the trace id. */
  LogRecordBuilder setTraceId(String traceId);

  /** Set the span id. */
  LogRecordBuilder setSpanId(String spanId);

  /** Set the flags. */
  LogRecordBuilder setFlags(int flags);

  /** Set the severity. */
  LogRecordBuilder setSeverity(Severity severity);

  /** Set the severity text. */
  LogRecordBuilder setSeverityText(String severityText);

  /** Set the name. */
  LogRecordBuilder setName(String name);

  /** Set the body. */
  LogRecordBuilder setBody(Body body);

  /** Set the body string. */
  LogRecordBuilder setBody(String body);

  /** Set the attributes. */
  LogRecordBuilder setAttributes(Attributes attributes);

  /** Build a {@link LogRecord} instance from the configured properties. */
  LogRecord build();
}
