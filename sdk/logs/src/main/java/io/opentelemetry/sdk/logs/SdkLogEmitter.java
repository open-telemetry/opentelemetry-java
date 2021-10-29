/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;
import io.opentelemetry.sdk.logs.data.Severity;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/** SDK implementation of {@link LogEmitter}. */
final class SdkLogEmitter implements LogEmitter {

  private final LogEmitterSharedState logEmitterSharedState;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  SdkLogEmitter(
      LogEmitterSharedState logEmitterSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.logEmitterSharedState = logEmitterSharedState;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  @Override
  public LogBuilder logBuilder() {
    return new SdkLogBuilder();
  }

  // VisibleForTesting
  InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return instrumentationLibraryInfo;
  }

  private final class SdkLogBuilder implements LogBuilder {

    private final LogDataBuilder logDataBuilder;

    SdkLogBuilder() {
      this.logDataBuilder =
          LogData.builder(logEmitterSharedState.getResource(), instrumentationLibraryInfo);
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
    public LogBuilder setTraceId(String traceId) {
      logDataBuilder.setTraceId(traceId);
      return this;
    }

    @Override
    public LogBuilder setSpanId(String spanId) {
      logDataBuilder.setSpanId(spanId);
      return this;
    }

    @Override
    public LogBuilder setFlags(int flags) {
      logDataBuilder.setFlags(flags);
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
    public LogBuilder setName(String name) {
      logDataBuilder.setName(name);
      return this;
    }

    @Override
    public LogBuilder setBody(Body body) {
      logDataBuilder.setBody(body);
      return this;
    }

    @Override
    public LogBuilder setBody(String body) {
      logDataBuilder.setBody(body);
      return this;
    }

    @Override
    public LogBuilder setAttributes(Attributes attributes) {
      logDataBuilder.setAttributes(attributes);
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
}
