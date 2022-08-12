/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.logs.data.Severity;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class NoopLogEmitterBuilder implements LogEmitterBuilder {

  private static final NoopLogEmitterBuilder INSTANCE = new NoopLogEmitterBuilder();
  private static final NoopLogEmitter NOOP_LOG_EMITTER = new NoopLogEmitter();

  private NoopLogEmitterBuilder() {}

  static LogEmitterBuilder getInstance() {
    return INSTANCE;
  }

  @Override
  public LogEmitterBuilder setSchemaUrl(String schemaUrl) {
    return this;
  }

  @Override
  public LogEmitterBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
    return this;
  }

  @Override
  public LogEmitter build() {
    return NOOP_LOG_EMITTER;
  }

  private static class NoopLogEmitter implements LogEmitter {

    private static final NoopLogBuilder NOOP_LOG_BUILDER = new NoopLogBuilder();

    private NoopLogEmitter() {}

    @Override
    public LogRecordBuilder logRecordBuilder() {
      return NOOP_LOG_BUILDER;
    }
  }

  private static class NoopLogBuilder implements LogRecordBuilder {

    private NoopLogBuilder() {}

    @Override
    public LogRecordBuilder setEpoch(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public LogRecordBuilder setEpoch(Instant instant) {
      return this;
    }

    @Override
    public LogRecordBuilder setContext(Context context) {
      return this;
    }

    @Override
    public LogRecordBuilder setSeverity(Severity severity) {
      return this;
    }

    @Override
    public LogRecordBuilder setSeverityText(String severityText) {
      return this;
    }

    @Override
    public LogRecordBuilder setBody(String body) {
      return this;
    }

    @Override
    public <T> LogRecordBuilder setAttribute(AttributeKey<T> key, T value) {
      return this;
    }

    @Override
    public void emit() {}
  }
}
