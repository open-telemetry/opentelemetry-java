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

import static io.opentelemetry.common.AttributeValue.Type.STRING;

import com.google.common.base.Preconditions;
import com.google.common.collect.EvictingQueue;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.EndSpanOptions;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Tracer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/** Implementation for the {@link Span} class that records trace events. */
@ThreadSafe
final class RecordEventsReadableSpan implements ReadableSpan, Span {

  private static final Logger logger = Logger.getLogger(Tracer.class.getName());

  @SuppressWarnings("checkstyle:LineLength")
  private static final String MAX_SPAN_ATTRIBUTE_COUNT_LOG_MESSAGE =
      "Span with name '%s' has reached the maximum number of attributes (%d). Dropping attribute with key '%s'";

  private static final String MAX_LINK_ATTRIBUTE_COUNT_LOG_MESSAGE =
      "Link has reached the maximum number of attributes (%d). Dropping %d attributes.";

  // Contains the identifiers associated with this Span.
  private final SpanContext context;
  // The parent SpanId of this span. Invalid if this is a root span.
  private final SpanId parentSpanId;
  // True if the parent is on a different process.
  private final boolean hasRemoteParent;
  // Handler called when the span starts and ends.
  private final SpanProcessor spanProcessor;
  // The displayed name of the span.
  // List of recorded links to parent and child spans.
  private final List<Link> links;
  // Number of links recorded.
  private final int totalRecordedLinks;
  // Max number of attibutes per span.
  private final int maxNumberOfAttributes;
  // Max number of attributes per event.
  private final int maxNumberOfAttributesPerEvent;

  // Lock used to internally guard the mutable state of this instance
  private final Object lock = new Object();

  @GuardedBy("lock")
  private String name;
  // The kind of the span.
  private final Kind kind;
  // The clock used to get the time.
  private final Clock clock;
  // The resource associated with this span.
  private final Resource resource;
  // instrumentation library of the named tracer which created this span
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;
  // The start time of the span.
  private final long startEpochNanos;
  // Set of recorded attributes. DO NOT CALL any other method that changes the ordering of events.
  @GuardedBy("lock")
  private final AttributesWithCapacity attributes;
  // List of recorded events.
  @GuardedBy("lock")
  private final EvictingQueue<TimedEvent> events;
  // Number of attributes recorded.
  @GuardedBy("lock")
  private int totalAttributeCount = 0;
  // Number of events recorded.
  @GuardedBy("lock")
  private int totalRecordedEvents = 0;
  // The status of the span.
  @GuardedBy("lock")
  @Nullable
  private Status status;
  // The end time of the span.
  @GuardedBy("lock")
  private long endEpochNanos;
  // True if the span is ended.
  @GuardedBy("lock")
  private boolean hasEnded;

  /**
   * Creates and starts a span with the given configuration.
   *
   * @param context supplies the trace_id and span_id for the newly started span.
   * @param name the displayed name for the new span.
   * @param kind the span kind.
   * @param parentSpanId the span_id of the parent span, or null if the new span is a root span.
   * @param hasRemoteParent {@code true} if the parentContext is remote. {@code false} if this is a
   *     root span.
   * @param traceConfig trace parameters like sampler and probability.
   * @param spanProcessor handler called when the span starts and ends.
   * @param clock the clock used to get the time.
   * @param resource the resource associated with this span.
   * @param attributes the attributes set during span creation.
   * @param links the links set during span creation, may be truncated.
   * @return a new and started span.
   */
  static RecordEventsReadableSpan startSpan(
      SpanContext context,
      String name,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      Kind kind,
      @Nullable SpanId parentSpanId,
      boolean hasRemoteParent,
      TraceConfig traceConfig,
      SpanProcessor spanProcessor,
      Clock clock,
      Resource resource,
      AttributesWithCapacity attributes,
      List<Link> links,
      int totalRecordedLinks,
      long startEpochNanos) {
    RecordEventsReadableSpan span =
        new RecordEventsReadableSpan(
            context,
            name,
            instrumentationLibraryInfo,
            kind,
            parentSpanId == null ? SpanId.getInvalid() : parentSpanId,
            hasRemoteParent,
            traceConfig,
            spanProcessor,
            clock,
            resource,
            attributes,
            links,
            totalRecordedLinks,
            startEpochNanos == 0 ? clock.now() : startEpochNanos);
    // Call onStart here instead of calling in the constructor to make sure the span is completely
    // initialized.
    spanProcessor.onStart(span);
    return span;
  }

  @Override
  public SpanData toSpanData() {
    // Copy immutable fields outside synchronized block.
    SpanContext spanContext = getSpanContext();
    SpanData.Builder builder =
        SpanData.newBuilder()
            .setName(getName())
            .setInstrumentationLibraryInfo(instrumentationLibraryInfo)
            .setTraceId(spanContext.getTraceId())
            .setSpanId(spanContext.getSpanId())
            .setTraceFlags(spanContext.getTraceFlags())
            .setLinks(getLinks())
            .setTotalRecordedLinks(totalRecordedLinks)
            .setKind(kind)
            .setTraceState(spanContext.getTraceState())
            .setParentSpanId(parentSpanId)
            .setHasRemoteParent(hasRemoteParent)
            .setResource(resource)
            .setStartEpochNanos(startEpochNanos);

    // Copy remainder within synchronized
    synchronized (lock) {
      return builder
          .setHasEnded(hasEnded)
          .setAttributes(attributes)
          .setEndEpochNanos(getEndEpochNanos())
          .setStatus(getStatusWithDefault())
          .setTimedEvents(adaptTimedEvents())
          .setTotalAttributeCount(totalAttributeCount)
          .setTotalRecordedEvents(totalRecordedEvents)
          // build() does the actual copying of the collections: it needs to be synchronized
          // because of the attributes and events collections.
          .build();
    }
  }

  @GuardedBy("lock")
  private List<SpanData.TimedEvent> adaptTimedEvents() {
    List<SpanData.TimedEvent> result = new ArrayList<>(events.size());
    for (io.opentelemetry.sdk.trace.TimedEvent sourceEvent : events) {
      result.add(
          SpanData.TimedEvent.create(
              sourceEvent.getEpochNanos(), sourceEvent.getName(), sourceEvent.getAttributes()));
    }
    return result;
  }

  @Override
  public boolean hasEnded() {
    synchronized (lock) {
      return hasEnded;
    }
  }

  @Override
  public SpanContext getSpanContext() {
    return getContext();
  }

  /**
   * Returns the name of the {@code Span}.
   *
   * @return the name of the {@code Span}.
   */
  @Override
  public String getName() {
    synchronized (lock) {
      return name;
    }
  }

  /**
   * Returns the instrumentation library specified when creating the tracer which produced this
   * span.
   *
   * @return an instance of {@link InstrumentationLibraryInfo} describing the instrumentation
   *     library
   */
  @Override
  public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return instrumentationLibraryInfo;
  }

  /**
   * Returns the end nano time (see {@link System#nanoTime()}) or zero if the current {@code Span}
   * is not ended.
   *
   * @return the end nano time.
   */
  private long getEndEpochNanos() {
    synchronized (lock) {
      return endEpochNanos;
    }
  }

  /**
   * Returns a copy of the links for this span.
   *
   * @return A copy of the Links for this span.
   */
  private List<SpanData.Link> getLinks() {
    if (links == null) {
      return Collections.emptyList();
    }
    List<SpanData.Link> result = new ArrayList<>(links.size());
    for (Link link : links) {
      SpanData.Link newLink;
      if (!(link instanceof SpanData.Link)) {
        // Make a copy because the given Link may not be immutable and we may reference a lot of
        // memory.
        newLink = SpanData.Link.create(link.getContext(), link.getAttributes());
      } else {
        newLink = (SpanData.Link) link;
      }
      result.add(newLink);
    }
    return Collections.unmodifiableList(result);
  }

  /**
   * Returns the latency of the {@code Span} in nanos. If still active then returns now() - start
   * time.
   *
   * @return the latency of the {@code Span} in nanos.
   */
  @Override
  public long getLatencyNanos() {
    synchronized (lock) {
      return (hasEnded ? endEpochNanos : clock.now()) - startEpochNanos;
    }
  }

  /**
   * Returns the {@code Clock} used by this {@code Span}.
   *
   * @return the {@code Clock} used by this {@code Span}.
   */
  Clock getClock() {
    return clock;
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
    Preconditions.checkNotNull(key, "key");
    if (value == null || (value.getType().equals(STRING) && value.getStringValue() == null)) {
      synchronized (lock) {
        attributes.remove(key);
      }
      return;
    }
    synchronized (lock) {
      if (hasEnded) {
        logger.log(Level.FINE, "Calling setAttribute() on an ended Span.");
        return;
      }
      totalAttributeCount++;
      if (attributes.get(key) == null && attributes.size() >= maxNumberOfAttributes) {
        logger.log(
            Level.FINE,
            String.format(
                MAX_SPAN_ATTRIBUTE_COUNT_LOG_MESSAGE, this.name, maxNumberOfAttributes, key));
        return;
      }
      attributes.putAttribute(key, value);
    }
  }

  @Override
  public void addEvent(String name) {
    addTimedEvent(TimedEvent.create(clock.now(), name));
  }

  @Override
  public void addEvent(String name, long timestamp) {
    addTimedEvent(TimedEvent.create(timestamp, name));
  }

  @Override
  public void addEvent(String name, Map<String, AttributeValue> attributes) {
    int totalAttributeCount = attributes.size();
    addTimedEvent(
        TimedEvent.create(
            clock.now(), name, limitEventAttributes(attributes), totalAttributeCount));
  }

  @Override
  public void addEvent(String name, Map<String, AttributeValue> attributes, long timestamp) {
    int totalAttributeCount = attributes.size();
    addTimedEvent(
        TimedEvent.create(timestamp, name, limitEventAttributes(attributes), totalAttributeCount));
  }

  @Override
  public void addEvent(Event event) {
    addTimedEvent(TimedEvent.create(clock.now(), event));
  }

  @Override
  public void addEvent(Event event, long timestamp) {
    addTimedEvent(TimedEvent.create(timestamp, event));
  }

  private Map<String, AttributeValue> limitEventAttributes(Map<String, AttributeValue> attributes) {
    if (attributes.size() <= this.maxNumberOfAttributesPerEvent) {
      return attributes;
    }

    Map<String, AttributeValue> temp = new HashMap<String, AttributeValue>();
    for (Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
      if (temp.size() < this.maxNumberOfAttributesPerEvent) {
        temp.put(entry.getKey(), entry.getValue());
      }
    }
    logger.log(
        Level.FINE,
        String.format(
            MAX_LINK_ATTRIBUTE_COUNT_LOG_MESSAGE,
            maxNumberOfAttributesPerEvent,
            attributes.size() - temp.size()));
    return temp;
  }

  private void addTimedEvent(TimedEvent timedEvent) {
    synchronized (lock) {
      if (hasEnded) {
        logger.log(Level.FINE, "Calling addEvent() on an ended Span.");
        return;
      }
      events.add(timedEvent);
      totalRecordedEvents++;
    }
  }

  @Override
  public void setStatus(Status status) {
    Preconditions.checkNotNull(status, "status");
    synchronized (lock) {
      if (hasEnded) {
        logger.log(Level.FINE, "Calling setStatus() on an ended Span.");
        return;
      }
      this.status = status;
    }
  }

  @Override
  public void updateName(String name) {
    Preconditions.checkNotNull(name, "name");
    synchronized (lock) {
      if (hasEnded) {
        logger.log(Level.FINE, "Calling updateName() on an ended Span.");
        return;
      }
      this.name = name;
    }
  }

  @Override
  public void end() {
    endInternal(clock.now());
  }

  @Override
  public void end(EndSpanOptions endOptions) {
    Preconditions.checkNotNull(endOptions, "endOptions");
    endInternal(endOptions.getEndTimestamp() == 0 ? clock.now() : endOptions.getEndTimestamp());
  }

  private void endInternal(long endEpochNanos) {
    synchronized (lock) {
      if (hasEnded) {
        logger.log(Level.FINE, "Calling end() on an ended Span.");
        return;
      }
      this.endEpochNanos = endEpochNanos;
      hasEnded = true;
    }
    spanProcessor.onEnd(this);
  }

  @Override
  public SpanContext getContext() {
    return context;
  }

  @Override
  public boolean isRecording() {
    return true;
  }

  @GuardedBy("lock")
  private Status getStatusWithDefault() {
    synchronized (lock) {
      return status == null ? Status.OK : status;
    }
  }

  private RecordEventsReadableSpan(
      SpanContext context,
      String name,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      Kind kind,
      SpanId parentSpanId,
      boolean hasRemoteParent,
      TraceConfig traceConfig,
      SpanProcessor spanProcessor,
      Clock clock,
      Resource resource,
      AttributesWithCapacity attributes,
      List<Link> links,
      int totalRecordedLinks,
      long startEpochNanos) {
    this.context = context;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
    this.parentSpanId = parentSpanId;
    this.hasRemoteParent = hasRemoteParent;
    this.links = links;
    this.totalRecordedLinks = totalRecordedLinks;
    this.name = name;
    this.kind = kind;
    this.spanProcessor = spanProcessor;
    this.resource = resource;
    this.hasEnded = false;
    this.clock = clock;
    this.startEpochNanos = startEpochNanos;
    this.attributes = attributes;
    this.events = EvictingQueue.create(traceConfig.getMaxNumberOfEvents());
    this.maxNumberOfAttributes = traceConfig.getMaxNumberOfAttributes();
    this.maxNumberOfAttributesPerEvent = traceConfig.getMaxNumberOfAttributesPerEvent();
  }
}
