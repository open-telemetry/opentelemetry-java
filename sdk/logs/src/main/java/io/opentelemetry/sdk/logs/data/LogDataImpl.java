/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class LogDataImpl implements LogData {

  LogDataImpl() {}

  static LogDataImpl create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      long epochNanos,
      @Nullable String traceId,
      @Nullable String spanId,
      int flags,
      Severity severity,
      @Nullable String severityText,
      @Nullable String name,
      Body body,
      Attributes attributes) {
    return new AutoValue_LogDataImpl(
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
