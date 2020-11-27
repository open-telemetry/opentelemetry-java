/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.ReadableAttributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;

/** Assertions for an exported {@link SpanData}. */
public class SpanDataAssert extends AbstractAssert<SpanDataAssert, SpanData> {

  SpanDataAssert(SpanData actual) {
    super(actual, SpanDataAssert.class);
  }

  /** Asserts the span has the given trace ID. */
  public SpanDataAssert hasTraceId(String traceId) {
    isNotNull();
    if (!actual.getTraceId().equals(traceId)) {
      failWithActualExpectedAndMessage(
          actual.getTraceId(),
          traceId,
          "Expected span [%s] to have trace ID <%s> but was <%s>",
          actual.getName(),
          traceId,
          actual.getTraceId());
    }
    return this;
  }

  /** Asserts the span has the given span ID. */
  public SpanDataAssert hasSpanId(String spanId) {
    isNotNull();
    if (!actual.getSpanId().equals(spanId)) {
      failWithActualExpectedAndMessage(
          actual.getSpanId(),
          spanId,
          "Expected span [%s] to have span ID <%s> but was <%s>",
          actual.getName(),
          spanId,
          actual.getSpanId());
    }
    return this;
  }

  /** Asserts the span is sampled. */
  public SpanDataAssert isSampled() {
    isNotNull();
    if (!actual.isSampled()) {
      failWithMessage("Expected span [%s] to be sampled but was not.", actual.getName());
    }
    return this;
  }

  /** Asserts the span is not sampled. */
  public SpanDataAssert isNotSampled() {
    isNotNull();
    if (actual.isSampled()) {
      failWithMessage("Expected span [%s] to not be sampled but it was.", actual.getName());
    }
    return this;
  }

  /** Asserts the span has the given {@link TraceState}. */
  public SpanDataAssert hasTraceState(TraceState traceState) {
    isNotNull();
    if (!actual.getTraceState().equals(traceState)) {
      failWithActualExpectedAndMessage(
          actual.getTraceState(),
          traceState,
          "Expected span [%s] to have trace state <%s> but was <%s>",
          actual.getName(),
          traceState,
          actual.getTraceState());
    }
    return this;
  }

  /** Asserts the span has the given parent span ID. */
  public SpanDataAssert hasParentSpanId(String parentSpanId) {
    isNotNull();
    if (!actual.getParentSpanId().equals(parentSpanId)) {
      failWithActualExpectedAndMessage(
          actual.getParentSpanId(),
          parentSpanId,
          "Expected span [%s] to have parent span ID <%s> but was <%s>",
          actual.getName(),
          parentSpanId,
          actual.getParentSpanId());
    }
    return this;
  }

  /** Asserts the span has the given {@link Resource}. */
  public SpanDataAssert hasResource(Resource resource) {
    isNotNull();
    if (!actual.getResource().equals(resource)) {
      failWithActualExpectedAndMessage(
          actual.getResource(),
          resource,
          "Expected span [%s] to have resource <%s> but was <%s>",
          actual.getName(),
          resource,
          actual.getResource());
    }
    return this;
  }

  /** Asserts the span has the given {@link InstrumentationLibraryInfo}. */
  public SpanDataAssert hasInstrumentationLibraryInfo(
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    isNotNull();
    if (!actual.getInstrumentationLibraryInfo().equals(instrumentationLibraryInfo)) {
      failWithActualExpectedAndMessage(
          actual.getInstrumentationLibraryInfo(),
          instrumentationLibraryInfo,
          "Expected span [%s] to have instrumentation library info <%s> but was <%s>",
          actual.getName(),
          instrumentationLibraryInfo,
          actual.getInstrumentationLibraryInfo());
    }
    return this;
  }

  /** Asserts the span has the given name. */
  public SpanDataAssert hasName(String name) {
    isNotNull();
    if (!actual.getName().equals(name)) {
      failWithActualExpectedAndMessage(
          actual.getName(),
          name,
          "Expected span to have name <%s> but was <%s>",
          name,
          actual.getName());
    }
    return this;
  }

  /** Asserts the span has the given kind. */
  public SpanDataAssert hasKind(Span.Kind kind) {
    isNotNull();
    if (!actual.getKind().equals(kind)) {
      failWithActualExpectedAndMessage(
          actual.getKind(),
          kind,
          "Expected span [%s] to have kind <%s> but was <%s>",
          actual.getName(),
          kind,
          actual.getKind());
    }
    return this;
  }

  /** Asserts the span starts at the given epoch timestamp, in nanos. */
  public SpanDataAssert startsAt(long startEpochNanos) {
    isNotNull();
    if (actual.getStartEpochNanos() != startEpochNanos) {
      failWithActualExpectedAndMessage(
          actual.getStartEpochNanos(),
          startEpochNanos,
          "Expected span [%s] to have start epoch <%s> nanos but was <%s>",
          actual.getName(),
          startEpochNanos,
          actual.getStartEpochNanos());
    }
    return this;
  }

  /** Asserts the span starts at the given epoch timestamp. */
  @SuppressWarnings("PreferJavaTimeOverload")
  public SpanDataAssert startsAt(long startEpoch, TimeUnit unit) {
    return startsAt(unit.toNanos(startEpoch));
  }

  /** Asserts the span starts at the given epoch timestamp. */
  public SpanDataAssert startsAt(Instant timestamp) {
    return startsAt(toNanos(timestamp));
  }

  /** Asserts the span has the given attributes. */
  public SpanDataAssert hasAttributes(ReadableAttributes attributes) {
    isNotNull();
    if (!actual.getAttributes().equals(attributes)) {
      failWithActualExpectedAndMessage(
          actual.getAttributes(),
          attributes,
          "Expected span [%s] to have attributes <%s> but was <%s>",
          actual.getName(),
          attributes,
          actual.getAttributes());
    }
    return this;
  }

  /** Asserts the span has attributes satisfying the given condition. */
  public SpanDataAssert hasAttributesSatisfying(Consumer<ReadableAttributes> attributes) {
    isNotNull();
    assertThat(actual.getAttributes()).as("attributes").satisfies(attributes);
    return this;
  }

  /** Asserts the span has the given events. */
  public SpanDataAssert hasEvents(Iterable<SpanData.Event> events) {
    isNotNull();
    assertThat(actual.getEvents())
        .withFailMessage(
            "Expected span [%s] to have events <%s> but was <%s>",
            actual.getName(), events, actual.getEvents())
        .containsExactlyInAnyOrderElementsOf(events);
    return this;
  }

  /** Asserts the span has the given events. */
  public SpanDataAssert hasEvents(SpanData.Event... events) {
    return hasEvents(Arrays.asList(events));
  }

  /** Asserts the span has events satisfying the given condition. */
  public SpanDataAssert hasEventsSatisfying(Consumer<List<? extends SpanData.Event>> condition) {
    isNotNull();
    assertThat(actual.getEvents()).satisfies(condition);
    return this;
  }

  /** Asserts the span has the given links. */
  public SpanDataAssert hasLinks(Iterable<SpanData.Link> links) {
    isNotNull();
    assertThat(actual.getLinks())
        .withFailMessage(
            "Expected span [%s] to have links <%s> but was <%s>",
            actual.getName(), links, actual.getLinks())
        .containsExactlyInAnyOrderElementsOf(links);
    return this;
  }

  /** Asserts the span has the given links. */
  public SpanDataAssert hasLinks(SpanData.Link... links) {
    return hasLinks(Arrays.asList(links));
  }

  /** Asserts the span has events satisfying the given condition. */
  public SpanDataAssert hasLinksSatisfying(Consumer<List<? extends SpanData.Link>> condition) {
    isNotNull();
    assertThat(actual.getLinks()).satisfies(condition);
    return this;
  }

  /** Asserts the span has the given {@link SpanData.Status}. */
  public SpanDataAssert hasStatus(SpanData.Status status) {
    isNotNull();
    if (!actual.getStatus().equals(status)) {
      failWithActualExpectedAndMessage(
          actual.getStatus(),
          status,
          "Expected span [%s] to have status <%s> but was <%s>",
          actual.getName(),
          status,
          actual.getStatus());
    }
    return this;
  }

  /** Asserts the span ends at the given epoch timestamp, in nanos. */
  public SpanDataAssert endsAt(long endEpochNanos) {
    isNotNull();
    if (actual.getEndEpochNanos() != endEpochNanos) {
      failWithActualExpectedAndMessage(
          actual.getEndEpochNanos(),
          endEpochNanos,
          "Expected span [%s] to have end epoch <%s> nanos but was <%s>",
          actual.getName(),
          endEpochNanos,
          actual.getEndEpochNanos());
    }
    return this;
  }

  /** Asserts the span ends at the given epoch timestamp. */
  @SuppressWarnings("PreferJavaTimeOverload")
  public SpanDataAssert endsAt(long startEpoch, TimeUnit unit) {
    return endsAt(unit.toNanos(startEpoch));
  }

  /** Asserts the span ends at the given epoch timestamp. */
  public SpanDataAssert endsAt(Instant timestamp) {
    return endsAt(toNanos(timestamp));
  }

  /** Asserts the span has ended. */
  public SpanDataAssert hasEnded() {
    isNotNull();
    if (!actual.hasEnded()) {
      failWithMessage("Expected span [%s] to have ended but did not", actual.getName());
    }
    return this;
  }

  /** Asserts the span has not ended. */
  public SpanDataAssert hasNotEnded() {
    isNotNull();
    if (actual.hasEnded()) {
      failWithMessage("Expected span [%s] to have not ended but did has", actual.getName());
    }
    return this;
  }

  /** Asserts the span has the given total recorded events. */
  public SpanDataAssert hasTotalRecordedEvents(int totalRecordedEvents) {
    isNotNull();
    if (actual.getTotalRecordedEvents() != totalRecordedEvents) {
      failWithActualExpectedAndMessage(
          actual.getTotalRecordedEvents(),
          totalRecordedEvents,
          "Expected span [%s] to have recorded <%s> total events but did not",
          actual.getName(),
          totalRecordedEvents,
          actual.getTotalRecordedEvents());
    }
    return this;
  }

  /** Asserts the span has the given total recorded links. */
  public SpanDataAssert hasTotalRecordedLinks(int totalRecordedLinks) {
    isNotNull();
    if (actual.getTotalRecordedLinks() != totalRecordedLinks) {
      failWithActualExpectedAndMessage(
          actual.getTotalRecordedLinks(),
          totalRecordedLinks,
          "Expected span [%s] to have recorded <%s> total links but did not",
          actual.getName(),
          totalRecordedLinks,
          actual.getTotalRecordedLinks());
    }
    return this;
  }

  /** Asserts the span has the given total attributes. */
  public SpanDataAssert hasTotalAttributeCount(int totalAttributeCount) {
    isNotNull();
    if (actual.getTotalAttributeCount() != totalAttributeCount) {
      failWithActualExpectedAndMessage(
          actual.getTotalAttributeCount(),
          totalAttributeCount,
          "Expected span [%s] to have recorded <%s> total attributes but did not",
          actual.getName(),
          totalAttributeCount,
          actual.getTotalAttributeCount());
    }
    return this;
  }

  private static long toNanos(Instant timestamp) {
    return TimeUnit.SECONDS.toNanos(timestamp.getEpochSecond()) + timestamp.getNano();
  }
}
