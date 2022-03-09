/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@AutoValue
@AutoValue.CopyAnnotations
@Immutable
@SuppressWarnings("deprecation") // name field to be removed
abstract class LogDataImpl implements LogData {

  LogDataImpl() {}

  static LogDataImpl create(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long epochNanos,
      SpanContext spanContext,
      Severity severity,
      @Nullable String severityText,
      @Nullable String name,
      Body body,
      Attributes attributes) {
    return new AutoValue_LogDataImpl(
        resource,
        instrumentationScopeInfo,
        epochNanos,
        spanContext,
        severity,
        severityText,
        name,
        body,
        attributes);
  }
}
