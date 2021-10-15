/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.api.common.Attributes;

public interface LogBuilder {

  LogBuilder setEpochNanos(long timestamp);

  LogBuilder setEpochMillis(long timestamp);

  LogBuilder setTraceId(String traceId);

  LogBuilder setSpanId(String spanId);

  LogBuilder setFlags(int flags);

  LogBuilder setSeverity(Severity severity);

  LogBuilder setSeverityText(String severityText);

  LogBuilder setName(String name);

  LogBuilder setBody(Body body);

  LogBuilder setBody(String body);

  LogBuilder setAttributes(Attributes attributes);

  LogData build();
}
