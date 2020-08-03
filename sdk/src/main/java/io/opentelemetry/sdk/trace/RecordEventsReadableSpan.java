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

import com.google.common.collect.EvictingQueue;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.common.ReadableKeyValuePairs.KeyValueConsumer;
import io.opentelemetry.internal.StringUtils;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.TimedEvent.RawTimedEventWithEvent;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.trace.EndSpanOptions;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.attributes.SemanticAttributes;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
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

  // The config used when constructing this Span.
  private final TraceConfig traceConfig;
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
  private final List<io.opentelemetry.trace.Link> links;
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
  // instrumentation library of the named tracer which created this span
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;
  // The start time of the span.
  private final long startEpochNanos;
  // Set of recorded attributes. DO NOT CALL any other method that changes the ordering of events.
  @GuardedBy("lock")
  @Nullable
  private AttributesMap attributes;
  // List of recorded events.
  @GuardedBy("lock")
  private final EvictingQueue<TimedEvent> events;
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
      @Nullable AttributesMap attributes,
      List<io.opentelemetry.trace.Link> links,
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
    this.traceConfig = traceConfig;
  }

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
      AttributesMap attributes,
      List<io.opentelemetry.trace.Link> links,
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
    // Copy within synchronized context
    synchronized (lock) {
      return SpanWrapper.create(
          this,
          getImmutableLinks(),
          getImmutableTimedEvents(),
          getImmutableAttributes(),
          (attributes == null) ? 0 : attributes.getTotalAddedValues(),
          totalRecordedEvents,
          getStatusWithDefault(),
          name,
          endEpochNanos,
          hasEnded);
    }
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
  long getEndEpochNanos() {
    synchronized (lock) {
      return endEpochNanos;
    }
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
    if (key == null || key.length() == 0) {
      return;
    }
    synchronized (lock) {
      if (hasEnded) {
        logger.log(Level.FINE, "Calling setAttribute() on an ended Span.");
        return;
      }
      if (value == null || (value.getType().equals(STRING) && value.getStringValue() == null)) {
        if (attributes == null) {
          return;
        }
        attributes.remove(key);
        return;
      }
      if (attributes == null) {
        attributes = new AttributesMap(traceConfig.getMaxNumberOfAttributes());
      }

      if (traceConfig.shouldTruncateStringAttributeValues()) {
        value = StringUtils.truncateToSize(value, traceConfig.getMaxLengthOfAttributeValues());
      }

      attributes.put(key, value);
    }
  }

  @Override
  public void addEvent(String name) {
    if (name == null) {
      return;
    }
    addTimedEvent(TimedEvent.create(clock.now(), name, Attributes.empty(), 0));
  }

  @Override
  public void addEvent(String name, long timestamp) {
    if (name == null) {
      return;
    }
    addTimedEvent(TimedEvent.create(timestamp, name, Attributes.empty(), 0));
  }

  @Override
  public void addEvent(String name, Attributes attributes) {
    if (name == null) {
      return;
    }
    int totalAttributeCount = attributes.size();
    addTimedEvent(
        TimedEvent.create(
            clock.now(),
            name,
            copyAndLimitAttributes(attributes, traceConfig.getMaxNumberOfAttributesPerEvent()),
            totalAttributeCount));
  }

  @Override
  public void addEvent(String name, Attributes attributes, long timestamp) {
    if (name == null) {
      return;
    }
    int totalAttributeCount = attributes.size();
    addTimedEvent(
        TimedEvent.create(
            timestamp,
            name,
            copyAndLimitAttributes(attributes, traceConfig.getMaxNumberOfAttributesPerEvent()),
            totalAttributeCount));
  }

  @Override
  public void addEvent(io.opentelemetry.trace.Event event) {
    if (event == null) {
      return;
    }
    addTimedEvent(TimedEvent.create(clock.now(), event));
  }

  @Override
  public void addEvent(io.opentelemetry.trace.Event event, long timestamp) {
    if (event == null) {
      return;
    }
    addTimedEvent(TimedEvent.create(timestamp, event));
  }

  static Attributes copyAndLimitAttributes(final Attributes attributes, final int limit) {
    if (attributes.isEmpty() || attributes.size() <= limit) {
      return attributes;
    }

    Attributes.Builder result = Attributes.newBuilder();
    attributes.forEach(new LimitingAttributeConsumer(limit, result));
    return result.build();
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
    if (status == null) {
      return;
    }
    synchronized (lock) {
      if (hasEnded) {
        logger.log(Level.FINE, "Calling setStatus() on an ended Span.");
        return;
      }
      this.status = status;
    }
  }

  @Override
  public void recordException(Throwable exception) {
    if (exception == null) {
      return;
    }
    long timestamp = clock.now();
    Attributes.Builder attributes = Attributes.newBuilder();
    SemanticAttributes.EXCEPTION_TYPE.set(attributes, exception.getClass().getCanonicalName());
    if (exception.getMessage() != null) {
      SemanticAttributes.EXCEPTION_MESSAGE.set(attributes, exception.getMessage());
    }
    StringWriter writer = new StringWriter();
    exception.printStackTrace(new PrintWriter(writer));
    SemanticAttributes.EXCEPTION_STACKTRACE.set(attributes, writer.toString());

    addEvent(SemanticAttributes.EXCEPTION_EVENT_NAME, attributes.build(), timestamp);
  }

  @Override
  public void updateName(String name) {
    if (name == null) {
      return;
    }
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
    if (endOptions == null) {
      end();
      return;
    }
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

  SpanId getParentSpanId() {
    return parentSpanId;
  }

  Resource getResource() {
    return resource;
  }

  Kind getKind() {
    return kind;
  }

  long getStartEpochNanos() {
    return startEpochNanos;
  }

  boolean hasRemoteParent() {
    return hasRemoteParent;
  }

  int getTotalRecordedLinks() {
    return totalRecordedLinks;
  }

  @GuardedBy("lock")
  private List<Link> getImmutableLinks() {
    if (links.isEmpty()) {
      return Collections.emptyList();
    }
    List<Link> result = new ArrayList<>(links.size());
    for (io.opentelemetry.trace.Link link : links) {
      Link newLink;
      if (!(link instanceof Link)) {
        // Make a copy because the given Link may not be immutable and we may reference a lot of
        // memory.
        newLink = Link.create(link.getContext(), link.getAttributes());
      } else {
        newLink = (Link) link;
      }
      result.add(newLink);
    }
    return Collections.unmodifiableList(result);
  }

  @GuardedBy("lock")
  private List<Event> getImmutableTimedEvents() {
    if (events.isEmpty()) {
      return Collections.emptyList();
    }

    List<Event> results = new ArrayList<>(events.size());
    for (TimedEvent event : events) {
      if (event instanceof RawTimedEventWithEvent) {
        // make sure to copy the data if the event is wrapping another one,
        // so we don't hold on the caller's memory
        results.add(
            TimedEvent.create(
                event.getEpochNanos(),
                event.getName(),
                event.getAttributes(),
                event.getTotalAttributeCount()));
      } else {
        results.add(event);
      }
    }
    return Collections.unmodifiableList(results);
  }

  @GuardedBy("lock")
  private ReadableAttributes getImmutableAttributes() {
    if (attributes == null || attributes.isEmpty()) {
      return Attributes.empty();
    }
    // if the span has ended, then the attributes are unmodifiable,
    // so we can return them directly and save copying all the data.
    if (hasEnded) {
      return attributes;
    }
    // otherwise, make a copy of the data into an immutable container.
    Attributes.Builder builder = Attributes.newBuilder();
    for (Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
      builder.setAttribute(entry.getKey(), entry.getValue());
    }
    return builder.build();
  }

  private static class LimitingAttributeConsumer implements KeyValueConsumer<AttributeValue> {
    private final int limit;
    private final Attributes.Builder builder;
    private int added;

    public LimitingAttributeConsumer(int limit, Attributes.Builder builder) {
      this.limit = limit;
      this.builder = builder;
    }

    @Override
    public void consume(String key, AttributeValue value) {
      if (added < limit) {
        builder.setAttribute(key, value);
        added++;
      }
    }
  }
}
