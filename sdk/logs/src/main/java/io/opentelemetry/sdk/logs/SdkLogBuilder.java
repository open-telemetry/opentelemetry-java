/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.AttributeUtil;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;
import io.opentelemetry.sdk.logs.data.Severity;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/** {@link SdkLogBuilder} is the SDK implementation of {@link LogBuilder}. */
final class SdkLogBuilder implements LogBuilder {

  private final LogDataBuilder logDataBuilder;
  private final LogEmitterSharedState logEmitterSharedState;
  private final LogLimits logLimits;

  SdkLogBuilder(LogEmitterSharedState logEmitterSharedState, LogDataBuilder logDataBuilder) {
    this.logEmitterSharedState = logEmitterSharedState;
    this.logDataBuilder = logDataBuilder;
    this.logLimits = logEmitterSharedState.getLogLimits();
  }

  @Override
  public LogBuilder setEpoch(long timestamp, TimeUnit unit) {
    logDataBuilder.setEpoch(timestamp, unit);
    return this;
  }

  @Override
  public LogBuilder setEpoch(Instant instant) {
    logDataBuilder.setEpoch(instant);
    return this;
  }

  @Override
  public LogBuilder setContext(Context context) {
    logDataBuilder.setContext(context);
    return this;
  }

  @Override
  public LogBuilder setSeverity(Severity severity) {
    logDataBuilder.setSeverity(severity);
    return this;
  }

  @Override
  public LogBuilder setSeverityText(String severityText) {
    logDataBuilder.setSeverityText(severityText);
    return this;
  }

  @Override
  public LogBuilder setBody(String body) {
    logDataBuilder.setBody(body);
    return this;
  }

  @Override
  public LogBuilder setAttributes(Attributes attributes) {
    logDataBuilder.setAttributes(
        AttributeUtil.applyAttributesLimit(
            attributes,
            logLimits.getMaxNumberOfAttributes(),
            logLimits.getMaxAttributeValueLength()));
    return this;
  }

  @Override
  public void emit() {
    if (logEmitterSharedState.hasBeenShutdown()) {
      return;
    }
    logEmitterSharedState.getLogProcessor().emit(logDataBuilder.build());
  }
}
