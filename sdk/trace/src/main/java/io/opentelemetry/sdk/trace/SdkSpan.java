/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.internal.AttributeUtil;
import io.opentelemetry.sdk.common.internal.AttributesMap;
import io.opentelemetry.sdk.common.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.common.internal.InstrumentationScopeUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.ExceptionEventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.internal.ExtendedSpanProcessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** Implementation for the {@link Span} class that records trace events. */
@ThreadSafe
final class SdkSpan implements ReadWriteSpan {

  private static final Logger logger = Logger.getLogger(SdkSpan.class.getName());

  // The config used when constructing this Span.
  private final SpanLimits spanLimits;
  // Contains the identifiers associated with this Span.
  private final SpanContext context;
  // The parent SpanContext of this span. Invalid if this is a root span.
  private final SpanContext parentSpanContext;
  // Handler called when the span starts and ends.
  private final SpanProcessor spanProcessor;
  // Resolves exception.* when an recordException is called
  private final ExceptionAttributeResolver exceptionAttributeResolver;
  // The kind of the span.
  private final SpanKind kind;
  // The clock used to get the time.
  private final AnchoredClock clock;
  // The resource associated with this span.
  private final Resource resource;
  // instrumentation scope of the named tracer which created this span
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  // The start time of the span.
  private final long startEpochNanos;
  // Callback to run when span ends to record metrics.
  private final Runnable recordEndMetrics;

  // Lock used to internally guard the mutable state of this instance
  private final Object lock = new Object();

  @GuardedBy("lock")
  private String name;

  // Set of recorded attributes. DO NOT CALL any other method that changes the ordering of events.
  @GuardedBy("lock")
  @Nullable
  private AttributesMap attributes;

  // List of recorded events.
  @GuardedBy("lock")
  @Nullable
  private List<EventData> events;

  // Number of events recorded.
  @GuardedBy("lock")
  private int totalRecordedEvents = 0;

  // The displayed name of the span.
  // List of recorded links to parent and child spans.
  @GuardedBy("lock")
  @Nullable
  List<LinkData> links;

  // Number of links recorded.
  @GuardedBy("lock")
  private int totalRecordedLinks;

  // The status of the span.
  @GuardedBy("lock")
  private StatusData status = StatusData.unset();

  // The end time of the span.
  @GuardedBy("lock")
  private long endEpochNanos;

  private enum EndState {
    NOT_ENDED,
    ENDING,
    ENDED
  }

  @GuardedBy("lock")
  private EndState hasEnded;

  /**
   * The thread on which {@link #end()} is called and which will be invoking the {@link
   * SpanProcessor}s. This field is used to ensure that only this thread may modify the span while
   * it is in state {@link EndState#ENDING} to prevent concurrent updates outside of {@link
   * ExtendedSpanProcessor#onEnding(ReadWriteSpan)}.
   */
  @GuardedBy("lock")
  @Nullable
  private Thread spanEndingThread;

  private SdkSpan(
      SpanContext context,
      String name,
      InstrumentationScopeInfo instrumentationScopeInfo,
      SpanKind kind,
      SpanContext parentSpanContext,
      SpanLimits spanLimits,
      SpanProcessor spanProcessor,
      ExceptionAttributeResolver exceptionAttributeResolver,
      AnchoredClock clock,
      Resource resource,
      @Nullable AttributesMap attributes,
      @Nullable List<LinkData> links,
      int totalRecordedLinks,
      long startEpochNanos,
      Runnable recordEndMetrics) {
    this.context = context;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.parentSpanContext = parentSpanContext;
    this.links = links;
    this.totalRecordedLinks = totalRecordedLinks;
    this.name = name;
    this.kind = kind;
    this.spanProcessor = spanProcessor;
    this.exceptionAttributeResolver = exceptionAttributeResolver;
    this.resource = resource;
    this.hasEnded = EndState.NOT_ENDED;
    this.clock = clock;
    this.startEpochNanos = startEpochNanos;
    this.attributes = attributes;
    this.spanLimits = spanLimits;
    this.recordEndMetrics = recordEndMetrics;
  }

  /**
   * Creates and starts a span with the given configuration.
   *
   * @param context supplies the trace_id and span_id for the newly started span.
   * @param name the displayed name for the new span.
   * @param kind the span kind.
   * @param parentSpan the parent span, or {@link Span#getInvalid()} if this span is a root span.
   * @param spanLimits limits applied to this span.
   * @param spanProcessor handler called when the span starts and ends.
   * @param tracerClock the tracer's clock
   * @param resource the resource associated with this span.
   * @param attributes the attributes set during span creation.
   * @param links the links set during span creation, may be truncated. The list MUST be immutable.
   * @param recordEndMetrics a {@link Runnable} to run when the span is ended to record metrics.
   * @return a new and started span.
   */
  static SdkSpan startSpan(
      SpanContext context,
      String name,
      InstrumentationScopeInfo instrumentationScopeInfo,
      SpanKind kind,
      Span parentSpan,
      Context parentContext,
      SpanLimits spanLimits,
      SpanProcessor spanProcessor,
      ExceptionAttributeResolver exceptionAttributeResolver,
      Clock tracerClock,
      Resource resource,
      @Nullable AttributesMap attributes,
      @Nullable List<LinkData> links,
      int totalRecordedLinks,
      long userStartEpochNanos,
      Runnable recordEndMetrics) {
    boolean createdAnchoredClock;
    AnchoredClock clock;
    if (parentSpan instanceof SdkSpan) {
      SdkSpan parentRecordEventsSpan = (SdkSpan) parentSpan;
      clock = parentRecordEventsSpan.clock;
      createdAnchoredClock = false;
    } else {
      clock = AnchoredClock.create(tracerClock);
      createdAnchoredClock = true;
    }

    long startEpochNanos;
    if (userStartEpochNanos != 0) {
      startEpochNanos = userStartEpochNanos;
    } else if (createdAnchoredClock) {
      // If this is a new AnchoredClock, the start time is now, so just use it to avoid
      // recomputing current time.
      startEpochNanos = clock.startTime();
    } else {
      // AnchoredClock created in the past, so need to compute now.
      startEpochNanos = clock.now();
    }

    SdkSpan span =
        new SdkSpan(
            context,
            name,
            instrumentationScopeInfo,
            kind,
            parentSpan.getSpanContext(),
            spanLimits,
            spanProcessor,
            exceptionAttributeResolver,
            clock,
            resource,
            attributes,
            links,
            totalRecordedLinks,
            startEpochNanos,
            recordEndMetrics);
    // Call onStart here instead of calling in the constructor to make sure the span is completely
    // initialized.
    if (spanProcessor.isStartRequired()) {
      spanProcessor.onStart(parentContext, span);
    }
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
          totalRecordedLinks,
          status,
          name,
          endEpochNanos,
          hasEnded == EndState.ENDED);
    }
  }

  @Override
  @Nullable
  public <T> T getAttribute(AttributeKey<T> key) {
    synchronized (lock) {
      return attributes == null ? null : attributes.get(key);
    }
  }

  @Override
  public Attributes getAttributes() {
    synchronized (lock) {
      return attributes == null ? Attributes.empty() : attributes.immutableCopy();
    }
  }

  @Override
  public boolean hasEnded() {
    synchronized (lock) {
      return hasEnded == EndState.ENDED;
    }
  }

  @Override
  public SpanContext getSpanContext() {
    return context;
  }

  @Override
  public SpanContext getParentSpanContext() {
    return parentSpanContext;
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

  @Override
  @Deprecated
  public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return InstrumentationScopeUtil.toInstrumentationLibraryInfo(getInstrumentationScopeInfo());
  }

  @Override
  public InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
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
      return (hasEnded == EndState.NOT_ENDED ? clock.now() : endEpochNanos) - startEpochNanos;
    }
  }

  /** Returns the {@link AnchoredClock} used by this {@link Span}. */
  AnchoredClock getClock() {
    return clock;
  }

  @Override
  public <T> ReadWriteSpan setAttribute(AttributeKey<T> key, @Nullable T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    synchronized (lock) {
      if (!isModifiableByCurrentThread()) {
        logger.log(Level.FINE, "Calling setAttribute() on an ended Span.");
        return this;
      }
      if (attributes == null) {
        attributes =
            AttributesMap.create(
                spanLimits.getMaxNumberOfAttributes(), spanLimits.getMaxAttributeValueLength());
      }

      attributes.put(key, value);
    }
    return this;
  }

  @GuardedBy("lock")
  private boolean isModifiableByCurrentThread() {
    return hasEnded == EndState.NOT_ENDED
        || (hasEnded == EndState.ENDING && Thread.currentThread() == spanEndingThread);
  }

  @Override
  public ReadWriteSpan addEvent(String name) {
    if (name == null) {
      return this;
    }
    addTimedEvent(EventData.create(clock.now(), name, Attributes.empty(), 0));
    return this;
  }

  @Override
  public ReadWriteSpan addEvent(String name, long timestamp, TimeUnit unit) {
    if (name == null || unit == null) {
      return this;
    }
    addTimedEvent(EventData.create(unit.toNanos(timestamp), name, Attributes.empty(), 0));
    return this;
  }

  @Override
  public ReadWriteSpan addEvent(String name, Attributes attributes) {
    if (name == null) {
      return this;
    }
    if (attributes == null) {
      attributes = Attributes.empty();
    }
    int totalAttributeCount = attributes.size();
    addTimedEvent(
        EventData.create(
            clock.now(),
            name,
            AttributeUtil.applyAttributesLimit(
                attributes,
                spanLimits.getMaxNumberOfAttributesPerEvent(),
                spanLimits.getMaxAttributeValueLength()),
            totalAttributeCount));
    return this;
  }

  @Override
  public ReadWriteSpan addEvent(String name, Attributes attributes, long timestamp, TimeUnit unit) {
    if (name == null || unit == null) {
      return this;
    }
    if (attributes == null) {
      attributes = Attributes.empty();
    }
    int totalAttributeCount = attributes.size();
    addTimedEvent(
        EventData.create(
            unit.toNanos(timestamp),
            name,
            AttributeUtil.applyAttributesLimit(
                attributes,
                spanLimits.getMaxNumberOfAttributesPerEvent(),
                spanLimits.getMaxAttributeValueLength()),
            totalAttributeCount));
    return this;
  }

  private void addTimedEvent(EventData timedEvent) {
    synchronized (lock) {
      if (!isModifiableByCurrentThread()) {
        logger.log(Level.FINE, "Calling addEvent() on an ended Span.");
        return;
      }
      if (events == null) {
        events = new ArrayList<>();
      }
      if (events.size() < spanLimits.getMaxNumberOfEvents()) {
        events.add(timedEvent);
      }
      totalRecordedEvents++;
    }
  }

  @Override
  public ReadWriteSpan setStatus(StatusCode statusCode, @Nullable String description) {
    if (statusCode == null) {
      return this;
    }
    synchronized (lock) {
      if (!isModifiableByCurrentThread()) {
        logger.log(Level.FINE, "Calling setStatus() on an ended Span.");
        return this;
      }

      // If current status is OK, ignore further attempts to change it
      if (this.status.getStatusCode() == StatusCode.OK) {
        logger.log(Level.FINE, "Calling setStatus() on a Span that is already set to OK.");
        return this;
      }

      // Ignore attempts to set status to UNSET
      if (statusCode == StatusCode.UNSET) {
        logger.log(Level.FINE, "Ignoring call to setStatus() with status UNSET.");
        return this;
      }

      // Ignore description when status is not ERROR
      if (description != null && statusCode != StatusCode.ERROR) {
        logger.log(Level.FINE, "Ignoring setStatus() description since status is not ERROR.");
        description = null;
      }

      this.status = StatusData.create(statusCode, description);
    }
    return this;
  }

  @Override
  public ReadWriteSpan recordException(Throwable exception) {
    recordException(exception, Attributes.empty());
    return this;
  }

  @Override
  public ReadWriteSpan recordException(Throwable exception, Attributes additionalAttributes) {
    if (exception == null) {
      return this;
    }
    if (additionalAttributes == null) {
      additionalAttributes = Attributes.empty();
    }

    int maxAttributeLength = spanLimits.getMaxAttributeValueLength();
    AttributesMap attributes =
        AttributesMap.create(
            spanLimits.getMaxNumberOfAttributes(), spanLimits.getMaxAttributeValueLength());

    exceptionAttributeResolver.setExceptionAttributes(
        attributes::putIfCapacity, exception, maxAttributeLength);

    additionalAttributes.forEach(attributes::put);

    addTimedEvent(
        ExceptionEventData.create(
            clock.now(), exception, attributes, attributes.getTotalAddedValues()));
    return this;
  }

  @Override
  public ReadWriteSpan updateName(String name) {
    if (name == null) {
      return this;
    }
    synchronized (lock) {
      if (!isModifiableByCurrentThread()) {
        logger.log(Level.FINE, "Calling updateName() on an ended Span.");
        return this;
      }
      this.name = name;
    }
    return this;
  }

  @Override
  public Span addLink(SpanContext spanContext, Attributes attributes) {
    if (spanContext == null || !spanContext.isValid()) {
      return this;
    }
    if (attributes == null) {
      attributes = Attributes.empty();
    }
    LinkData link =
        LinkData.create(
            spanContext,
            AttributeUtil.applyAttributesLimit(
                attributes,
                spanLimits.getMaxNumberOfAttributesPerLink(),
                spanLimits.getMaxAttributeValueLength()));
    synchronized (lock) {
      if (!isModifiableByCurrentThread()) {
        logger.log(Level.FINE, "Calling addLink() on an ended Span.");
        return this;
      }
      if (links == null) {
        links = new ArrayList<>();
      }
      if (links.size() < spanLimits.getMaxNumberOfLinks()) {
        links.add(link);
      }
      totalRecordedLinks++;
    }
    return this;
  }

  @Override
  public void end() {
    endInternal(clock.now());
  }

  @Override
  public void end(long timestamp, TimeUnit unit) {
    if (unit == null) {
      unit = TimeUnit.NANOSECONDS;
    }
    endInternal(timestamp == 0 ? clock.now() : unit.toNanos(timestamp));
  }

  private void endInternal(long endEpochNanos) {
    synchronized (lock) {
      if (hasEnded != EndState.NOT_ENDED) {
        logger.log(Level.FINE, "Calling end() on an ended or ending Span.");
        return;
      }
      this.endEpochNanos = endEpochNanos;
      spanEndingThread = Thread.currentThread();
      hasEnded = EndState.ENDING;
    }
    recordEndMetrics.run();
    if (spanProcessor instanceof ExtendedSpanProcessor) {
      ExtendedSpanProcessor extendedSpanProcessor = (ExtendedSpanProcessor) spanProcessor;
      if (extendedSpanProcessor.isOnEndingRequired()) {
        extendedSpanProcessor.onEnding(this);
      }
    }
    synchronized (lock) {
      hasEnded = EndState.ENDED;
      spanEndingThread = null;
    }
    if (spanProcessor.isEndRequired()) {
      spanProcessor.onEnd(this);
    }
  }

  @Override
  public boolean isRecording() {
    synchronized (lock) {
      return hasEnded != EndState.ENDED;
    }
  }

  Resource getResource() {
    return resource;
  }

  @Override
  public SpanKind getKind() {
    return kind;
  }

  long getStartEpochNanos() {
    return startEpochNanos;
  }

  @GuardedBy("lock")
  private List<EventData> getImmutableTimedEvents() {
    if (events == null) {
      return Collections.emptyList();
    }

    // if the span has ended, then the events are unmodifiable
    // so we can return them directly and save copying all the data.
    if (hasEnded == EndState.ENDED) {
      return Collections.unmodifiableList(events);
    }

    return Collections.unmodifiableList(new ArrayList<>(events));
  }

  @GuardedBy("lock")
  private Attributes getImmutableAttributes() {
    if (attributes == null || attributes.isEmpty()) {
      return Attributes.empty();
    }
    // if the span has ended, then the attributes are unmodifiable,
    // so we can return them directly and save copying all the data.
    if (hasEnded == EndState.ENDED) {
      return attributes;
    }
    // otherwise, make a copy of the data into an immutable container.
    return attributes.immutableCopy();
  }

  @GuardedBy("lock")
  private List<LinkData> getImmutableLinks() {
    if (links == null || links.isEmpty()) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(links);
  }

  @Override
  public String toString() {
    String name;
    String attributes;
    String status;
    long totalRecordedEvents;
    long endEpochNanos;
    long totalRecordedLinks;
    synchronized (lock) {
      name = this.name;
      attributes = String.valueOf(this.attributes);
      status = String.valueOf(this.status);
      totalRecordedEvents = this.totalRecordedEvents;
      endEpochNanos = this.endEpochNanos;
      totalRecordedLinks = this.totalRecordedLinks;
    }
    return "SdkSpan{traceId="
        + context.getTraceId()
        + ", spanId="
        + context.getSpanId()
        + ", parentSpanContext="
        + parentSpanContext
        + ", name="
        + name
        + ", kind="
        + kind
        + ", attributes="
        + attributes
        + ", status="
        + status
        + ", totalRecordedEvents="
        + totalRecordedEvents
        + ", totalRecordedLinks="
        + totalRecordedLinks
        + ", startEpochNanos="
        + startEpochNanos
        + ", endEpochNanos="
        + endEpochNanos
        + "}";
  }
}
