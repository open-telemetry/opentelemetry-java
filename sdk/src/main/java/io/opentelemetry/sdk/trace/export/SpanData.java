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

package io.opentelemetry.sdk.trace.export;

import com.google.auto.value.AutoValue;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Timestamp;
import io.opentelemetry.trace.TraceId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
   * Returns a new immutable {@code SpanData}.
   *
   * @param context the {@code SpanContext} of the {@code Span}.
   * @param parentSpanId the parent {@code SpanId} of the {@code Span}. {@code null} if the {@code
   *     Span} is a root.
   * @param resource the resource this span was executed on.
   * @param name the name of the {@code Span}.
   * @param kind the kind of the {@code Span}.
   * @param startTimestamp the start {@code Timestamp} of the {@code Span}.
   * @param attributes the attributes associated with the {@code Span}.
   * @param timedEvents the events associated with the {@code Span}.
   * @param links the links associated with the {@code Span}.
   * @param status the {@code Status} of the {@code Span}.
   * @param endTimestamp the end {@code Timestamp} of the {@code Span}.
   * @return a new immutable {@code SpanData}.
   * @since 0.1.0
   */
  public static SpanData create(
      SpanContext context,
      @Nullable SpanId parentSpanId,
      Resource resource,
      String name,
      Kind kind,
      Timestamp startTimestamp,
      Map<String, AttributeValue> attributes,
      List<TimedEvent> timedEvents,
      List<io.opentelemetry.trace.Link> links,
      Status status,
      Timestamp endTimestamp) {
    return AutoValue_SpanData.newBuilder()
        .context(context)
        .parentSpanId(parentSpanId == null ? SpanId.getInvalid() : parentSpanId)
        .resource(resource)
        .name(name)
        .kind(kind)
        .startTimestamp(startTimestamp)
        .attributes(
            Collections.unmodifiableMap(
                new HashMap<>(Utils.checkNotNull(attributes, "attributes"))))
        .timedEvents(
            Collections.unmodifiableList(
                new ArrayList<>(Utils.checkNotNull(timedEvents, "timedEvents"))))
        .links(Collections.unmodifiableList(new ArrayList<>(Utils.checkNotNull(links, "links"))))
        .status(status)
        .endTimestamp(endTimestamp)
        .build();
  }

  /**
   * Returns the {@code SpanContext} associated with this {@code Span}.
   *
   * @return the {@code SpanContext} associated with this {@code Span}.
   * @since 0.1.0
   */
  public abstract SpanContext getContext();

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
   * Returns the start {@code Timestamp} of this {@code Span}.
   *
   * @return the start {@code Timestamp} of this {@code Span}.
   * @since 0.1.0
   */
  public abstract Timestamp getStartTimestamp();

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
   * Returns the end {@code Timestamp}.
   *
   * @return the end {@code Timestamp}.
   * @since 0.1.0
   */
  public abstract Timestamp getEndTimestamp();

  /**
   * The trace id for this span, retrieved from the associated SpanContext.
   *
   * @return the TraceId
   */
  public TraceId getTraceId() {
    return getContext().getTraceId();
  }

  /**
   * The id of this span, retrieved from the associated SpanContext.
   *
   * @return the SpanId
   */
  public SpanId getSpanId() {
    return getContext().getSpanId();
  }

  /**
   * A timed event representation.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class TimedEvent {
    /**
     * Returns a new immutable {@code TimedEvent<T>}.
     *
     * @param timestamp the {@code Timestamp} of this event.
     * @param event the event.
     * @return a new immutable {@code TimedEvent<T>}
     * @since 0.1.0
     */
    public static TimedEvent create(Timestamp timestamp, io.opentelemetry.trace.Event event) {
      return new AutoValue_SpanData_TimedEvent(timestamp, event.getName(), event.getAttributes());
    }

    /**
     * Returns the {@code Timestamp} of this event.
     *
     * @return the {@code Timestamp} of this event.
     * @since 0.1.0
     */
    public abstract Timestamp getTimestamp();

    /**
     * Returns the name of this event.
     *
     * @return the name of this event.
     */
    public abstract String getName();

    /**
     * Gets the attributes for this event.
     *
     * @return the attributes for this event.
     */
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
    return new AutoValue_SpanData.Builder();
  }

  /**
   * A {@code Builder} class for {@link SpanData}.
   *
   * @since 0.1.0
   */
  @AutoValue.Builder
  public abstract static class Builder {

    /**
     * The SpanContext associated with this span.
     *
     * @param context the SpanContext
     * @see SpanContext
     * @return this
     * @since 0.1.0
     */
    public abstract Builder context(SpanContext context);

    /**
     * The parent span id associated for this span, which may be null.
     *
     * @param parentSpanId the SpanId of the parent
     * @see SpanId
     * @return this
     * @since 0.1.0
     */
    public abstract Builder parentSpanId(SpanId parentSpanId);

    /**
     * Set the resource associated with this span. Must not be null.
     *
     * @param resource the Resource that generated this span.
     * @see Resource
     * @return this
     * @since 0.1.0
     */
    public abstract Builder resource(Resource resource);

    /**
     * Set the name of the span. Must not be null.
     *
     * @param name the name.
     * @return this
     * @since 0.1.0
     */
    public abstract Builder name(String name);

    /**
     * Set the start timestamp of the span. Must not be null.
     *
     * @param timestamp the start Timestamp
     * @see Timestamp
     * @return this
     * @since 0.1.0
     */
    public abstract Builder startTimestamp(Timestamp timestamp);

    /**
     * Set the end timestamp of the span. Must not be null.
     *
     * @param timestamp the end Timestamp
     * @see Timestamp
     * @return this
     * @since 0.1.0
     */
    public abstract Builder endTimestamp(Timestamp timestamp);

    /**
     * Set the attributes that are associated with this span, as a Map of String keys to
     * AttributeValue instances. Must not be null, may be empty.
     *
     * @param attributes a Map&lt;String, AttributeValue&gt; of attributes.
     * @see AttributeValue
     * @return this
     * @since 0.1.0
     */
    public abstract Builder attributes(Map<String, AttributeValue> attributes);

    /**
     * Set timed events that are associated with this span. Must not be null, may be empty.
     *
     * @param events A List&lt;TimedEvent&gt; of events associated with this span.
     * @see TimedEvent
     * @return this
     * @since 0.1.0
     */
    public abstract Builder timedEvents(List<TimedEvent> events);

    /**
     * Set the status for this span. Must not be null.
     *
     * @param status The Status of this span.
     * @return this
     * @since 0.1.0
     */
    public abstract Builder status(Status status);

    /**
     * Set the kind of span. Must not be null.
     *
     * @param kind The Kind of span.
     * @return this
     * @since 0.1.0
     */
    public abstract Builder kind(Kind kind);

    /**
     * Set the links associated with this span. Must not be null, may be empty.
     *
     * @param links A List&lt;Link&gt;
     * @see io.opentelemetry.trace.Link
     * @return this
     * @since 0.1.0
     */
    public abstract Builder links(List<io.opentelemetry.trace.Link> links);

    /**
     * Create a new SpanData instance from the data in this.
     *
     * @return a new SpanData instance
     * @since 0.1.0
     */
    public abstract SpanData build();
  }
}
