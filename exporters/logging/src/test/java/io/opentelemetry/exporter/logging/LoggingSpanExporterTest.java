/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

/** Tests for the {@link LoggingSpanExporter}. */
class LoggingSpanExporterTest {

  private static final SpanData SPAN1 =
      TestSpanData.builder()
          .setHasEnded(true)
          .setSpanContext(
              SpanContext.create(
                  TraceId.fromLongs(1234L, 6789L),
                  SpanId.fromLong(9876L),
                  TraceFlags.getSampled(),
                  TraceState.getDefault()))
          .setStartEpochNanos(100)
          .setEndEpochNanos(100 + 1000)
          .setStatus(StatusData.ok())
          .setName("testSpan1")
          .setKind(Kind.INTERNAL)
          .setAttributes(Attributes.of(stringKey("animal"), "cat", longKey("lives"), 9L))
          .setEvents(
              Collections.singletonList(
                  EventData.create(
                      100 + 500,
                      "somethingHappenedHere",
                      Attributes.of(booleanKey("important"), true))))
          .setTotalRecordedEvents(1)
          .setTotalRecordedLinks(0)
          .setInstrumentationLibraryInfo(InstrumentationLibraryInfo.create("tracer1", null))
          .build();

  private static final SpanData SPAN2 =
      TestSpanData.builder()
          .setHasEnded(false)
          .setSpanContext(
              SpanContext.create(
                  TraceId.fromLongs(20L, 30L),
                  SpanId.fromLong(15L),
                  TraceFlags.getSampled(),
                  TraceState.getDefault()))
          .setStartEpochNanos(500)
          .setEndEpochNanos(500 + 1001)
          .setStatus(StatusData.error())
          .setName("testSpan2")
          .setKind(Kind.CLIENT)
          .setInstrumentationLibraryInfo(InstrumentationLibraryInfo.create("tracer2", "1.0"))
          .build();

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(LoggingSpanExporter.class);

  LoggingSpanExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = new LoggingSpanExporter();
  }

  @AfterEach
  void tearDown() {
    exporter.close();
  }

  @Test
  void log() {
    exporter.export(Arrays.asList(SPAN1, SPAN2));

    assertThat(logs.getEvents())
        .hasSize(2)
        .allSatisfy(log -> assertThat(log.getLevel()).isEqualTo(Level.INFO));
    assertThat(logs.getEvents().get(0).getMessage())
        .isEqualTo(
            "'testSpan1' : 00000000000004d20000000000001a85 0000000000002694 "
                + "INTERNAL [tracer: tracer1:] "
                + "{animal=\"cat\", lives=9}");
    assertThat(logs.getEvents().get(1).getMessage())
        .isEqualTo(
            "'testSpan2' : 0000000000000014000000000000001e 000000000000000f "
                + "CLIENT [tracer: tracer2:1.0] {}");
  }

  @Test
  void returnCode() {
    long epochNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    SpanData spanData =
        TestSpanData.builder()
            .setHasEnded(true)
            .setSpanContext(
                SpanContext.create(
                    TraceId.fromLongs(1234L, 6789L),
                    SpanId.fromLong(9876L),
                    TraceFlags.getSampled(),
                    TraceState.getDefault()))
            .setStartEpochNanos(epochNanos)
            .setEndEpochNanos(epochNanos + 1000)
            .setStatus(StatusData.ok())
            .setName("testSpan")
            .setKind(Kind.INTERNAL)
            .setEvents(
                Collections.singletonList(
                    EventData.create(
                        epochNanos + 500,
                        "somethingHappenedHere",
                        Attributes.of(booleanKey("important"), true))))
            .setTotalRecordedEvents(1)
            .setTotalRecordedLinks(0)
            .build();
    CompletableResultCode resultCode = exporter.export(singletonList(spanData));
    assertThat(resultCode.isSuccess()).isTrue();
  }

  @Test
  void testFlush() {
    final AtomicBoolean flushed = new AtomicBoolean(false);
    Logger.getLogger(LoggingSpanExporter.class.getName())
        .addHandler(
            new StreamHandler(System.err, new SimpleFormatter()) {
              @Override
              public synchronized void flush() {
                flushed.set(true);
              }
            });
    exporter.flush();
    assertThat(flushed.get()).isTrue();
  }
}
