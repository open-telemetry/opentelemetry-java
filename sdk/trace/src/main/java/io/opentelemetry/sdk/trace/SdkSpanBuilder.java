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
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.MonotonicClock;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** {@link SdkSpanBuilder} is SDK implementation of {@link SpanBuilder}. */
final class SdkSpanBuilder implements SpanBuilder {

  private final String spanName;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;
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
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      TracerSharedState tracerSharedState,
      SpanLimits spanLimits) {
    this.spanName = spanName;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
    this.tracerSharedState = tracerSharedState;
    this.spanLimits = spanLimits;
  }

  @Override
  public SpanBuilder setParent(Context context) {
    if (context == null) {
      return this;
    }
    this.parent = context;
    return this;
  }

  @Override
  public SpanBuilder setNoParent() {
    this.parent = Context.root();
    return this;
  }

  @Override
  public SpanBuilder setSpanKind(SpanKind spanKind) {
    if (spanKind == null) {
      return this;
    }
    this.spanKind = spanKind;
    return this;
  }

  @Override
  public SpanBuilder addLink(SpanContext spanContext) {
    if (spanContext == null || !spanContext.isValid()) {
      return this;
    }
    addLink(LinkData.create(spanContext));
    return this;
  }

  @Override
  public SpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
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
            RecordEventsReadableSpan.applyAttributesLimit(
                attributes, spanLimits.getMaxNumberOfAttributesPerLink()),
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
  public SpanBuilder setAttribute(String key, String value) {
    return setAttribute(stringKey(key), value);
  }

  @Override
  public SpanBuilder setAttribute(String key, long value) {
    return setAttribute(longKey(key), value);
  }

  @Override
  public SpanBuilder setAttribute(String key, double value) {
    return setAttribute(doubleKey(key), value);
  }

  @Override
  public SpanBuilder setAttribute(String key, boolean value) {
    return setAttribute(booleanKey(key), value);
  }

  @Override
  public <T> SpanBuilder setAttribute(AttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    if (attributes == null) {
      attributes = new AttributesMap(spanLimits.getMaxNumberOfAttributes());
    }

    attributes.put(key, value);
    return this;
  }

  @Override
  public SpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit) {
    if (startTimestamp < 0 || unit == null) {
      return this;
    }
    startEpochNanos = unit.toNanos(startTimestamp);
    return this;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Span startSpan() {
    final Context parentContext = parent == null ? Context.current() : parent;
    final Span parentSpan = Span.fromContext(parentContext);
    final SpanContext parentSpanContext = parentSpan.getSpanContext();
    final String traceId;
    IdGenerator idGenerator = tracerSharedState.getIdGenerator();
    String spanId = idGenerator.generateSpanId();
    if (!parentSpanContext.isValid()) {
      // New root span.
      traceId = idGenerator.generateTraceId();
    } else {
      // New child span.
      traceId = parentSpanContext.getTraceId();
    }
    List<LinkData> immutableLinks =
        links == null ? Collections.emptyList() : Collections.unmodifiableList(links);
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
        SpanContext.create(
            traceId,
            spanId,
            isSampled(samplingDecision) ? TraceFlags.getSampled() : TraceFlags.getDefault(),
            samplingResultTraceState);

    if (!isRecording(samplingDecision)) {
      return Span.wrap(spanContext);
    }
    Attributes samplingAttributes = samplingResult.getAttributes();
    if (!samplingAttributes.isEmpty()) {
      if (attributes == null) {
        attributes = new AttributesMap(spanLimits.getMaxNumberOfAttributes());
      }
      samplingAttributes.forEach((key, value) -> attributes.put((AttributeKey) key, value));
    }

    // Avoid any possibility to modify the attributes by adding attributes to the Builder after the
    // startSpan is called. If that happens all the attributes will be added in a new map.
    AttributesMap recordedAttributes = attributes;
    attributes = null;

    return RecordEventsReadableSpan.startSpan(
        spanContext,
        spanName,
        instrumentationLibraryInfo,
        spanKind,
        parentSpanContext,
        parentContext,
        spanLimits,
        tracerSharedState.getActiveSpanProcessor(),
        getClock(parentSpan, tracerSharedState.getClock()),
        tracerSharedState.getResource(),
        recordedAttributes,
        immutableLinks,
        totalNumberOfLinksAdded,
        startEpochNanos);
  }

  private static Clock getClock(Span parent, Clock clock) {
    if (parent instanceof RecordEventsReadableSpan) {
      RecordEventsReadableSpan parentRecordEventsSpan = (RecordEventsReadableSpan) parent;
      return parentRecordEventsSpan.getClock();
    } else {
      return MonotonicClock.create(clock);
    }
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
}
