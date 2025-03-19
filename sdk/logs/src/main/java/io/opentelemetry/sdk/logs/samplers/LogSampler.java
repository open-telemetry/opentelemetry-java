/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.samplers;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface LogSampler {

  static LogSampler alwaysOnSampler() {
    return (parentContext, severity, value, attributes) -> true;
  }

  static LogSampler parentBasedSampler() {
    return (parentContext, severity, value, attributes) -> {
      SpanContext spanContext = Span.fromContext(parentContext).getSpanContext();
      return spanContext.isSampled();
    };
  }

  boolean shouldSample(
      Context parentContext,
      Severity severity,
      @Nullable Value<?> value,
      @Nullable Attributes attributes);
}
