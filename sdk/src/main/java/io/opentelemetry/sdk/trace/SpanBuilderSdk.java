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

import io.opentelemetry.internal.Utils;
import io.opentelemetry.resources.Resource;
import io.opentelemetry.sdk.internal.Clock;
import io.opentelemetry.sdk.internal.TimestampConverter;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Sampler;
import io.opentelemetry.trace.Sampler.Decision;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceOptions;
import io.opentelemetry.trace.Tracestate;
import io.opentelemetry.trace.unsafe.ContextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;

/** {@link SpanBuilderSdk} is SDK implementation of {@link Span.Builder}. */
@SuppressWarnings("unused") // TODO: finish implementation
class SpanBuilderSdk implements Span.Builder {
  private static final long INVALID_ID = 0;

  private final String spanName;
  private final SpanProcessor spanProcessor;
  private final TraceConfig traceConfig;
  private final Resource resource;

  private final Clock clock;
  private final Random random;

  @Nullable private Span parent;
  @Nullable private SpanContext remoteParent;
  private Kind spanKind = Kind.INTERNAL;
  @Nullable private List<Link> links;
  private Boolean recordEvents;
  private Sampler sampler;
  private ParentType parentType = ParentType.CURRENT_SPAN;

  SpanBuilderSdk(
      String spanName,
      SpanProcessor spanProcessor,
      TraceConfig traceConfig,
      Resource resource,
      Sampler sampler,
      Random random,
      Clock clock) {
    this.spanName = spanName;
    this.spanProcessor = spanProcessor;
    this.traceConfig = traceConfig;
    this.resource = resource;
    this.sampler = sampler;
    this.random = random;
    this.clock = clock;
  }

  @Override
  public Span.Builder setParent(Span parent) {
    this.parent = Utils.checkNotNull(parent, "parent");
    this.remoteParent = null;
    this.parentType = ParentType.EXPLICIT_PARENT;
    return this;
  }

  @Override
  public Span.Builder setParent(SpanContext remoteParent) {
    this.remoteParent = Utils.checkNotNull(remoteParent, "remoteParent");
    this.parent = null;
    this.parentType = ParentType.EXPLICIT_REMOTE_PARENT;
    return this;
  }

  @Override
  public Span.Builder setNoParent() {
    this.parentType = ParentType.NO_PARENT;
    this.parent = null;
    this.remoteParent = null;
    return this;
  }

  @Override
  public Span.Builder setSpanKind(Kind spanKind) {
    this.spanKind = Utils.checkNotNull(spanKind, "spanKind");
    return this;
  }

  @Override
  public Span.Builder setSampler(Sampler sampler) {
    this.sampler = Utils.checkNotNull(sampler, "sampler");
    return this;
  }

  @Override
  public Span.Builder addLink(SpanContext spanContext) {
    Utils.checkNotNull(spanContext, "spanContext");
    addLink(SpanData.Link.create(spanContext));
    return this;
  }

  @Override
  public Span.Builder addLink(SpanContext spanContext, Map<String, AttributeValue> attributes) {
    Utils.checkNotNull(spanContext, "spanContext");
    Utils.checkNotNull(attributes, "attributes");
    addLink(SpanData.Link.create(spanContext, attributes));
    return this;
  }

  @Override
  public Span.Builder addLink(Link link) {
    Utils.checkNotNull(link, "link");
    if (links == null) {
      links = new ArrayList<>();
    }
    links.add(link);
    return this;
  }

  @Override
  public Span.Builder setRecordEvents(boolean recordEvents) {
    this.recordEvents = recordEvents;
    return this;
  }

  @Override
  public Span startSpan() {
    Span currentSpan = null;
    if (parentType == ParentType.CURRENT_SPAN) {
      currentSpan = ContextUtils.getValue();
    }
    SpanContext parentContext = parent(parentType, currentSpan, parent, remoteParent);
    SpanContext spanContext = createSpanContext(spanName, parentContext, sampler);
    boolean recordEvents =
        this.recordEvents != null ? this.recordEvents : spanContext.getTraceOptions().isSampled();
    TimestampConverter timestampConverter = getTimestampConverter(parent);
    return RecordEventsReadableSpanImpl.startSpan(
        spanContext,
        spanName,
        spanKind,
        parentContext != null ? parentContext.getSpanId() : null,
        traceConfig,
        spanProcessor,
        timestampConverter,
        clock,
        resource,
        recordEvents);
  }

  private SpanContext createSpanContext(String name, SpanContext parentContext, Sampler sampler) {
    TraceId traceId;
    SpanId spanId = SpanId.generateRandomId(random);
    Tracestate tracestate = Tracestate.getDefault();
    Boolean hasRemoteParent = false;
    if (parentContext == null || !parentContext.isValid()) {
      // New root span.
      traceId = TraceId.generateRandomId(random);
      // This is a root span so no remote or local parent.
      hasRemoteParent = null;
    } else {
      // New child span.
      traceId = parentContext.getTraceId();
      tracestate = parentContext.getTracestate();
    }
    Decision samplingDecision =
        sampler.shouldSample(
            parentContext,
            hasRemoteParent,
            traceId,
            spanId,
            name,
            // TODO links in span builder contain only context
            Collections.<Span>emptyList());
    return SpanContext.create(
        traceId,
        spanId,
        TraceOptions.builder().setIsSampled(samplingDecision.isSampled()).build(),
        tracestate);
  }

  @Nullable
  private static TimestampConverter getTimestampConverter(Span parent) {
    TimestampConverter timestampConverter = null;
    if (parent instanceof RecordEventsReadableSpanImpl) {
      RecordEventsReadableSpanImpl parentRecordEventsSpan = (RecordEventsReadableSpanImpl) parent;
      timestampConverter = parentRecordEventsSpan.getTimestampConverter();
      parentRecordEventsSpan.addChild();
    }
    return timestampConverter;
  }

  @Nullable
  private static SpanContext parent(
      ParentType parentType, Span currentSpan, Span explicitParent, SpanContext remoteParent) {
    switch (parentType) {
      case NO_PARENT:
        return null;
      case CURRENT_SPAN:
        return currentSpan != null ? currentSpan.getContext() : null;
      case EXPLICIT_PARENT:
        return explicitParent.getContext();
      case EXPLICIT_REMOTE_PARENT:
        return remoteParent;
    }
    throw new IllegalStateException("Unknown parent type");
  }

  private enum ParentType {
    CURRENT_SPAN,
    EXPLICIT_PARENT,
    EXPLICIT_REMOTE_PARENT,
    NO_PARENT,
  }
}
