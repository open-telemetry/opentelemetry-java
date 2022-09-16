/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@AutoValue
@AutoValue.CopyAnnotations
@Immutable
abstract class SdkLogData implements LogData {

  SdkLogData() {}

  static SdkLogData create(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long epochNanos,
      SpanContext spanContext,
      Severity severity,
      @Nullable String severityText,
      Body body,
      Attributes attributes,
      int totalAttributeCount) {
    return new AutoValue_SdkLogData(
        resource,
        instrumentationScopeInfo,
        epochNanos,
        spanContext,
        severity,
        severityText,
        body,
        attributes,
        totalAttributeCount);
  }
}
