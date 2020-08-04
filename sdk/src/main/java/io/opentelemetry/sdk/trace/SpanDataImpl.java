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
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.List;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
// AutoValue generated hashCode is fine but we need to define equals to accept the base SpanData
// type.
@SuppressWarnings("EqualsHashCode")
abstract class SpanDataImpl implements SpanData {

  static SpanData.Builder newBuilder(SpanData spanData) {
    return new AutoValue_SpanDataImpl.Builder()
        .setTraceId(spanData.getTraceId())
        .setSpanId(spanData.getSpanId())
        .setTraceFlags(spanData.getTraceFlags())
        .setTraceState(spanData.getTraceState())
        .setParentSpanId(spanData.getParentSpanId())
        .setResource(spanData.getResource())
        .setInstrumentationLibraryInfo(spanData.getInstrumentationLibraryInfo())
        .setName(spanData.getName())
        .setKind(spanData.getKind())
        .setStartEpochNanos(spanData.getStartEpochNanos())
        .setAttributes(spanData.getAttributes())
        .setEvents(spanData.getEvents())
        .setLinks(spanData.getLinks())
        .setStatus(spanData.getStatus())
        .setEndEpochNanos(spanData.getEndEpochNanos())
        .setHasRemoteParent(spanData.getHasRemoteParent())
        .setHasEnded(spanData.getHasEnded())
        .setTotalRecordedEvents(spanData.getTotalRecordedEvents())
        .setTotalRecordedLinks(spanData.getTotalRecordedLinks())
        .setTotalAttributeCount(spanData.getTotalAttributeCount());
  }

  @Override
  public final SpanData.Builder toBuilder() {
    return autoToBuilder();
  }

  abstract Builder autoToBuilder();

  // AutoValue won't generate equals that compares with SpanData interface.
  @Override
  public final boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof SpanData) {
      SpanData that = (SpanData) o;
      return getTraceId().equals(that.getTraceId())
          && getSpanId().equals(that.getSpanId())
          && getTraceFlags().equals(that.getTraceFlags())
          && getTraceState().equals(that.getTraceState())
          && getParentSpanId().equals(that.getParentSpanId())
          && getResource().equals(that.getResource())
          && getInstrumentationLibraryInfo().equals(that.getInstrumentationLibraryInfo())
          && getName().equals(that.getName())
          && getKind().equals(that.getKind())
          && getStartEpochNanos() == that.getStartEpochNanos()
          && getAttributes().equals(that.getAttributes())
          && getEvents().equals(that.getEvents())
          && getLinks().equals(that.getLinks())
          && getStatus().equals(that.getStatus())
          && getEndEpochNanos() == that.getEndEpochNanos()
          && getHasRemoteParent() == that.getHasRemoteParent()
          && getHasEnded() == that.getHasEnded()
          && getTotalRecordedEvents() == that.getTotalRecordedEvents()
          && getTotalRecordedLinks() == that.getTotalRecordedLinks()
          && getTotalAttributeCount() == that.getTotalAttributeCount();
    }
    return false;
  }

  /**
   * A {@code Builder} class for {@link SpanDataImpl}.
   *
   * @since 0.1.0
   */
  @AutoValue.Builder
  abstract static class Builder implements SpanData.Builder {

    @Override
    public final SpanData build() {
      return autoBuild();
    }

    abstract SpanDataImpl autoBuild();

    @Override
    public abstract Builder setTraceId(TraceId traceId);

    @Override
    public abstract Builder setSpanId(SpanId spanId);

    @Override
    public abstract Builder setTraceFlags(TraceFlags traceFlags);

    @Override
    public abstract Builder setTraceState(TraceState traceState);

    @Override
    public abstract Builder setParentSpanId(SpanId parentSpanId);

    @Override
    public abstract Builder setResource(Resource resource);

    @Override
    public abstract Builder setInstrumentationLibraryInfo(
        InstrumentationLibraryInfo instrumentationLibraryInfo);

    @Override
    public abstract Builder setName(String name);

    @Override
    public abstract Builder setStartEpochNanos(long epochNanos);

    @Override
    public abstract Builder setEndEpochNanos(long epochNanos);

    @Override
    public abstract Builder setAttributes(ReadableAttributes attributes);

    @Override
    public abstract Builder setEvents(List<Event> events);

    @Override
    public abstract Builder setStatus(Status status);

    @Override
    public abstract Builder setKind(Kind kind);

    @Override
    public abstract Builder setLinks(List<Link> links);

    @Override
    public abstract Builder setHasRemoteParent(boolean hasRemoteParent);

    @Override
    public abstract Builder setHasEnded(boolean hasEnded);

    @Override
    public abstract Builder setTotalRecordedEvents(int totalRecordedEvents);

    @Override
    public abstract Builder setTotalRecordedLinks(int totalRecordedLinks);

    @Override
    public abstract Builder setTotalAttributeCount(int totalAttributeCount);
  }
}
