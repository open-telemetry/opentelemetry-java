/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.propagation.ExtendedContextPropagators;
import io.opentelemetry.api.incubator.trace.ExtendedSpanBuilder;
import io.opentelemetry.api.incubator.trace.SpanCallable;
import io.opentelemetry.api.incubator.trace.SpanRunnable;
import io.opentelemetry.api.internal.ImmutableSpanContext;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributeUtil;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

/** {@link SdkSpanBuilder} is SDK implementation of {@link SpanBuilder}. */
final class SdkSpanBuilder implements ExtendedSpanBuilder {

  private final String spanName;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final TracerSharedState tracerSharedState;
  private final SpanLimits spanLimits;

  @Nullable private Context parent; // null means: Use current context.
  private SpanKind spanKind = SpanKind.INTERNAL;
  @Nullable private AttributesMap attributes;
  @Nullable private List<LinkData> links;
  private int totalNumberOfLinksAdded = 0;
  private long startEpochNanos = 0;

  SdkSpanBuilder(
      String spanName,
      InstrumentationScopeInfo instrumentationScopeInfo,
      TracerSharedState tracerSharedState,
      SpanLimits spanLimits) {
    this.spanName = spanName;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.tracerSharedState = tracerSharedState;
    this.spanLimits = spanLimits;
  }

  @Override
  public ExtendedSpanBuilder setParent(Context context) {
    if (context == null) {
      return this;
    }
    this.parent = context;
    return this;
  }

  @Override
  public ExtendedSpanBuilder setNoParent() {
    this.parent = Context.root();
    return this;
  }

  @Override
  public ExtendedSpanBuilder setSpanKind(SpanKind spanKind) {
    if (spanKind == null) {
      return this;
    }
    this.spanKind = spanKind;
    return this;
  }

  @Override
  public ExtendedSpanBuilder addLink(SpanContext spanContext) {
    if (spanContext == null || !spanContext.isValid()) {
      return this;
    }
    addLink(LinkData.create(spanContext));
    return this;
  }

  @Override
  public ExtendedSpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
    if (spanContext == null || !spanContext.isValid()) {
      return this;
    }
    if (attributes == null) {
      attributes = Attributes.empty();
    }
    int totalAttributeCount = attributes.size();
    addLink(
        LinkData.create(
            spanContext,
            AttributeUtil.applyAttributesLimit(
                attributes,
                spanLimits.getMaxNumberOfAttributesPerLink(),
                spanLimits.getMaxAttributeValueLength()),
            totalAttributeCount));
    return this;
  }

  private void addLink(LinkData link) {
    totalNumberOfLinksAdded++;
    if (links == null) {
      links = new ArrayList<>(spanLimits.getMaxNumberOfLinks());
    }

    // don't bother doing anything with any links beyond the max.
    if (links.size() == spanLimits.getMaxNumberOfLinks()) {
      return;
    }

    links.add(link);
  }

  @Override
  public ExtendedSpanBuilder setAttribute(String key, String value) {
    return setAttribute(stringKey(key), value);
  }

  @Override
  public ExtendedSpanBuilder setAttribute(String key, long value) {
    return setAttribute(longKey(key), value);
  }

  @Override
  public ExtendedSpanBuilder setAttribute(String key, double value) {
    return setAttribute(doubleKey(key), value);
  }

  @Override
  public ExtendedSpanBuilder setAttribute(String key, boolean value) {
    return setAttribute(booleanKey(key), value);
  }

  @Override
  public <T> ExtendedSpanBuilder setAttribute(AttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    attributes().put(key, value);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit) {
    if (startTimestamp < 0 || unit == null) {
      return this;
    }
    startEpochNanos = unit.toNanos(startTimestamp);
    return this;
  }

  @Override
  public ExtendedSpanBuilder setParentFrom(
      ContextPropagators propagators, Map<String, String> carrier) {
    setParent(ExtendedContextPropagators.extractTextMapPropagationContext(carrier, propagators));
    return this;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Span startSpan() {
    Context parentContext = parent == null ? Context.current() : parent;
    Span parentSpan = Span.fromContext(parentContext);
    SpanContext parentSpanContext = parentSpan.getSpanContext();
    String traceId;
    IdGenerator idGenerator = tracerSharedState.getIdGenerator();
    String spanId = idGenerator.generateSpanId();
    if (!parentSpanContext.isValid()) {
      // New root span.
      traceId = idGenerator.generateTraceId();
    } else {
      // New child span.
      traceId = parentSpanContext.getTraceId();
    }
    List<LinkData> currentLinks = links;
    List<LinkData> immutableLinks =
        currentLinks == null ? Collections.emptyList() : Collections.unmodifiableList(currentLinks);
    // Avoid any possibility to modify the links list by adding links to the Builder after the
    // startSpan is called. If that happens all the links will be added in a new list.
    links = null;
    Attributes immutableAttributes = attributes == null ? Attributes.empty() : attributes;
    SamplingResult samplingResult =
        tracerSharedState
            .getSampler()
            .shouldSample(
                parentContext, traceId, spanName, spanKind, immutableAttributes, immutableLinks);
    SamplingDecision samplingDecision = samplingResult.getDecision();

    TraceState samplingResultTraceState =
        samplingResult.getUpdatedTraceState(parentSpanContext.getTraceState());
    SpanContext spanContext =
        ImmutableSpanContext.create(
            traceId,
            spanId,
            isSampled(samplingDecision) ? TraceFlags.getSampled() : TraceFlags.getDefault(),
            samplingResultTraceState,
            /* remote= */ false,
            tracerSharedState.isIdGeneratorSafeToSkipIdValidation());

    if (!isRecording(samplingDecision)) {
      return Span.wrap(spanContext);
    }
    Attributes samplingAttributes = samplingResult.getAttributes();
    if (!samplingAttributes.isEmpty()) {
      samplingAttributes.forEach((key, value) -> attributes().put((AttributeKey) key, value));
    }

    // Avoid any possibility to modify the attributes by adding attributes to the Builder after the
    // startSpan is called. If that happens all the attributes will be added in a new map.
    AttributesMap recordedAttributes = attributes;
    attributes = null;

    return SdkSpan.startSpan(
        spanContext,
        spanName,
        instrumentationScopeInfo,
        spanKind,
        parentSpan,
        parentContext,
        spanLimits,
        tracerSharedState.getActiveSpanProcessor(),
        tracerSharedState.getClock(),
        tracerSharedState.getResource(),
        recordedAttributes,
        currentLinks,
        totalNumberOfLinksAdded,
        startEpochNanos);
  }

  @Override
  public <T, E extends Throwable> T startAndCall(SpanCallable<T, E> spanCallable) throws E {
    return startAndCall(spanCallable, SdkSpanBuilder::setSpanError);
  }

  @Override
  public <T, E extends Throwable> T startAndCall(
      SpanCallable<T, E> spanCallable, BiConsumer<Span, Throwable> handleException) throws E {
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
    startAndRun(runnable, SdkSpanBuilder::setSpanError);
  }

  @SuppressWarnings("NullAway")
  @Override
  public <E extends Throwable> void startAndRun(
      SpanRunnable<E> runnable, BiConsumer<Span, Throwable> handleException) throws E {
    startAndCall(
        () -> {
          runnable.runInSpan();
          return null;
        },
        handleException);
  }

  private AttributesMap attributes() {
    AttributesMap attributes = this.attributes;
    if (attributes == null) {
      this.attributes =
          AttributesMap.create(
              spanLimits.getMaxNumberOfAttributes(), spanLimits.getMaxAttributeValueLength());
      attributes = this.attributes;
    }
    return attributes;
  }

  // Visible for testing
  static boolean isRecording(SamplingDecision decision) {
    return SamplingDecision.RECORD_ONLY.equals(decision)
        || SamplingDecision.RECORD_AND_SAMPLE.equals(decision);
  }

  // Visible for testing
  static boolean isSampled(SamplingDecision decision) {
    return SamplingDecision.RECORD_AND_SAMPLE.equals(decision);
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
