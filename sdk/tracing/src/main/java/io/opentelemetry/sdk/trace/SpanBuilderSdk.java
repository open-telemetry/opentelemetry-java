/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.common.AttributesKeys.booleanKey;
import static io.opentelemetry.common.AttributesKeys.doubleKey;
import static io.opentelemetry.common.AttributesKeys.longKey;
import static io.opentelemetry.common.AttributesKeys.stringKey;

import io.grpc.Context;
import io.opentelemetry.common.AttributeConsumer;
import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.internal.StringUtils;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.MonotonicClock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.Sampler.SamplingResult;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

/** {@link SpanBuilderSdk} is SDK implementation of {@link Span.Builder}. */
final class SpanBuilderSdk implements Span.Builder {
  private final String spanName;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;
  private final SpanProcessor spanProcessor;
  private final TraceConfig traceConfig;
  private final Resource resource;
  private final IdsGenerator idsGenerator;
  private final Clock clock;

  @Nullable private Context parent;
  private Kind spanKind = Kind.INTERNAL;
  @Nullable private AttributesMap attributes;
  @Nullable private List<io.opentelemetry.trace.Link> links;
  private int totalNumberOfLinksAdded = 0;
  private long startEpochNanos = 0;
  private boolean isRootSpan;

  SpanBuilderSdk(
      String spanName,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      SpanProcessor spanProcessor,
      TraceConfig traceConfig,
      Resource resource,
      IdsGenerator idsGenerator,
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
  public Span.Builder setParent(Context context) {
    Objects.requireNonNull(context, "context");
    this.isRootSpan = false;
    this.parent = context;
    return this;
  }

  @Override
  public Span.Builder setNoParent() {
    this.isRootSpan = true;
    this.parent = null;
    return this;
  }

  @Override
  public Span.Builder setSpanKind(Kind spanKind) {
    this.spanKind = Objects.requireNonNull(spanKind, "spanKind");
    return this;
  }

  @Override
  public Span.Builder addLink(Span span) {
    addLink(Link.create(span));
    return this;
  }

  @Override
  public Span.Builder addLink(Span span, Attributes attributes) {
    int totalAttributeCount = attributes.size();
    addLink(
        Link.create(
            span,
            RecordEventsReadableSpan.copyAndLimitAttributes(
                attributes, traceConfig.getMaxNumberOfAttributesPerLink()),
            totalAttributeCount));
    return this;
  }

  @Override
  public Span.Builder addLink(io.opentelemetry.trace.Link link) {
    Objects.requireNonNull(link, "link");
    totalNumberOfLinksAdded++;
    if (links == null) {
      links = new ArrayList<>(traceConfig.getMaxNumberOfLinks());
    }

    // don't bother doing anything with any links beyond the max.
    if (links.size() == traceConfig.getMaxNumberOfLinks()) {
      return this;
    }

    links.add(link);
    return this;
  }

  @Override
  public Span.Builder setAttribute(String key, String value) {
    return setAttribute(stringKey(key), value);
  }

  @Override
  public Span.Builder setAttribute(String key, long value) {
    return setAttribute(longKey(key), value);
  }

  @Override
  public Span.Builder setAttribute(String key, double value) {
    return setAttribute(doubleKey(key), value);
  }

  @Override
  public Span.Builder setAttribute(String key, boolean value) {
    return setAttribute(booleanKey(key), value);
  }

  @Override
  public <T> Span.Builder setAttribute(AttributeKey<T> key, T value) {
    Objects.requireNonNull(key, "key");
    if (value == null) {
      if (attributes != null) {
        attributes.remove(key);
      }
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
  public Span.Builder setStartTimestamp(long startTimestamp) {
    Utils.checkArgument(startTimestamp >= 0, "Negative startTimestamp");
    startEpochNanos = startTimestamp;
    return this;
  }

  @Override
  public Span startSpan() {
    final Context parentContext =
        isRootSpan ? Context.ROOT : parent == null ? Context.current() : parent;
    final Span parentSpan = TracingContextUtils.getSpan(parentContext);
    String traceId;
    String spanId = idsGenerator.generateSpanId();
    TraceState traceState = TraceState.getDefault();
    if (!parentSpan.isValid()) {
      // New root span.
      traceId = idsGenerator.generateTraceId();
    } else {
      // New child span.
      traceId = parentSpan.getTraceIdAsHexString();
      traceState = parentSpan.getTraceState();
    }
    List<io.opentelemetry.trace.Link> immutableLinks =
        links == null
            ? Collections.<io.opentelemetry.trace.Link>emptyList()
            : Collections.unmodifiableList(links);
    // Avoid any possibility to modify the links list by adding links to the Builder after the
    // startSpan is called. If that happens all the links will be added in a new list.
    links = null;
    ReadableAttributes immutableAttributes = attributes == null ? Attributes.empty() : attributes;
    SamplingResult samplingResult =
        traceConfig
            .getSampler()
            .shouldSample(
                parentSpan, traceId, spanName, spanKind, immutableAttributes, immutableLinks);
    Sampler.Decision samplingDecision = samplingResult.getDecision();

    if (!Samplers.isRecording(samplingDecision)) {
      return Span.getUnsampled(traceId, spanId, traceState);
    }
    ReadableAttributes samplingAttributes = samplingResult.getAttributes();
    if (!samplingAttributes.isEmpty()) {
      if (attributes == null) {
        attributes = new AttributesMap(traceConfig.getMaxNumberOfAttributes());
      }
      samplingAttributes.forEach(
          new AttributeConsumer() {
            @Override
            public <T> void consume(AttributeKey<T> key, T value) {
              attributes.put(key, value);
            }
          });
    }

    // Avoid any possibility to modify the attributes by adding attributes to the Builder after the
    // startSpan is called. If that happens all the attributes will be added in a new map.
    AttributesMap recordedAttributes = attributes;
    attributes = null;

    return RecordEventsReadableSpan.startSpan(
        traceId,
        spanId,
        TraceFlags.getSampled(),
        traceState,
        spanName,
        instrumentationLibraryInfo,
        spanKind,
        parentSpan.getSpanIdAsHexString(),
        parentSpan.isRemote(),
        traceConfig,
        spanProcessor,
        getClock(parentSpan, clock),
        resource,
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
}
