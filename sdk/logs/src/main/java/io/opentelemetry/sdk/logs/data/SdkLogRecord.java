/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** Sdk implementation of {@link LogRecord}. */
@AutoValue
@Immutable
abstract class SdkLogRecord implements LogRecord {

  SdkLogRecord() {}

  static SdkLogRecord create(
      long epochNanos,
      @Nullable String traceId,
      @Nullable String spanId,
      int flags,
      Severity severity,
      @Nullable String severityText,
      @Nullable String name,
      Body body,
      Attributes attributes) {
    return new AutoValue_SdkLogRecord(
        epochNanos, traceId, spanId, flags, severity, severityText, name, body, attributes);
  }
}
