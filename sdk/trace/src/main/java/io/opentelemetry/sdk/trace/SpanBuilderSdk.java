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
import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.MonotonicClock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** {@link SpanBuilderSdk} is SDK implementation of {@link SpanBuilder}. */
final class SpanBuilderSdk implements SpanBuilder {

  private final String spanName;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;
  private final SpanProcessor spanProcessor;
  private final TraceConfig traceConfig;
  private final Resource resource;
  private final IdGenerator idsGenerator;
  private final Clock clock;

  @Nullable private Context parent;
  private Kind spanKind = Kind.INTERNAL;
  @Nullable private AttributesMap attributes;
  @Nullable private List<Link> links;
  private int totalNumberOfLinksAdded = 0;
  private long startEpochNanos = 0;
  private boolean isRootSpan;

  SpanBuilderSdk(
      String spanName,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      SpanProcessor spanProcessor,
      TraceConfig traceConfig,
      Resource resource,
      IdGenerator idsGenerator,
      Clock clock) {
    this.spanName = spanName;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
    this.spanProcessor = spanProcessor;
    this.traceConfig = traceConfig;
    this.resource = resource;
    this.idsGenerator = idsGenerator;
    this.clock = clock;
  }

  @Override
  public SpanBuilder setParent(Context context) {
    Objects.requireNonNull(context, "context");
    this.isRootSpan = false;
    this.parent = context;
    return this;
  }

  @Override
  public SpanBuilder setNoParent() {
    this.isRootSpan = true;
    this.parent = null;
    return this;
  }

  @Override
  public SpanBuilder setSpanKind(Kind spanKind) {
    this.spanKind = Objects.requireNonNull(spanKind, "spanKind");
    return this;
  }

  @Override
  public SpanBuilder addLink(SpanContext spanContext) {
    addLink(Link.create(spanContext));
    return this;
  }

  @Override
  public SpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
    int totalAttributeCount = attributes.size();
    addLink(
        Link.create(
            spanContext,
            RecordEventsReadableSpan.copyAndLimitAttributes(
                attributes, traceConfig.getMaxNumberOfAttributesPerLink()),
            totalAttributeCount));
    return this;
  }

  private void addLink(Link link) {
    Objects.requireNonNull(link, "link");
    totalNumberOfLinksAdded++;
    if (links == null) {
      links = new ArrayList<>(traceConfig.getMaxNumberOfLinks());
    }

    // don't bother doing anything with any links beyond the max.
    if (links.size() == traceConfig.getMaxNumberOfLinks()) {
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
    Objects.requireNonNull(key, "key");
    if (value == null) {
      return this;
    }
    if (attributes == null) {
      attributes = new AttributesMap(traceConfig.getMaxNumberOfAttributes());
    }

    if (traceConfig.shouldTruncateStringAttributeValues()) {
      value = StringUtils.truncateToSize(key, value, traceConfig.getMaxLengthOfAttributeValues());
    }

    attributes.put(key, value);
    return this;
  }

  @Override
  public SpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit) {
    Utils.checkArgument(startTimestamp >= 0, "Negative startTimestamp");
    startEpochNanos = unit.toNanos(startTimestamp);
    return this;
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Span startSpan() {
    final Context parentContext =
        isRootSpan ? Context.root() : parent == null ? Context.current() : parent;
    final Span parentSpan = Span.fromContext(parentContext);
    final SpanContext parentSpanContext = parentSpan.getSpanContext();
    String traceId;
    String spanId = idsGenerator.generateSpanId();
    if (!parentSpanContext.isValid()) {
      // New root span.
      traceId = idsGenerator.generateTraceId();
    } else {
      // New child span.
      traceId = parentSpanContext.getTraceIdAsHexString();
    }
    List<SpanData.Link> immutableLinks =
        links == null ? Collections.emptyList() : Collections.unmodifiableList(links);
    // Avoid any possibility to modify the links list by adding links to the Builder after the
    // startSpan is called. If that happens all the links will be added in a new list.
    links = null;
    ReadableAttributes immutableAttributes = attributes == null ? Attributes.empty() : attributes;
    SamplingResult samplingResult =
        traceConfig
            .getSampler()
            .shouldSample(
                parentContext, traceId, spanName, spanKind, immutableAttributes, immutableLinks);
    SamplingResult.Decision samplingDecision = samplingResult.getDecision();

    TraceState samplingResultTraceState =
        samplingResult.getUpdatedTraceState(parentSpanContext.getTraceState());
    SpanContext spanContext =
        createSpanContext(traceId, spanId, samplingResultTraceState, isSampled(samplingDecision));

    if (!isRecording(samplingDecision)) {
      return Span.wrap(spanContext);
    }
    ReadableAttributes samplingAttributes = samplingResult.getAttributes();
    if (!samplingAttributes.isEmpty()) {
      if (attributes == null) {
        attributes = new AttributesMap(traceConfig.getMaxNumberOfAttributes());
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
        traceConfig,
        spanProcessor,
        getClock(parentSpan, clock),
        resource,
        recordedAttributes,
        immutableLinks,
        totalNumberOfLinksAdded,
        startEpochNanos);
  }

  private static SpanContext createSpanContext(
      String traceId, String spanId, TraceState traceState, boolean isSampled) {
    byte traceFlags = isSampled ? TraceFlags.getSampled() : TraceFlags.getDefault();
    return SpanContext.create(traceId, spanId, traceFlags, traceState);
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
  static boolean isRecording(SamplingResult.Decision decision) {
    return SamplingResult.Decision.RECORD_ONLY.equals(decision)
        || SamplingResult.Decision.RECORD_AND_SAMPLE.equals(decision);
  }

  // Visible for testing
  static boolean isSampled(SamplingResult.Decision decision) {
    return SamplingResult.Decision.RECORD_AND_SAMPLE.equals(decision);
  }
}
