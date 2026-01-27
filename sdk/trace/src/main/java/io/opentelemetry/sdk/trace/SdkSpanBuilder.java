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
import io.opentelemetry.api.internal.ImmutableSpanContext;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributeUtil;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** {@link SdkSpanBuilder} is SDK implementation of {@link SpanBuilder}. */
class SdkSpanBuilder implements SpanBuilder {

  private final String spanName;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final TracerSharedState tracerSharedState;
  private final SpanLimits spanLimits;
  private final Context rootContextWithRandomTraceIdBit;

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
    this.rootContextWithRandomTraceIdBit =
        preparePrimordialContext(
            Context.root(),
            TraceFlags.getDefault().withRandomTraceIdBit(),
            TraceState.getDefault());
  }

  /*
   * A primordial context can be passed as the parent context for a root span
   * if a non-default TraceFlags or TraceState need to be passed to the sampler
   */
  private static Context preparePrimordialContext(
      Context parentContext, TraceFlags traceFlags, TraceState traceState) {
    SpanContext spanContext =
        SpanContext.create(TraceId.getInvalid(), SpanId.getInvalid(), traceFlags, traceState);
    Span span = Span.wrap(spanContext);
    return span.storeInContext(parentContext);
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
    attributes().put(key, value);
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
    Context parentContext = parent == null ? Context.current() : parent;
    Span parentSpan = Span.fromContext(parentContext);
    SpanContext parentSpanContext = parentSpan.getSpanContext();
    String traceId;
    boolean isTraceIdRandom;
    IdGenerator idGenerator = tracerSharedState.getIdGenerator();
    String spanId = idGenerator.generateSpanId();

    Context parentContextForSampler = parentContext;
    if (!parentSpanContext.isValid()) {
      // New root span.
      traceId = idGenerator.generateTraceId();
      if (idGenerator.generatesRandomTraceIds()) {
        isTraceIdRandom = true;
        // Replace parentContext for sampling with one with RANDOM_TRACE_ID bit set
        parentContextForSampler = rootContextWithRandomTraceIdBit;
      } else {
        isTraceIdRandom = false;
      }
    } else {
      // New child span.
      traceId = parentSpanContext.getTraceId();
      isTraceIdRandom = parentSpanContext.getTraceFlags().isTraceIdRandom();
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
                parentContextForSampler,
                traceId,
                spanName,
                spanKind,
                immutableAttributes,
                immutableLinks);
    SamplingDecision samplingDecision = samplingResult.getDecision();

    TraceState samplingResultTraceState =
        samplingResult.getUpdatedTraceState(parentSpanContext.getTraceState());
    SpanContext spanContext =
        ImmutableSpanContext.create(
            traceId,
            spanId,
            newTraceFlags(isTraceIdRandom, isSampled(samplingDecision)),
            samplingResultTraceState,
            /* remote= */ false,
            tracerSharedState.isIdGeneratorSafeToSkipIdValidation());

    Runnable recordEndSpanMetrics =
        tracerSharedState.getTracerInstrumentation().startSpan(parentSpanContext, samplingDecision);

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
        tracerSharedState.getExceptionAttributesResolver(),
        tracerSharedState.getClock(),
        tracerSharedState.getResource(),
        recordedAttributes,
        currentLinks,
        totalNumberOfLinksAdded,
        startEpochNanos,
        recordEndSpanMetrics);
  }

  private static TraceFlags newTraceFlags(boolean randomTraceId, boolean sampled) {
    TraceFlags traceFlags = TraceFlags.getDefault();
    if (randomTraceId) {
      traceFlags = traceFlags.withRandomTraceIdBit();
    }
    if (sampled) {
      traceFlags = traceFlags.withSampledBit();
    }
    return traceFlags;
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
}
