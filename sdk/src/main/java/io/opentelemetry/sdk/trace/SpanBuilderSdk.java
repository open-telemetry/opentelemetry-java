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
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Sampler;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceOptions;
import io.opentelemetry.trace.Tracestate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** {@link SpanBuilderSdk} is SDK implementation of {@link Span.Builder}. */
@SuppressWarnings("unused") // TODO: finish implementation
class SpanBuilderSdk implements Span.Builder {
  private final String spanName;
  private final SpanProcessor spanProcessor;
  private final TraceConfig traceConfig;

  @Nullable private Span parent;
  @Nullable private SpanContext remoteParent;
  private Kind spanKind = Kind.INTERNAL;
  @Nullable private List<Link> links;
  private boolean recordEvents;
  private Sampler sampler;
  private ParentType parentType = ParentType.CURRENT_SPAN;

  SpanBuilderSdk(String spanName, SpanProcessor spanProcessor, TraceConfig traceConfig) {
    this.spanName = spanName;
    this.spanProcessor = spanProcessor;
    this.traceConfig = traceConfig;
  }

  @Override
  public Span.Builder setParent(Span parent) {
    this.parent = Utils.checkNotNull(parent, "parent");
    this.parent = parent;
    this.parentType = ParentType.EXPLICIT_REMOTE_PARENT;
    return this;
  }

  @Override
  public Span.Builder setParent(SpanContext remoteParent) {
    this.remoteParent = Utils.checkNotNull(remoteParent, "remoteParent");
    this.parentType = ParentType.EXPLICIT_REMOTE_PARENT;
    return this;
  }

  @Override
  public Span.Builder setNoParent() {
    this.parentType = ParentType.NO_PARENT;
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
    // TODO: get remoteParent span from the context if noParent=false and remoteParent/remoteParents
    // are null
    // TODO: correctly implement this.
    SpanContext context =
        SpanContext.create(
            TraceId.fromBytes(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 8, 7, 6, 5, 4, 3, 2, 1}, 0),
            SpanId.fromBytes(new byte[] {1, 2, 3, 4, 5, 6, 7, 8}, 0),
            TraceOptions.builder().setIsSampled(true).build(),
            Tracestate.getDefault());
    return RecordEventsReadableSpanImpl.startSpan(
        context,
        spanName,
        spanKind,
        null,
        traceConfig,
        spanProcessor,
        null,
        MillisClock.getInstance(),
        Resource.getEmpty());
  }

  private enum ParentType {
    CURRENT_SPAN,
    EXPLICIT_PARENT,
    EXPLICIT_REMOTE_PARENT,
    NO_PARENT,
  }
}
