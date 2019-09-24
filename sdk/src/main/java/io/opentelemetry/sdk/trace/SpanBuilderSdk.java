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
import io.opentelemetry.sdk.internal.Clock;
import io.opentelemetry.sdk.internal.TimestampConverter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Sampler;
import io.opentelemetry.trace.Sampler.Decision;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import io.opentelemetry.trace.unsafe.ContextUtils;
import io.opentelemetry.trace.util.Links;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;

/** {@link SpanBuilderSdk} is SDK implementation of {@link Span.Builder}. */
class SpanBuilderSdk implements Span.Builder {
  private static final long INVALID_ID = 0;

  private static final TraceFlags TRACE_OPTIONS_SAMPLED =
      TraceFlags.builder().setIsSampled(true).build();
  private static final TraceFlags TRACE_OPTIONS_NOT_SAMPLED =
      TraceFlags.builder().setIsSampled(false).build();

  private final String spanName;
  private final SpanProcessor spanProcessor;
  private final TraceConfig traceConfig;
  private final Resource resource;

  private final Clock clock;
  // TODO change with ThreadLocal version
  // https://github.com/open-telemetry/opentelemetry-java/issues/406
  private final Random random;

  @Nullable private Span parent;
  @Nullable private SpanContext remoteParent;
  private Kind spanKind = Kind.INTERNAL;
  private List<Link> links;
  private Sampler sampler;
  private ParentType parentType = ParentType.CURRENT_SPAN;
  private boolean recordEvents = false;

  SpanBuilderSdk(
      String spanName,
      SpanProcessor spanProcessor,
      TraceConfig traceConfig,
      Resource resource,
      Random random,
      Clock clock) {
    this.spanName = spanName;
    this.spanProcessor = spanProcessor;
    this.traceConfig = traceConfig;
    this.resource = resource;
    this.links = Collections.emptyList();
    this.sampler = traceConfig.getSampler();
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
    addLink(Links.create(spanContext));
    return this;
  }

  @Override
  public Span.Builder addLink(SpanContext spanContext, Map<String, AttributeValue> attributes) {
    Utils.checkNotNull(spanContext, "spanContext");
    Utils.checkNotNull(attributes, "attributes");
    addLink(Links.create(spanContext, attributes));
    return this;
  }

  @Override
  public Span.Builder addLink(Link link) {
    Utils.checkNotNull(link, "link");
    // This is the Collection.emptyList which is immutable.
    if (links.isEmpty()) {
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
    SpanContext parentContext = parent(parentType, parent, remoteParent);
    TraceId traceId;
    SpanId spanId = generateRandomSpanId(random);
    Tracestate tracestate = Tracestate.getDefault();
    if (parentContext == null || !parentContext.isValid()) {
      // New root span.
      traceId = generateRandomTraceId(random);
      // This is a root span so no remote or local parent.
      parentContext = null;
    } else {
      // New child span.
      traceId = parentContext.getTraceId();
      tracestate = parentContext.getTracestate();
    }
    Decision samplingDecision =
        sampler.shouldSample(parentContext, false, traceId, spanId, spanName, links);
    SpanContext spanContext =
        SpanContext.create(
            traceId,
            spanId,
            samplingDecision.isSampled() ? TRACE_OPTIONS_SAMPLED : TRACE_OPTIONS_NOT_SAMPLED,
            tracestate);

    if (!recordEvents && !samplingDecision.isSampled()) {
      return DefaultSpan.create(spanContext);
    }
    TimestampConverter timestampConverter = getTimestampConverter(parentSpan(parentType, parent));

    return RecordEventsReadableSpan.startSpan(
        spanContext,
        spanName,
        spanKind,
        parentContext != null ? parentContext.getSpanId() : null,
        traceConfig,
        spanProcessor,
        timestampConverter,
        clock,
        resource,
        samplingDecision.attributes(),
        truncatedLinks(),
        links.size());
  }

  private List<Link> truncatedLinks() {
    if (links.size() <= traceConfig.getMaxNumberOfLinks()) {
      return links;
    }
    return links.subList(links.size() - traceConfig.getMaxNumberOfLinks(), links.size());
  }

  @Nullable
  private static TimestampConverter getTimestampConverter(Span parent) {
    TimestampConverter timestampConverter = null;
    if (parent instanceof RecordEventsReadableSpan) {
      RecordEventsReadableSpan parentRecordEventsSpan = (RecordEventsReadableSpan) parent;
      timestampConverter = parentRecordEventsSpan.getTimestampConverter();
      parentRecordEventsSpan.addChild();
    }
    return timestampConverter;
  }

  @Nullable
  private static SpanContext parent(
      ParentType parentType, Span explicitParent, SpanContext remoteParent) {
    Span currentSpan = ContextUtils.getValue();
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

  @Nullable
  private static Span parentSpan(ParentType parentType, Span explicitParent) {
    switch (parentType) {
      case CURRENT_SPAN:
        return ContextUtils.getValue();
      case EXPLICIT_PARENT:
        return explicitParent;
      default:
        return null;
    }
  }

  /**
   * Generates a new random {@code SpanId}.
   *
   * @param random The random number generator.
   * @return a valid new {@code SpanId}.
   */
  static SpanId generateRandomSpanId(Random random) {
    long id;
    do {
      id = random.nextLong();
    } while (id == 0);
    return new SpanId(id);
  }

  /**
   * Generates a new random {@code TraceIde}.
   *
   * @param random The random number generator.
   * @return a valid new {@code TraceId}.
   */
  static TraceId generateRandomTraceId(Random random) {
    long idHi;
    long idLo;
    do {
      idHi = random.nextLong();
      idLo = random.nextLong();
    } while (idHi == INVALID_ID && idLo == INVALID_ID);
    return new TraceId(idHi, idLo);
  }

  private enum ParentType {
    CURRENT_SPAN,
    EXPLICIT_PARENT,
    EXPLICIT_REMOTE_PARENT,
    NO_PARENT,
  }
}
