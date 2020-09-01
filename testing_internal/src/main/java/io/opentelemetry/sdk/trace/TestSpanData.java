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

package io.opentelemetry.sdk.trace;

import com.google.auto.value.AutoValue;
import io.grpc.Context;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable representation of all data collected by the {@link io.opentelemetry.trace.Span} class.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class TestSpanData implements SpanData {

  /**
   * Creates a new Builder for creating an SpanData instance.
   *
   * @return a new Builder.
   * @since 0.1.0
   */
  public static Builder newBuilder() {
    return new AutoValue_TestSpanData.Builder()
        .setParent(Context.ROOT)
        .setInstrumentationLibraryInfo(InstrumentationLibraryInfo.getEmpty())
        .setLinks(Collections.<Link>emptyList())
        .setTotalRecordedLinks(0)
        .setAttributes(Attributes.empty())
        .setEvents(Collections.<Event>emptyList())
        .setTotalRecordedEvents(0)
        .setResource(Resource.getEmpty())
        .setTraceState(TraceState.getDefault())
        .setTraceFlags(TraceFlags.getDefault())
        .setTotalAttributeCount(0);
  }

  @Override
  public boolean getHasRemoteParent() {
    return TracingContextUtils.getSpan(getParent()).getContext().isRemote();
  }

  @Override
  public SpanId getParentSpanId() {
    return TracingContextUtils.getSpan(getParent()).getContext().getSpanId();
  }

  /**
   * A {@code Builder} class for {@link TestSpanData}.
   *
   * @since 0.1.0
   */
  @AutoValue.Builder
  public abstract static class Builder {

    abstract TestSpanData autoBuild();

    abstract List<Event> getEvents();

    abstract List<Link> getLinks();

    abstract TraceId getTraceId();

    /**
     * Create a new SpanData instance from the data in this.
     *
     * @return a new SpanData instance
     * @since 0.1.0
     */
    public TestSpanData build() {
      // make unmodifiable copies of any collections
      setEvents(Collections.unmodifiableList(new ArrayList<>(getEvents())));
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
     * Set the {@link TraceFlags} on this builder.
     *
     * @param traceFlags the trace flags.
     * @return this.
     */
    public abstract Builder setTraceFlags(TraceFlags traceFlags);

    /**
     * Set the {@link TraceState} on this builder.
     *
     * @param traceState the {@code TraceState}.
     * @return this.
     */
    public abstract Builder setTraceState(TraceState traceState);

    /**
     * The parent Context associated for this span, which may be empty.
     *
     * @param parent the parent Context of the parent
     * @return this.
     * @since 0.1.0
     */
    public abstract Builder setParent(Context parent);

    /**
     * Utility function to set a parent context based on the current one.
     *
     * @see #setParent(Context)
     */
    public Builder setParent(SpanId parentSpanId, boolean hasRemoteParent) {
      return setParent(
          TracingContextUtils.withSpan(
              DefaultSpan.create(
                  hasRemoteParent
                      ? SpanContext.createFromRemoteParent(
                          getTraceId(),
                          parentSpanId,
                          TraceFlags.getDefault(),
                          TraceState.getDefault())
                      : SpanContext.create(
                          getTraceId(),
                          parentSpanId,
                          TraceFlags.getDefault(),
                          TraceState.getDefault())),
              Context.current()));
    }

    /**
     * Set the {@link Resource} associated with this span. Must not be null.
     *
     * @param resource the Resource that generated this span.
     * @return this
     * @since 0.1.0
     */
    public abstract Builder setResource(Resource resource);

    /**
     * Sets the instrumentation library of the tracer which created this span. Must not be null.
     *
     * @param instrumentationLibraryInfo the instrumentation library of the tracer which created
     *     this span.
     * @return this
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
    public abstract Builder setAttributes(ReadableAttributes attributes);

    /**
     * Set timed events that are associated with this span. Must not be null, may be empty.
     *
     * @param events A List&lt;Event&gt; of events associated with this span.
     * @return this
     * @see Event
     * @since 0.1.0
     */
    public abstract Builder setEvents(List<Event> events);

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
    public abstract Builder setLinks(List<Link> links);

    /**
     * Sets to true if the span has been ended.
     *
     * @param hasEnded A boolean indicating if the span has been ended.
     * @return this
     * @since 0.4.0
     */
    public abstract Builder setHasEnded(boolean hasEnded);

    /**
     * Set the total number of events recorded on this span.
     *
     * @param totalRecordedEvents The total number of events recorded.
     * @return this
     * @since 0.4.0
     */
    public abstract Builder setTotalRecordedEvents(int totalRecordedEvents);

    /**
     * Set the total number of links recorded on this span.
     *
     * @param totalRecordedLinks The total number of links recorded.
     * @return this
     * @since 0.4.0
     */
    public abstract Builder setTotalRecordedLinks(int totalRecordedLinks);

    /**
     * Set the total number of attributes recorded on this span.
     *
     * @param totalAttributeCount The total number of attributes recorded.
     * @return this
     */
    public abstract Builder setTotalAttributeCount(int totalAttributeCount);
  }
}
