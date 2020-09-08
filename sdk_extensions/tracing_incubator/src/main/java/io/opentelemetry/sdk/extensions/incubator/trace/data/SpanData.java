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

package io.opentelemetry.sdk.extensions.incubator.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceState;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A {@link io.opentelemetry.sdk.trace.data.SpanData} implementation with a builder that can be used
 * to modify parts of a {@link io.opentelemetry.sdk.trace.data.SpanData}.
 *
 * <pre>{@code
 * String clientType = ClientConfig.parseUserAgent(
 *   data.getAttributes().get(SemanticAttributes.HTTP_USER_AGENT).getStringValue());
 * Attributes newAttributes = Attributes.newBuilder(data.getAttributes())
 *   .setAttribute("client_type", clientType)
 *   .build();
 * data = io.opentelemetry.sdk.extensions.incubator.trace.data.SpanData.newBuilder(data)
 *   .setAttributes(newAttributes)
 *   .build();
 * exporter.export(data);
 *
 * }</pre>
 */
// AutoValue generated hashCode is fine but we need to define equals to accept the base SpanData
// type.
@Immutable
@AutoValue
public abstract class SpanData implements io.opentelemetry.sdk.trace.data.SpanData {

  /**
   * Returns a {@link SpanData.Builder} populated with the information in the provided {@link
   * io.opentelemetry.sdk.trace.data.SpanData}.
   */
  public static SpanData.Builder newBuilder(io.opentelemetry.sdk.trace.data.SpanData spanData) {
    return new AutoValue_SpanData.Builder()
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

  public final SpanData.Builder toBuilder() {
    return autoToBuilder();
  }

  abstract Builder autoToBuilder();

  // AutoValue won't generate equals that compares with SpanData interface but generates hash code
  // fine.
  @SuppressWarnings("EqualsHashCode")
  @Override
  public final boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (o instanceof io.opentelemetry.sdk.trace.data.SpanData) {
      io.opentelemetry.sdk.trace.data.SpanData that = (io.opentelemetry.sdk.trace.data.SpanData) o;
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
   * A {@code Builder} class for {@link SpanData}.
   *
   * @since 0.1.0
   */
  @AutoValue.Builder
  abstract static class Builder {

    public final SpanData build() {
      return autoBuild();
    }

    abstract SpanData autoBuild();

    public abstract Builder setTraceId(String traceId);

    public abstract Builder setSpanId(String spanId);

    public abstract Builder setTraceFlags(TraceFlags traceFlags);

    public abstract Builder setTraceState(TraceState traceState);

    public abstract Builder setParentSpanId(String parentSpanId);

    public abstract Builder setResource(Resource resource);

    public abstract Builder setInstrumentationLibraryInfo(
        InstrumentationLibraryInfo instrumentationLibraryInfo);

    public abstract Builder setName(String name);

    public abstract Builder setStartEpochNanos(long epochNanos);

    public abstract Builder setEndEpochNanos(long epochNanos);

    public abstract Builder setAttributes(ReadableAttributes attributes);

    public abstract Builder setEvents(List<Event> events);

    public abstract Builder setStatus(Status status);

    public abstract Builder setKind(Kind kind);

    public abstract Builder setLinks(List<Link> links);

    public abstract Builder setHasRemoteParent(boolean hasRemoteParent);

    public abstract Builder setHasEnded(boolean hasEnded);

    public abstract Builder setTotalRecordedEvents(int totalRecordedEvents);

    public abstract Builder setTotalRecordedLinks(int totalRecordedLinks);

    public abstract Builder setTotalAttributeCount(int totalAttributeCount);
  }
}
