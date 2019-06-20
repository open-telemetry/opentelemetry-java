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
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Sampler;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** {@link SpanBuilderSdk} is SDK implementation of {@link Span.Builder}. */
@SuppressWarnings("unused") // TODO: finish implementation
class SpanBuilderSdk implements Span.Builder {

  @Nullable private SpanContext parent;
  private Kind spanKind;
  @Nullable private List<Link> links;
  private boolean recordEvents;
  private Sampler sampler;
  private final String spanName;
  private ParentType parentType = ParentType.CURRENT_SPAN;

  SpanBuilderSdk(String spanName) {
    this.spanName = spanName;
  }

  @Override
  public Span.Builder setParent(Span parent) {
    Utils.checkNotNull(parent, "parent");
    this.setParent(parent.getContext());
    return this;
  }

  @Override
  public Span.Builder setParent(SpanContext remoteParent) {
    this.parent = Utils.checkNotNull(remoteParent, "remoteParent");
    this.parentType = ParentType.EXPLICIT_PARENT;
    return this;
  }

  @Override
  public Span.Builder setNoParent() {
    this.parentType = ParentType.NO_PARENT;
    this.parent = null;
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
    // TODO get parent span from the context if noParent=false and parent/remoteParents are null
    throw new UnsupportedOperationException("to be implemented");
  }

  private enum ParentType {
    CURRENT_SPAN,
    EXPLICIT_PARENT,
    NO_PARENT,
  }
}
