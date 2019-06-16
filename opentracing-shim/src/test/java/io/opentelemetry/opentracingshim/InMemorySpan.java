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

package io.opentelemetry.opentracingshim;

import io.opentelemetry.resources.Resource;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Sampler;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.SpanData.TimedEvent;
import io.opentelemetry.trace.SpanData.Timestamp;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceOptions;
import io.opentelemetry.trace.Tracestate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// TODO - Use sampler to create TraceOptions.
final class InMemorySpan implements Span {
  private String name;
  private final InMemoryTracer tracer;
  private final SpanContext context;
  private final SpanId parentSpanId;
  private final Resource resource;
  private final Kind kind;
  private final Timestamp startTimestamp;
  private final Map<String, AttributeValue> attributes = new HashMap<>();
  private final List<TimedEvent> timedEvents = new ArrayList<>();
  private final List<Link> links;
  private Status status = Status.OK;
  private boolean ended;

  InMemorySpan(
      InMemoryTracer tracer,
      SpanContext context,
      SpanId parentSpanId,
      Resource resource,
      String name,
      Kind kind,
      Timestamp startTimestamp,
      List<Link> links) {
    this.tracer = tracer;
    this.context = context;
    this.parentSpanId = parentSpanId;
    this.resource = resource;
    this.name = name;
    this.kind = kind;
    this.startTimestamp = startTimestamp;
    this.links = links;
  }

  @Override
  public void setAttribute(String key, String value) {
    setAttribute(key, AttributeValue.stringAttributeValue(value));
  }

  @Override
  public void setAttribute(String key, long value) {
    setAttribute(key, AttributeValue.longAttributeValue(value));
  }

  @Override
  public void setAttribute(String key, double value) {
    setAttribute(key, AttributeValue.doubleAttributeValue(value));
  }

  @Override
  public void setAttribute(String key, boolean value) {
    setAttribute(key, AttributeValue.booleanAttributeValue(value));
  }

  @Override
  public void setAttribute(String key, AttributeValue value) {
    synchronized (this) {
      endedCheck();
      attributes.put(key, value);
    }
  }

  @Override
  public void addEvent(String name) {
    addEvent(SpanData.Event.create(name));
  }

  @Override
  public void addEvent(String name, Map<String, AttributeValue> attributes) {
    addEvent(SpanData.Event.create(name, attributes));
  }

  @Override
  public void addEvent(Event event) {
    synchronized (this) {
      endedCheck();
      Timestamp tstamp = Timestamp.fromMillis(System.currentTimeMillis());
      timedEvents.add(TimedEvent.create(tstamp, event));
    }
  }

  @Override
  public void addLink(Link link) {
    synchronized (this) {
      endedCheck();
      links.add(link);
    }
  }

  @Override
  public void addLink(SpanContext spanContext) {
    synchronized (this) {
      endedCheck();
      links.add(SpanData.Link.create(spanContext));
    }
  }

  @Override
  public void addLink(SpanContext spanContext, Map<String, AttributeValue> attributes) {
    synchronized (this) {
      endedCheck();
      links.add(SpanData.Link.create(spanContext, attributes));
    }
  }

  @Override
  public void setStatus(Status status) {
    if (status == null) {
      throw new NullPointerException("status");
    }

    synchronized (this) {
      endedCheck();
      this.status = status;
    }
  }

  @Override
  public void updateName(String name) {
    synchronized (this) {
      endedCheck();
      this.name = name;
    }
  }

  @Override
  public void end() {
    synchronized (this) {
      endedCheck();
      tracer.recordSpanData(
          SpanData.create(
              context,
              parentSpanId,
              resource,
              name,
              kind,
              startTimestamp,
              attributes,
              timedEvents,
              links,
              status,
              Timestamp.fromMillis(System.currentTimeMillis())));
      ended = true;
    }
  }

  @Override
  public SpanContext getContext() {
    return context;
  }

  @Override
  public boolean isRecordingEvents() {
    return false;
  }

  private void endedCheck() {
    if (ended) {
      throw new IllegalStateException("Span is already ended.");
    }
  }

  // TODO - Use recordEvents and sampler.
  static final class Builder implements Span.Builder {
    final InMemoryTracer tracer;
    final String name;
    final List<Link> links = new ArrayList<>();
    SpanContext explicitParent;
    ParentType parentType = ParentType.CURRENT;
    Kind kind = Kind.INTERNAL;

    static final Random random = new Random();

    Builder(InMemoryTracer tracer, String name) {
      this.tracer = tracer;
      this.name = name;
    }

    @Override
    public Builder setParent(Span parent) {
      if (parent == null) {
        throw new NullPointerException("parent");
      }

      explicitParent = parent.getContext();
      parentType = ParentType.EXPLICIT;
      return this;
    }

    @Override
    public Builder setParent(SpanContext remoteParent) {
      if (remoteParent == null) {
        throw new NullPointerException("remoteParent");
      }

      explicitParent = remoteParent;
      parentType = ParentType.EXPLICIT;
      return this;
    }

    @Override
    public Builder setNoParent() {
      explicitParent = null;
      parentType = ParentType.NONE;
      return this;
    }

    @Override
    public Builder setSampler(Sampler sampler) {
      return this;
    }

    @Override
    public Builder addLink(SpanContext spanContext) {
      links.add(SpanData.Link.create(spanContext));
      return this;
    }

    @Override
    public Builder addLink(SpanContext spanContext, Map<String, AttributeValue> attributes) {
      links.add(SpanData.Link.create(spanContext, attributes));
      return this;
    }

    @Override
    public Builder addLink(Link link) {
      links.add(link);
      return this;
    }

    @Override
    public Builder setRecordEvents(boolean recordEvents) {
      return this;
    }

    @Override
    public Builder setSpanKind(Kind kind) {
      if (kind == null) { // TODO: or normalize to INTERNAL?
        throw new NullPointerException("kind");
      }

      this.kind = kind;
      return this;
    }

    @Override
    public Span startSpan() {
      SpanContext parentContext = null;

      switch (parentType) {
        case CURRENT:
          parentContext = tracer.getCurrentSpan().getContext();
          break;
        case NONE:
          parentContext = null;
          break;
        case EXPLICIT:
          parentContext = explicitParent;
          break;
      }

      TraceId traceId = null;
      SpanId parentSpanId = null;

      if (parentContext != null) {
        traceId = parentContext.getTraceId();
        parentSpanId = parentContext.getSpanId();
      }

      if (traceId == null || TraceId.getInvalid().equals(traceId)) {
        traceId = createTraceId();
        parentSpanId = null;
      }

      // TODO - Create properly the remaining SpanContext members.
      SpanContext context =
          SpanContext.create(
              traceId, createSpanId(), TraceOptions.getDefault(), Tracestate.getDefault());
      return new InMemorySpan(
          tracer,
          context,
          parentSpanId,
          tracer.getResource(),
          name,
          kind,
          Timestamp.fromMillis(System.currentTimeMillis()),
          links);
    }

    static SpanId createSpanId() {
      long id;
      do {
        id = random.nextLong();
      } while (id == 0);

      return new SpanId(id);
    }

    static TraceId createTraceId() {
      long idHi;
      long idLo;
      do {
        idHi = random.nextLong();
        idLo = random.nextLong();
      } while (idHi == 0 && idLo == 0);

      return new TraceId(idHi, idLo);
    }

    enum ParentType {
      CURRENT,
      EXPLICIT,
      NONE,
    }
  }
}
