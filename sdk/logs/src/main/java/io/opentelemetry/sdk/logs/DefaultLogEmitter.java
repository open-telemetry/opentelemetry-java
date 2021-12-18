/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.logs.data.Severity;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

/**
 * No-op implementations of {@link LogEmitter}.
 *
 * <p>This implementation should induce as close to zero overhead as possible.
 */
@ThreadSafe
class DefaultLogEmitter implements LogEmitter {

  private static final DefaultLogEmitter INSTANCE = new DefaultLogEmitter();
  private static final LogBuilder BUILDER_INSTANCE = new NoopLogBuilder();

  static LogEmitter getInstance() {
    return INSTANCE;
  }

  @Override
  public LogBuilder logBuilder() {
    return BUILDER_INSTANCE;
  }

  // Noop implementation of LogBuilder.
  private static final class NoopLogBuilder implements LogBuilder {

    @Override
    public LogBuilder setEpoch(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public LogBuilder setEpoch(Instant instant) {
      return this;
    }

    @Override
    public LogBuilder setContext(Context context) {
      return this;
    }

    @Override
    public LogBuilder setSeverity(Severity severity) {
      return this;
    }

    @Override
    public LogBuilder setSeverityText(String severityText) {
      return this;
    }

    @Override
    public LogBuilder setName(String name) {
      return this;
    }

    @Override
    public LogBuilder setBody(String body) {
      return this;
    }

    @Override
    public LogBuilder setAttributes(Attributes attributes) {
      return this;
    }

    @Override
    public void emit() {}
  }
}
