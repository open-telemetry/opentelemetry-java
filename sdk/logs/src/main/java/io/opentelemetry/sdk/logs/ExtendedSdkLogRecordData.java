/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.common.ExtendedAttributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.internal.ExtendedLogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@AutoValue
@AutoValue.CopyAnnotations
@Immutable
@SuppressWarnings("deprecation")
abstract class ExtendedSdkLogRecordData implements ExtendedLogRecordData {

  ExtendedSdkLogRecordData() {}

  static ExtendedSdkLogRecordData create(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      @Nullable String eventName,
      long epochNanos,
      long observedEpochNanos,
      SpanContext spanContext,
      Severity severity,
      @Nullable String severityText,
      @Nullable Value<?> body,
      ExtendedAttributes attributes,
      int totalAttributeCount) {
    return new AutoValue_ExtendedSdkLogRecordData(
        resource,
        instrumentationScopeInfo,
        epochNanos,
        observedEpochNanos,
        spanContext,
        severity,
        severityText,
        totalAttributeCount,
        attributes,
        body,
        eventName);
  }

  @Override
  @Nullable
  public abstract Value<?> getBodyValue();

  @Override
  @Nullable
  public abstract String getEventName();

  @Override
  @SuppressWarnings("deprecation") // Implementation of deprecated method
  public Body getBody() {
    Value<?> valueBody = getBodyValue();
    return valueBody == null ? Body.empty() : Body.string(valueBody.asString());
  }
}
