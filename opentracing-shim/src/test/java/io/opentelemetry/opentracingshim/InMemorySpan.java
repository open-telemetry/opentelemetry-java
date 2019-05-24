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

import io.opentelemetry.resource.Resource;
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
import io.opentelemetry.trace.Tracer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// TODO - Use sampler to create TraceOptions.
final class InMemorySpan implements Span {
  private String name;
  private final Tracer tracer;
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
      Tracer tracer,
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
    final Tracer tracer;
    final String name;
    final List<Link> links = new ArrayList<>();
    SpanContext parentContext;
    boolean isRootSpan;
    Kind kind = Kind.INTERNAL;

    static final Random random = new Random();

    Builder(Tracer tracer, String name) {
      this.tracer = tracer;
      this.name = name;
    }

    @Override
    public Builder setParent(Span parent) {
      parentContext = parent.getContext();
      return this;
    }

    @Override
    public Builder setParent(SpanContext remoteParent) {
      parentContext = remoteParent;
      return this;
    }

    @Override
    public Builder setNoParent() {
      isRootSpan = true;
      return this;
    }

    @Override
    public Builder setSampler(Sampler sampler) {
      return this;
    }

    @Override
    public Builder addLink(Link link) {
      links.add(link);
      return this;
    }

    @Override
    public Builder addLinks(List<Link> links) {
      this.links.addAll(links);
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
      if (parentContext == null && !isRootSpan) {
        parentContext = tracer.getCurrentSpan().getContext();
      }

      TraceId traceId = null;
      SpanId parentSpanId = null;

      if (parentContext != null) {
        traceId = parentContext.getTraceId();
        parentSpanId = parentContext.getSpanId();
      }

      if (traceId == null || TraceId.INVALID.equals(traceId)) {
        traceId = createTraceId();
        parentSpanId = null;
      }

      // TODO - Create properly the remaining SpanContext members.
      SpanContext context = SpanContext.create(traceId, createSpanId(), null, null);
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
      SpanId spanId;
      do {
        ByteBuffer buff = ByteBuffer.allocate(SpanId.SIZE);
        spanId = SpanId.fromBytes(buff.putLong(random.nextLong()).array(), 0);
      } while (spanId.equals(SpanId.INVALID));

      return spanId;
    }

    static TraceId createTraceId() {
      TraceId traceId;
      do {
        ByteBuffer buff = ByteBuffer.allocate(TraceId.SIZE);
        traceId =
            TraceId.fromBytes(
                buff.putLong(random.nextLong()).putLong(random.nextLong()).array(), 0);
      } while (traceId.equals(TraceId.INVALID));

      return traceId;
    }
  }
}
