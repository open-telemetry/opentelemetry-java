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

/**
 * Immutable class that stores {@link SpanData} based on a {@link RecordEventsReadableSpan}.
 *
 * <p>This class stores a reference to a mutable {@link RecordEventsReadableSpan} ({@code delegate})
 * which it uses only the immutable parts from, and a copy of all the mutable parts.
 *
 * <p>When adding a new field to {@link RecordEventsReadableSpan}, store a copy if and only if the
 * field is mutable in the {@link RecordEventsReadableSpan}. Otherwise retrieve it from the
 * referenced {@link RecordEventsReadableSpan}.
 */
@Immutable
@AutoValue
abstract class SpanWrapper implements SpanData {
  abstract RecordEventsReadableSpan delegate();

  abstract List<Link> resolvedLinks();

  abstract List<Event> resolvedEvents();

  abstract ReadableAttributes attributes();

  abstract int totalAttributeCount();

  abstract int totalRecordedEvents();

  abstract Status status();

  abstract String name();

  abstract long endEpochNanos();

  abstract boolean hasEnded();

  /**
   * Note: the collections that are passed into this creator method are assumed to be immutable to
   * preserve the overall immutability of the class.
   */
  static SpanWrapper create(
      RecordEventsReadableSpan delegate,
      List<Link> links,
      List<Event> events,
      ReadableAttributes attributes,
      int totalAttributeCount,
      int totalRecordedEvents,
      Status status,
      String name,
      long endEpochNanos,
      boolean hasEnded) {
    return new AutoValue_SpanWrapper(
        delegate,
        links,
        events,
        attributes,
        totalAttributeCount,
        totalRecordedEvents,
        status,
        name,
        endEpochNanos,
        hasEnded);
  }

  @Override
  public TraceId getTraceId() {
    return delegate().getContext().getTraceId();
  }

  @Override
  public SpanId getSpanId() {
    return delegate().getContext().getSpanId();
  }

  @Override
  public TraceFlags getTraceFlags() {
    return delegate().getSpanContext().getTraceFlags();
  }

  @Override
  public TraceState getTraceState() {
    return delegate().getSpanContext().getTraceState();
  }

  @Override
  public SpanId getParentSpanId() {
    return delegate().getParentSpanId();
  }

  @Override
  public Resource getResource() {
    return delegate().getResource();
  }

  @Override
  public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return delegate().getInstrumentationLibraryInfo();
  }

  @Override
  public String getName() {
    return name();
  }

  @Override
  public Kind getKind() {
    return delegate().getKind();
  }

  @Override
  public long getStartEpochNanos() {
    return delegate().getStartEpochNanos();
  }

  @Override
  public ReadableAttributes getAttributes() {
    return attributes();
  }

  @Override
  public List<Event> getEvents() {
    return resolvedEvents();
  }

  @Override
  public List<Link> getLinks() {
    return resolvedLinks();
  }

  @Override
  public Status getStatus() {
    return status();
  }

  @Override
  public long getEndEpochNanos() {
    return endEpochNanos();
  }

  @Override
  public boolean getHasRemoteParent() {
    return delegate().hasRemoteParent();
  }

  @Override
  public boolean getHasEnded() {
    return hasEnded();
  }

  @Override
  public int getTotalRecordedEvents() {
    return totalRecordedEvents();
  }

  @Override
  public int getTotalRecordedLinks() {
    return delegate().getTotalRecordedLinks();
  }

  @Override
  public int getTotalAttributeCount() {
    return totalAttributeCount();
  }
}
