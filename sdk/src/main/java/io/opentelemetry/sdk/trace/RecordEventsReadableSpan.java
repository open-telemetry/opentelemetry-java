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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.EvictingQueue;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.AttributeValue;
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
import java.util.LinkedHashMap;
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

  // Contains the identifiers associated with this Span.
  private final SpanContext context;
  // The parent SpanId of this span. Invalid if this is a root span.
  private final SpanId parentSpanId;
  // Handler called when the span starts and ends.
  private final SpanProcessor spanProcessor;
  // The displayed name of the span.
  // List of recorded links to parent and child spans.
  private final List<Link> links;
  // Number of links recorded.
  private final int totalRecordedLinks;

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
  // The start time of the span.
  private final long startEpochNanos;
  // Set of recorded attributes. DO NOT CALL any other method that changes the ordering of events.
  @GuardedBy("lock")
  private final AttributesWithCapacity attributes;
  // List of recorded events.
  @GuardedBy("lock")
  private final EvictingQueue<TimedEvent> events;
  // Number of events recorded.
  @GuardedBy("lock")
  private int totalRecordedEvents = 0;
  // The number of children.
  @GuardedBy("lock")
  private int numberOfChildren;
  // The status of the span.
  @GuardedBy("lock")
  @Nullable
  private Status status;
  // The end time of the span.
  @GuardedBy("lock")
  private long endEpochNanos;
  // True if the span is ended.
  @GuardedBy("lock")
  private boolean hasBeenEnded;

  /**
   * Creates and starts a span with the given configuration.
   *
   * @param context supplies the trace_id and span_id for the newly started span.
   * @param name the displayed name for the new span.
   * @param kind the span kind.
   * @param parentSpanId the span_id of the parent span, or null if the new span is a root span.
   * @param traceConfig trace parameters like sampler and probability.
   * @param spanProcessor handler called when the span starts and ends.
   * @param clock the clock used to get the time.
   * @param resource the resource associated with this span.
   * @param attributes the attributes set during span creation.
   * @param links the links set during span creation, may be truncated.
   * @param totalRecordedLinks the total number of links set (including dropped links).
   * @return a new and started span.
   */
  @VisibleForTesting
  static RecordEventsReadableSpan startSpan(
      SpanContext context,
      String name,
      Kind kind,
      @Nullable SpanId parentSpanId,
      TraceConfig traceConfig,
      SpanProcessor spanProcessor,
      Clock clock,
      Resource resource,
      Map<String, AttributeValue> attributes,
      List<Link> links,
      int totalRecordedLinks,
      long startEpochNanos) {
    RecordEventsReadableSpan span =
        new RecordEventsReadableSpan(
            context,
            name,
            kind,
            parentSpanId == null ? SpanId.getInvalid() : parentSpanId,
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
    SpanContext spanContext = getSpanContext();
    return SpanData.newBuilder()
        .setName(getName())
        .setTraceId(spanContext.getTraceId())
        .setSpanId(spanContext.getSpanId())
        .setTraceFlags(spanContext.getTraceFlags())
        .setTracestate(spanContext.getTracestate())
        .setAttributes(getAttributes())
        .setStartEpochNanos(startEpochNanos)
        .setEndEpochNanos(getEndEpochNanos())
        .setKind(kind)
        .setLinks(getLinks())
        .setParentSpanId(parentSpanId)
        .setResource(resource)
        .setStatus(getStatus())
        .setTimedEvents(adaptTimedEvents())
        .build();
  }

  private List<SpanData.TimedEvent> adaptTimedEvents() {
    List<io.opentelemetry.sdk.trace.TimedEvent> sourceEvents = getTimedEvents();
    List<SpanData.TimedEvent> result = new ArrayList<>(sourceEvents.size());
    for (io.opentelemetry.sdk.trace.TimedEvent sourceEvent : sourceEvents) {
      result.add(
          SpanData.TimedEvent.create(
              sourceEvent.getEpochNanos(), sourceEvent.getName(), sourceEvent.getAttributes()));
    }
    return result;
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
   * Returns the end nano time (see {@link System#nanoTime()}). If the current {@code Span} is not
   * ended then returns {@link Clock#nanoTime()}.
   *
   * @return the end nano time.
   */
  private long getEndEpochNanos() {
    synchronized (lock) {
      return getEndNanoTimeInternal();
    }
  }

  /**
   * Returns the status of the {@code Span}. If not set defaults to {@link Status#OK}.
   *
   * @return the status of the {@code Span}.
   */
  @VisibleForTesting
  Status getStatus() {
    synchronized (lock) {
      return getStatusWithDefault();
    }
  }

  /**
   * Returns a copy of the timed events for this span.
   *
   * @return The TimedEvents for this span.
   */
  private List<TimedEvent> getTimedEvents() {
    synchronized (lock) {
      return new ArrayList<>(events);
    }
  }

  /**
   * Returns a copy of the links for this span.
   *
   * @return A copy of the Links for this span.
   */
  @VisibleForTesting
  List<Link> getLinks() {
    synchronized (lock) {
      if (links == null) {
        return Collections.emptyList();
      }
      List<Link> result = new ArrayList<>(links.size());
      for (Link link : links) {
        Link newLink = link;
        if (!(link instanceof SpanData.Link)) {
          // Make a copy because the given Link may not be immutable and we may reference a lot of
          // memory.
          newLink = SpanData.Link.create(link.getContext(), link.getAttributes());
        }
        result.add(newLink);
      }
      return Collections.unmodifiableList(result);
    }
  }

  /**
   * Returns an unmodifiable view of the attributes associated with this span.
   *
   * @return An unmodifiable view of the attributes associated wit this span
   */
  @VisibleForTesting
  Map<String, AttributeValue> getAttributes() {
    synchronized (lock) {
      return Collections.unmodifiableMap(attributes);
    }
  }

  /**
   * Returns the latency of the {@code Span} in nanos. If still active then returns now() - start
   * time.
   *
   * @return the latency of the {@code Span} in nanos.
   */
  long getLatencyNs() {
    synchronized (lock) {
      return getEndNanoTimeInternal() - startEpochNanos;
    }
  }

  // Use getEndNanoTimeInternal to avoid over-locking.
  @GuardedBy("lock")
  private long getEndNanoTimeInternal() {
    return hasBeenEnded ? endEpochNanos : clock.now();
  }

  /**
   * Returns the kind of this {@code Span}.
   *
   * @return the kind of this {@code Span}.
   */
  public Kind getKind() {
    return kind;
  }

  /**
   * Returns the span id of this span's parent span.
   *
   * @return The span id of the parent span.
   */
  @VisibleForTesting
  public SpanId getParentSpanId() {
    return parentSpanId;
  }

  /**
   * Returns the {@code Clock} used by this {@code Span}.
   *
   * @return the {@code Clock} used by this {@code Span}.
   */
  @VisibleForTesting
  public Clock getClock() {
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
    Preconditions.checkNotNull(value, "value");
    synchronized (lock) {
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling setAttribute() on an ended Span.");
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
    addTimedEvent(TimedEvent.create(clock.now(), name, attributes));
  }

  @Override
  public void addEvent(String name, Map<String, AttributeValue> attributes, long timestamp) {
    addTimedEvent(TimedEvent.create(timestamp, name, attributes));
  }

  @Override
  public void addEvent(Event event) {
    addTimedEvent(TimedEvent.create(clock.now(), event));
  }

  @Override
  public void addEvent(Event event, long timestamp) {
    addTimedEvent(TimedEvent.create(timestamp, event));
  }

  private void addTimedEvent(TimedEvent timedEvent) {
    synchronized (lock) {
      if (hasBeenEnded) {
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
      if (hasBeenEnded) {
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
      if (hasBeenEnded) {
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
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling end() on an ended Span.");
        return;
      }
      this.endEpochNanos = endEpochNanos;
      hasBeenEnded = true;
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

  void addChild() {
    synchronized (lock) {
      if (hasBeenEnded) {
        logger.log(Level.FINE, "Calling end() on an ended Span.");
        return;
      }
      numberOfChildren++;
    }
  }

  @GuardedBy("lock")
  private Status getStatusWithDefault() {
    synchronized (lock) {
      return status == null ? Status.OK : status;
    }
  }

  // A map implementation with a fixed capacity that drops events when the map gets full. Eviction
  // is based on the access order.
  static final class AttributesWithCapacity extends LinkedHashMap<String, AttributeValue> {

    private final long capacity;
    private int totalRecordedAttributes = 0;
    // Here because -Werror complains about this: [serial] serializable class AttributesWithCapacity
    // has no definition of serialVersionUID. This class shouldn't be serialized.
    private static final long serialVersionUID = 42L;

    private AttributesWithCapacity(long capacity) {
      // Capacity of the map is capacity + 1 to avoid resizing because removeEldestEntry is invoked
      // by put and putAll after inserting a new entry into the map. The loadFactor is set to 1
      // to avoid resizing because. The accessOrder is set to true.
      super((int) capacity + 1, 1, /*accessOrder=*/ true);
      this.capacity = capacity;
    }

    // Users must call this method instead of put to keep count of the total number of entries
    // inserted.
    private void putAttribute(String key, AttributeValue value) {
      totalRecordedAttributes += 1;
      put(key, value);
    }

    int getNumberOfDroppedAttributes() {
      return totalRecordedAttributes - size();
    }

    // It is called after each put or putAll call in order to determine if the eldest inserted
    // entry should be removed or not.
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, AttributeValue> eldest) {
      return size() > this.capacity;
    }
  }

  private RecordEventsReadableSpan(
      SpanContext context,
      String name,
      Kind kind,
      SpanId parentSpanId,
      TraceConfig traceConfig,
      SpanProcessor spanProcessor,
      Clock clock,
      Resource resource,
      Map<String, AttributeValue> attributes,
      List<Link> links,
      int totalRecordedLinks,
      long startEpochNanos) {
    this.context = context;
    this.parentSpanId = parentSpanId;
    this.links = links;
    this.totalRecordedLinks = totalRecordedLinks;
    this.name = name;
    this.kind = kind;
    this.spanProcessor = spanProcessor;
    this.resource = resource;
    this.hasBeenEnded = false;
    this.numberOfChildren = 0;
    this.clock = clock;
    this.startEpochNanos = startEpochNanos;
    this.attributes = new AttributesWithCapacity(traceConfig.getMaxNumberOfAttributes());
    this.attributes.putAll(attributes);
    this.events = EvictingQueue.create(traceConfig.getMaxNumberOfEvents());
  }

  @SuppressWarnings("NoFinalizer")
  @Override
  protected void finalize() throws Throwable {
    synchronized (lock) {
      if (!hasBeenEnded) {
        logger.log(Level.SEVERE, "Span " + name + " is GC'ed without being ended.");
      }
    }
    super.finalize();
  }

  /**
   * The count of links that have been dropped.
   *
   * @return The number of links that have been dropped.
   */
  @VisibleForTesting
  int getDroppedLinksCount() {
    return totalRecordedLinks - links.size();
  }

  @VisibleForTesting
  int getNumberOfChildren() {
    synchronized (lock) {
      return numberOfChildren;
    }
  }

  @VisibleForTesting
  int getTotalRecordedEvents() {
    synchronized (lock) {
      return totalRecordedEvents;
    }
  }
}
