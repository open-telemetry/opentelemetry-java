/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributeUtil;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.Severity;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** {@link SdkLogBuilder} is the SDK implementation of {@link LogBuilder}. */
final class SdkLogBuilder implements LogBuilder {

  private final LogEmitterSharedState logEmitterSharedState;
  private final LogLimits logLimits;

  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private long epochNanos;
  private SpanContext spanContext = SpanContext.getInvalid();
  private Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
  @Nullable private String severityText;
  private Body body = Body.empty();
  private Attributes attributes = Attributes.empty();

  SdkLogBuilder(
      LogEmitterSharedState logEmitterSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo) {
    this.logEmitterSharedState = logEmitterSharedState;
    this.logLimits = logEmitterSharedState.getLogLimits();
    this.instrumentationScopeInfo = instrumentationScopeInfo;
  }

  @Override
  public LogBuilder setEpoch(long timestamp, TimeUnit unit) {
    this.epochNanos = unit.toNanos(timestamp);
    return this;
  }

  @Override
  public LogBuilder setEpoch(Instant instant) {
    this.epochNanos = TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
    return this;
  }

  @Override
  public LogBuilder setContext(Context context) {
    spanContext = Span.fromContext(context).getSpanContext();
    return this;
  }

  @Override
  public LogBuilder setSeverity(Severity severity) {
    this.severity = severity;
    return this;
  }

  @Override
  public LogBuilder setSeverityText(String severityText) {
    this.severityText = severityText;
    return this;
  }

  @Override
  public LogBuilder setBody(String body) {
    this.body = Body.string(body);
    return this;
  }

  @Override
  public LogBuilder setAttributes(Attributes attributes) {
    this.attributes =
        AttributeUtil.applyAttributesLimit(
            attributes,
            logLimits.getMaxNumberOfAttributes(),
            logLimits.getMaxAttributeValueLength());
    return this;
  }

  @Override
  public void emit() {
    if (logEmitterSharedState.hasBeenShutdown()) {
      return;
    }
    logEmitterSharedState
        .getLogProcessor()
        .emit(
            SdkLogData.create(
                logEmitterSharedState.getResource(),
                instrumentationScopeInfo,
                this.epochNanos == 0 ? logEmitterSharedState.getClock().now() : this.epochNanos,
                spanContext,
                severity,
                severityText,
                body,
                attributes));
  }
}
