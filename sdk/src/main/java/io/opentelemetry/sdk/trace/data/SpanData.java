/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable representation of all data collected by the {@link io.opentelemetry.trace.Span} class.
 *
 * @since 0.1.0
 */
@Immutable
public interface SpanData {

  /**
   * Gets the trace id for this span.
   *
   * @return the trace id.
   */
  TraceId getTraceId();

  /**
   * Gets the span id for this span.
   *
   * @return the span id.
   */
  SpanId getSpanId();

  /**
   * Gets the trace flags for this span.
   *
   * @return the trace flags for this span.
   */
  TraceFlags getTraceFlags();

  /**
   * Gets the {@code TraceState} for this span.
   *
   * @return the {@code TraceState} for this span.
   */
  TraceState getTraceState();

  /**
   * Returns the parent {@code SpanId}. If the {@code Span} is a root {@code Span}, the SpanId
   * returned will be invalid..
   *
   * @return the parent {@code SpanId} or an invalid SpanId if this is a root {@code Span}.
   * @since 0.1.0
   */
  SpanId getParentSpanId();

  /**
   * Returns the resource of this {@code Span}.
   *
   * @return the resource of this {@code Span}.
   * @since 0.1.0
   */
  Resource getResource();

  /**
   * Returns the instrumentation library specified when creating the tracer which produced this
   * {@code Span}.
   *
   * @return an instance of {@link InstrumentationLibraryInfo}
   */
  InstrumentationLibraryInfo getInstrumentationLibraryInfo();

  /**
   * Returns the name of this {@code Span}.
   *
   * @return the name of this {@code Span}.
   * @since 0.1.0
   */
  String getName();

  /**
   * Returns the kind of this {@code Span}.
   *
   * @return the kind of this {@code Span}.
   * @since 0.1.0
   */
  Kind getKind();

  /**
   * Returns the start epoch timestamp in nanos of this {@code Span}.
   *
   * @return the start epoch timestamp in nanos of this {@code Span}.
   * @since 0.1.0
   */
  long getStartEpochNanos();

  /**
   * Returns the attributes recorded for this {@code Span}.
   *
   * @return the attributes recorded for this {@code Span}.
   * @since 0.1.0
   */
  ReadableAttributes getAttributes();

  /**
   * Returns the timed events recorded for this {@code Span}.
   *
   * @return the timed events recorded for this {@code Span}.
   * @since 0.1.0
   */
  List<Event> getEvents();

  /**
   * Returns links recorded for this {@code Span}.
   *
   * @return links recorded for this {@code Span}.
   * @since 0.1.0
   */
  List<Link> getLinks();

  /**
   * Returns the {@code Status}.
   *
   * @return the {@code Status}.
   * @since 0.1.0
   */
  Status getStatus();

  /**
   * Returns the end epoch timestamp in nanos of this {@code Span}.
   *
   * @return the end epoch timestamp in nanos of this {@code Span}.
   * @since 0.1.0
   */
  long getEndEpochNanos();

  /**
   * Returns {@code true} if the parent is on a different process. {@code false} if this is a root
   * span.
   *
   * @return {@code true} if the parent is on a different process. {@code false} if this is a root
   *     span.
   * @since 0.3.0
   */
  boolean getHasRemoteParent();

  /**
   * Returns whether this Span has already been ended.
   *
   * @return {@code true} if the span has already been ended, {@code false} if not.
   * @since 0.4.0
   */
  boolean getHasEnded();

  /**
   * The total number of {@link Event} events that were recorded on this span. This number may be
   * larger than the number of events that are attached to this span, if the total number recorded
   * was greater than the configured maximum value. See: {@link TraceConfig#getMaxNumberOfEvents()}
   *
   * @return The total number of events recorded on this span.
   */
  int getTotalRecordedEvents();

  /**
   * The total number of {@link Link} links that were recorded on this span. This number may be
   * larger than the number of links that are attached to this span, if the total number recorded
   * was greater than the configured maximum value. See: {@link TraceConfig#getMaxNumberOfLinks()}
   *
   * @return The total number of links recorded on this span.
   */
  int getTotalRecordedLinks();

  /**
   * The total number of attributes that were recorded on this span. This number may be larger than
   * the number of attributes that are attached to this span, if the total number recorded was
   * greater than the configured maximum value. See: {@link TraceConfig#getMaxNumberOfAttributes()}
   *
   * @return The total number of attributes on this span.
   */
  int getTotalAttributeCount();

  /**
   * Returns a {@link Builder} populated with the information of this {@link SpanData}. This can be
   * used to apply modifications to {@link SpanData} during export, e.g., by calculating derived
   * attributes.
   *
   * @since 0.8.0
   */
  Builder toBuilder();

  /**
   * A builder of {@link SpanData}.
   *
   * @since 0.8.0
   */
  interface Builder {

    /**
     * Set the trace id on this builder.
     *
     * @param traceId the trace id.
     * @return this builder (for chaining).
     */
    Builder setTraceId(TraceId traceId);

    /**
     * Set the span id on this builder.
     *
     * @param spanId the span id.
     * @return this builder (for chaining).
     */
    Builder setSpanId(SpanId spanId);

    /**
     * Set the {@link TraceFlags} on this builder.
     *
     * @param traceFlags the trace flags.
     * @return this.
     */
    Builder setTraceFlags(TraceFlags traceFlags);

    /**
     * Set the {@link TraceState} on this builder.
     *
     * @param traceState the {@code TraceState}.
     * @return this.
     */
    Builder setTraceState(TraceState traceState);

    /**
     * The parent span id associated for this span, which may be null.
     *
     * @param parentSpanId the SpanId of the parent
     * @return this.
     */
    Builder setParentSpanId(SpanId parentSpanId);

    /**
     * Set the {@link Resource} associated with this span. Must not be null.
     *
     * @param resource the Resource that generated this span.
     * @return this
     */
    Builder setResource(Resource resource);

    /**
     * Sets the instrumentation library of the tracer which created this span. Must not be null.
     *
     * @param instrumentationLibraryInfo the instrumentation library of the tracer which created
     *     this span.
     * @return this
     */
    Builder setInstrumentationLibraryInfo(InstrumentationLibraryInfo instrumentationLibraryInfo);

    /**
     * Set the name of the span. Must not be null.
     *
     * @param name the name.
     * @return this
     */
    Builder setName(String name);

    /**
     * Set the start timestamp of the span.
     *
     * @param epochNanos the start epoch timestamp in nanos.
     * @return this
     */
    Builder setStartEpochNanos(long epochNanos);

    /**
     * Set the end timestamp of the span.
     *
     * @param epochNanos the end epoch timestamp in nanos.
     * @return this
     */
    Builder setEndEpochNanos(long epochNanos);

    /**
     * Set the attributes that are associated with this span, as a Map of String keys to
     * AttributeValue instances. Must not be null, may be empty.
     *
     * @param attributes a Map&lt;String, AttributeValue&gt; of attributes.
     * @return this
     * @see AttributeValue
     */
    Builder setAttributes(ReadableAttributes attributes);

    /**
     * Set timed events that are associated with this span. Must not be null, may be empty.
     *
     * @param events A List&lt;Event&gt; of events associated with this span.
     * @return this
     * @see Event
     */
    Builder setEvents(List<Event> events);

    /**
     * Set the status for this span. Must not be null.
     *
     * @param status The Status of this span.
     * @return this
     */
    Builder setStatus(Status status);

    /**
     * Set the kind of span. Must not be null.
     *
     * @param kind The Kind of span.
     * @return this
     */
    Builder setKind(Kind kind);

    /**
     * Set the links associated with this span. Must not be null, may be empty.
     *
     * @param links A List&lt;Link&gt;
     * @return this
     * @see io.opentelemetry.trace.Link
     */
    Builder setLinks(List<Link> links);

    /**
     * Sets to true if the span has a parent on a different process.
     *
     * @param hasRemoteParent A boolean indicating if the span has a remote parent.
     * @return this
     */
    Builder setHasRemoteParent(boolean hasRemoteParent);

    /**
     * Sets to true if the span has been ended.
     *
     * @param hasEnded A boolean indicating if the span has been ended.
     * @return this
     */
    Builder setHasEnded(boolean hasEnded);

    /**
     * Set the total number of events recorded on this span.
     *
     * @param totalRecordedEvents The total number of events recorded.
     * @return this
     */
    Builder setTotalRecordedEvents(int totalRecordedEvents);

    /**
     * Set the total number of links recorded on this span.
     *
     * @param totalRecordedLinks The total number of links recorded.
     * @return this
     */
    Builder setTotalRecordedLinks(int totalRecordedLinks);

    /**
     * Set the total number of attributes recorded on this span.
     *
     * @param totalAttributeCount The total number of attributes recorded.
     * @return this
     */
    Builder setTotalAttributeCount(int totalAttributeCount);

    /**
     * Create a new SpanData instance from the data in this {@link SpanData.Builder}.
     *
     * @return a new SpanData instance
     */
    SpanData build();
  }

  /**
   * An immutable implementation of {@link io.opentelemetry.trace.Link}.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  abstract class Link implements io.opentelemetry.trace.Link {

    private static final Attributes DEFAULT_ATTRIBUTE_COLLECTION = Attributes.empty();
    private static final int DEFAULT_ATTRIBUTE_COUNT = 0;

    /**
     * Returns a new immutable {@code Link}.
     *
     * @param spanContext the {@code SpanContext} of this {@code Link}.
     * @return a new immutable {@code Event<T>}
     * @since 0.1.0
     */
    public static Link create(SpanContext spanContext) {
      return new AutoValue_SpanData_Link(
          spanContext, DEFAULT_ATTRIBUTE_COLLECTION, DEFAULT_ATTRIBUTE_COUNT);
    }

    /**
     * Returns a new immutable {@code Link}.
     *
     * @param spanContext the {@code SpanContext} of this {@code Link}.
     * @param attributes the attributes of this {@code Link}.
     * @return a new immutable {@code Event<T>}
     * @since 0.1.0
     */
    public static Link create(SpanContext spanContext, Attributes attributes) {
      return new AutoValue_SpanData_Link(spanContext, attributes, attributes.size());
    }

    /**
     * Returns a new immutable {@code Link}.
     *
     * @param spanContext the {@code SpanContext} of this {@code Link}.
     * @param attributes the attributes of this {@code Link}.
     * @param totalAttributeCount the total number of attributed for this {@code Link}.
     * @return a new immutable {@code Event<T>}
     * @since 0.1.0
     */
    public static Link create(
        SpanContext spanContext, Attributes attributes, int totalAttributeCount) {
      return new AutoValue_SpanData_Link(spanContext, attributes, totalAttributeCount);
    }

    /**
     * The total number of attributes that were recorded on this Link. This number may be larger
     * than the number of attributes that are attached to this span, if the total number recorded
     * was greater than the configured maximum value. See: {@link
     * TraceConfig#getMaxNumberOfAttributesPerLink()}
     *
     * @return The number of attributes on this link.
     */
    public abstract int getTotalAttributeCount();

    Link() {}
  }

  interface Event extends io.opentelemetry.trace.Event {
    /**
     * Returns the epoch time in nanos of this event.
     *
     * @return the epoch time in nanos of this event.
     * @since 0.1.0
     */
    long getEpochNanos();

    /**
     * The total number of attributes that were recorded on this Event. This number may be larger
     * than the number of attributes that are attached to this span, if the total number recorded
     * was greater than the configured maximum value. See: {@link
     * TraceConfig#getMaxNumberOfAttributesPerEvent()}
     *
     * @return The total number of attributes on this event.
     */
    int getTotalAttributeCount();
  }
}
