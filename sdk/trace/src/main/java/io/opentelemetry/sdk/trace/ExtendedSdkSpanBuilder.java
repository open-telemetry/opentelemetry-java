/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.propagation.ExtendedContextPropagators;
import io.opentelemetry.api.incubator.trace.ExtendedSpanBuilder;
import io.opentelemetry.api.incubator.trace.SpanCallable;
import io.opentelemetry.api.incubator.trace.SpanRunnable;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.common.ApiUsageLogger;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

/** {@link ExtendedSdkSpanBuilder} is SDK implementation of {@link ExtendedSpanBuilder}. */
final class ExtendedSdkSpanBuilder extends SdkSpanBuilder implements ExtendedSpanBuilder {

  ExtendedSdkSpanBuilder(
      String spanName,
      InstrumentationScopeInfo instrumentationScopeInfo,
      TracerSharedState tracerSharedState,
      SpanLimits spanLimits) {
    super(spanName, instrumentationScopeInfo, tracerSharedState, spanLimits);
  }

  @Override
  public ExtendedSpanBuilder setParent(Context context) {
    super.setParent(context);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setNoParent() {
    super.setNoParent();
    return this;
  }

  @Override
  public ExtendedSpanBuilder setSpanKind(SpanKind spanKind) {
    super.setSpanKind(spanKind);
    return this;
  }

  @Override
  public ExtendedSpanBuilder addLink(SpanContext spanContext) {
    super.addLink(spanContext);
    return this;
  }

  @Override
  public ExtendedSpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
    super.addLink(spanContext, attributes);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setAttribute(String key, @Nullable String value) {
    super.setAttribute(key, value);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setAttribute(String key, long value) {
    super.setAttribute(key, value);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setAttribute(String key, double value) {
    super.setAttribute(key, value);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setAttribute(String key, boolean value) {
    super.setAttribute(key, value);
    return this;
  }

  @Override
  public <T> ExtendedSpanBuilder setAttribute(AttributeKey<T> key, @Nullable T value) {
    super.setAttribute(key, value);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit) {
    super.setStartTimestamp(startTimestamp, unit);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setParentFrom(
      ContextPropagators propagators, Map<String, String> carrier) {
    if (propagators == null) {
      ApiUsageLogger.logNullParam(ExtendedSdkSpanBuilder.class, "setParentFrom", "propagators");
      return this;
    }
    if (carrier == null) {
      ApiUsageLogger.logNullParam(ExtendedSdkSpanBuilder.class, "setParentFrom", "carrier");
      return this;
    }
    super.setParent(
        ExtendedContextPropagators.extractTextMapPropagationContext(carrier, propagators));
    return this;
  }

  @SuppressWarnings("NullAway")
  @Override
  public <T, E extends Throwable> T startAndCall(SpanCallable<T, E> spanCallable) throws E {
    if (spanCallable == null) {
      ApiUsageLogger.logNullParam(ExtendedSdkSpanBuilder.class, "startAndCall", "spanCallable");
      return null;
    }
    return startAndCall(spanCallable, ExtendedSdkSpanBuilder::setSpanError);
  }

  @SuppressWarnings("NullAway")
  @Override
  public <T, E extends Throwable> T startAndCall(
      SpanCallable<T, E> spanCallable, BiConsumer<Span, Throwable> handleException) throws E {
    if (spanCallable == null) {
      ApiUsageLogger.logNullParam(ExtendedSdkSpanBuilder.class, "startAndCall", "spanCallable");
      return null;
    }
    if (handleException == null) {
      ApiUsageLogger.logNullParam(ExtendedSdkSpanBuilder.class, "startAndCall", "handleException");
      return null;
    }
    Span span = startSpan();

    //noinspection unused
    try (Scope unused = span.makeCurrent()) {
      return spanCallable.callInSpan();
    } catch (Throwable e) {
      handleException.accept(span, e);
      throw e;
    } finally {
      span.end();
    }
  }

  @Override
  public <E extends Throwable> void startAndRun(SpanRunnable<E> runnable) throws E {
    if (runnable == null) {
      ApiUsageLogger.logNullParam(ExtendedSdkSpanBuilder.class, "startAndRun", "runnable");
      return;
    }
    startAndRun(runnable, ExtendedSdkSpanBuilder::setSpanError);
  }

  @SuppressWarnings("NullAway")
  @Override
  public <E extends Throwable> void startAndRun(
      SpanRunnable<E> runnable, BiConsumer<Span, Throwable> handleException) throws E {
    if (runnable == null) {
      ApiUsageLogger.logNullParam(ExtendedSdkSpanBuilder.class, "startAndRun", "runnable");
      return;
    }
    if (handleException == null) {
      ApiUsageLogger.logNullParam(ExtendedSdkSpanBuilder.class, "startAndRun", "handleException");
      return;
    }
    startAndCall(
        () -> {
          runnable.runInSpan();
          return null;
        },
        handleException);
  }

  /**
   * Marks a span as error. This is the default exception handler.
   *
   * @param span the span
   * @param exception the exception that caused the error
   */
  private static void setSpanError(Span span, Throwable exception) {
    span.setStatus(StatusCode.ERROR);
    span.recordException(exception);
  }
}
