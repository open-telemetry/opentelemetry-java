/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AnyValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@AutoValue
@AutoValue.CopyAnnotations
@Immutable
abstract class SdkLogRecordData implements LogRecordData {

  SdkLogRecordData() {}

  static SdkLogRecordData create(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long epochNanos,
      long observedEpochNanos,
      SpanContext spanContext,
      Severity severity,
      @Nullable String severityText,
      @Nullable AnyValue<?> body,
      Attributes attributes,
      int totalAttributeCount) {
    return new AutoValue_SdkLogRecordData(
        resource,
        instrumentationScopeInfo,
        epochNanos,
        observedEpochNanos,
        spanContext,
        severity,
        severityText,
        attributes,
        totalAttributeCount,
        body);
  }

  @Override
  @Nullable
  public abstract AnyValue<?> getAnyValueBody();

  @Override
  @SuppressWarnings("deprecation") // Implementation of deprecated method
  public io.opentelemetry.sdk.logs.data.Body getBody() {
    AnyValue<?> anyValueBody = getAnyValueBody();
    return anyValueBody == null
        ? io.opentelemetry.sdk.logs.data.Body.empty()
        : io.opentelemetry.sdk.logs.data.Body.string(anyValueBody.asString());
  }
}
