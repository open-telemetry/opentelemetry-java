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

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable representation of all data collected by the {@link io.opentelemetry.trace.Span} class.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class SpanData {

  /**
   * Gets the trace id for this span.
   *
   * @return the trace id.
   */
  public abstract TraceId getTraceId();

  /**
   * Gets the span id for this span.
   *
   * @return the span id.
   */
  public abstract SpanId getSpanId();

  /**
   * Gets the trace flags for this span.
   *
   * @return the trace flags for this span.
   */
  public abstract TraceFlags getTraceFlags();

  /**
   * Gets the Tracestate for this span.
   *
   * @return the Tracestate for this span.
   */
  public abstract Tracestate getTracestate();

  /**
   * Returns the parent {@code SpanId}. If the {@code Span} is a root {@code Span}, the SpanId
   * returned will be invalid..
   *
   * @return the parent {@code SpanId} or an invalid SpanId if this is a root {@code Span}.
   * @since 0.1.0
   */
  public abstract SpanId getParentSpanId();

  /**
   * Returns the resource of this {@code Span}.
   *
   * @return the resource of this {@code Span}.
   * @since 0.1.0
   */
  public abstract Resource getResource();

  /**
   * Returns the instrumentation library specified when creating the tracer which produced this
   * {@code Span}.
   *
   * @return an instance of {@link InstrumentationLibraryInfo}
   */
  public abstract InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  /**
   * Returns the name of this {@code Span}.
   *
   * @return the name of this {@code Span}.
   * @since 0.1.0
   */
  public abstract String getName();

  /**
   * Returns the kind of this {@code Span}.
   *
   * @return the kind of this {@code Span}.
   * @since 0.1.0
   */
  public abstract Kind getKind();

  /**
   * Returns the start epoch timestamp in nanos of this {@code Span}.
   *
   * @return the start epoch timestamp in nanos of this {@code Span}.
   * @since 0.1.0
   */
  public abstract long getStartEpochNanos();

  /**
   * Returns the attributes recorded for this {@code Span}.
   *
   * @return the attributes recorded for this {@code Span}.
   * @since 0.1.0
   */
  public abstract Map<String, AttributeValue> getAttributes();

  /**
   * Returns the timed events recorded for this {@code Span}.
   *
   * @return the timed events recorded for this {@code Span}.
   * @since 0.1.0
   */
  public abstract List<TimedEvent> getTimedEvents();

  /**
   * Returns links recorded for this {@code Span}.
   *
   * @return links recorded for this {@code Span}.
   * @since 0.1.0
   */
  public abstract List<io.opentelemetry.trace.Link> getLinks();

  /**
   * Returns the {@code Status}.
   *
   * @return the {@code Status}.
   * @since 0.1.0
   */
  public abstract Status getStatus();

  /**
   * Returns the end epoch timestamp in nanos of this {@code Span}.
   *
   * @return the end epoch timestamp in nanos of this {@code Span}.
   * @since 0.1.0
   */
  public abstract long getEndEpochNanos();

  /**
   * Returns {@code true} if the parent is on a different process. {@code null} if this is a root
   * span.
   *
   * @return {@code true} if the parent is on a different process. {@code null} if this is a root
   *     span.
   * @since 0.5 -- update?
   */
  @Nullable
  public abstract Boolean getHasRemoteParent();

  /**
   * An immutable implementation of {@link Link}.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class Link implements io.opentelemetry.trace.Link {
    /**
     * Returns a new immutable {@code Link}.
     *
     * @param spanContext the {@code SpanContext} of this {@code Link}.
     * @return a new immutable {@code TimedEvent<T>}
     * @since 0.1.0
     */
    public static Link create(SpanContext spanContext) {
      return new AutoValue_SpanData_Link(
          spanContext, Collections.<String, AttributeValue>emptyMap());
    }

    /**
     * Returns a new immutable {@code Link}.
     *
     * @param spanContext the {@code SpanContext} of this {@code Link}.
     * @param attributes the attributes of this {@code Link}.
     * @return a new immutable {@code TimedEvent<T>}
     * @since 0.1.0
     */
    public static Link create(SpanContext spanContext, Map<String, AttributeValue> attributes) {
      return new AutoValue_SpanData_Link(
          spanContext, Collections.unmodifiableMap(new LinkedHashMap<>(attributes)));
    }
  }

  /**
   * A timed event representation.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class TimedEvent implements Event {
    /**
     * Returns a new immutable {@code TimedEvent}.
     *
     * @param epochNanos epoch timestamp in nanos of the {@code Event}.
     * @param name the name of the {@code Event}.
     * @param attributes the attributes of the {@code Event}.
     * @return a new immutable {@code TimedEvent<T>}
     * @since 0.1.0
     */
    public static TimedEvent create(
        long epochNanos, String name, Map<String, AttributeValue> attributes) {
      return new AutoValue_SpanData_TimedEvent(epochNanos, name, attributes);
    }

    /**
     * Returns the epoch time in nanos of this event.
     *
     * @return the epoch time in nanos of this event.
     * @since 0.1.0
     */
    public abstract long getEpochNanos();

    @Override
    public abstract String getName();

    @Override
    public abstract Map<String, AttributeValue> getAttributes();

    TimedEvent() {}
  }

  /**
   * Creates a new Builder for creating an SpanData instance.
   *
   * @return a new Builder.
   * @since 0.1.0
   */
  public static Builder newBuilder() {
    return new AutoValue_SpanData.Builder()
        .setParentSpanId(SpanId.getInvalid())
        .setInstrumentationLibraryInfo(InstrumentationLibraryInfo.EMPTY)
        .setLinks(Collections.<io.opentelemetry.trace.Link>emptyList())
        .setAttributes(Collections.<String, AttributeValue>emptyMap())
        .setTimedEvents(Collections.<TimedEvent>emptyList())
        .setResource(Resource.getEmpty())
        .setTracestate(Tracestate.getDefault())
        .setTraceFlags(TraceFlags.getDefault());
  }

  /**
   * A {@code Builder} class for {@link SpanData}.
   *
   * @since 0.1.0
   */
  @AutoValue.Builder
  public abstract static class Builder {

    abstract SpanData autoBuild();

    abstract Map<String, AttributeValue> getAttributes();

    abstract List<TimedEvent> getTimedEvents();

    abstract List<io.opentelemetry.trace.Link> getLinks();

    /**
     * Create a new SpanData instance from the data in this.
     *
     * @return a new SpanData instance
     * @since 0.1.0
     */
    public SpanData build() {
      // make unmodifiable copies of any collections
      setAttributes(Collections.unmodifiableMap(new HashMap<>(getAttributes())));
      setTimedEvents(Collections.unmodifiableList(new ArrayList<>(getTimedEvents())));
      setLinks(Collections.unmodifiableList(new ArrayList<>(getLinks())));
      return autoBuild();
    }

    /**
     * Set the trace id on this builder.
     *
     * @param traceId the trace id.
     * @return this builder (for chaining).
     */
    public abstract Builder setTraceId(TraceId traceId);

    /**
     * Set the span id on this builder.
     *
     * @param spanId the span id.
     * @return this builder (for chaining).
     */
    public abstract Builder setSpanId(SpanId spanId);

    /**
     * Set the trace flags on this builder.
     *
     * @param traceFlags the trace flags.
     * @return this builder (for chaining).
     */
    public abstract Builder setTraceFlags(TraceFlags traceFlags);

    /**
     * Set the tracestate for this builder.
     *
     * @param tracestate the tracestate
     * @return this builder (for chaining).
     */
    public abstract Builder setTracestate(Tracestate tracestate);

    /**
     * The parent span id associated for this span, which may be null.
     *
     * @param parentSpanId the SpanId of the parent
     * @return this
     * @see SpanId
     * @since 0.1.0
     */
    public abstract Builder setParentSpanId(SpanId parentSpanId);

    /**
     * Set the resource associated with this span. Must not be null.
     *
     * @param resource the Resource that generated this span.
     * @return this
     * @see Resource
     * @since 0.1.0
     */
    public abstract Builder setResource(Resource resource);

    /**
     * Sets the instrumentation library of the tracer which created this span. Must not be null.
     *
     * @param instrumentationLibraryInfo the instrumentation library of the tracer which created
     *     this span.
     * @return this
     * @see InstrumentationLibraryInfo
     * @since 0.2.0
     */
    public abstract Builder setInstrumentationLibraryInfo(
        InstrumentationLibraryInfo instrumentationLibraryInfo);

    /**
     * Set the name of the span. Must not be null.
     *
     * @param name the name.
     * @return this
     * @since 0.1.0
     */
    public abstract Builder setName(String name);

    /**
     * Set the start timestamp of the span.
     *
     * @param epochNanos the start epoch timestamp in nanos.
     * @return this
     * @since 0.1.0
     */
    public abstract Builder setStartEpochNanos(long epochNanos);

    /**
     * Set the end timestamp of the span.
     *
     * @param epochNanos the end epoch timestamp in nanos.
     * @return this
     * @since 0.1.0
     */
    public abstract Builder setEndEpochNanos(long epochNanos);

    /**
     * Set the attributes that are associated with this span, as a Map of String keys to
     * AttributeValue instances. Must not be null, may be empty.
     *
     * @param attributes a Map&lt;String, AttributeValue&gt; of attributes.
     * @return this
     * @see AttributeValue
     * @since 0.1.0
     */
    public abstract Builder setAttributes(Map<String, AttributeValue> attributes);

    /**
     * Set timed events that are associated with this span. Must not be null, may be empty.
     *
     * @param events A List&lt;TimedEvent&gt; of events associated with this span.
     * @return this
     * @see TimedEvent
     * @since 0.1.0
     */
    public abstract Builder setTimedEvents(List<TimedEvent> events);

    /**
     * Set the status for this span. Must not be null.
     *
     * @param status The Status of this span.
     * @return this
     * @since 0.1.0
     */
    public abstract Builder setStatus(Status status);

    /**
     * Set the kind of span. Must not be null.
     *
     * @param kind The Kind of span.
     * @return this
     * @since 0.1.0
     */
    public abstract Builder setKind(Kind kind);

    /**
     * Set the links associated with this span. Must not be null, may be empty.
     *
     * @param links A List&lt;Link&gt;
     * @return this
     * @see io.opentelemetry.trace.Link
     * @since 0.1.0
     */
    public abstract Builder setLinks(List<io.opentelemetry.trace.Link> links);

    /**
     * Sets to true if the span has a parent on a different process.
     *
     * @param hasRemoteParent A Boolean indicating if the span has a remote parent.
     * @return this
     * @since 0.1.0
     */
    public abstract Builder setHasRemoteParent(Boolean hasRemoteParent);
  }
}
