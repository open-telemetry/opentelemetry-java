/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporters.logging;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import io.opentelemetry.sdk.trace.data.SpanData.Status;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the {@link LoggingSpanExporter}. */
class LoggingSpanExporterTest {

  LoggingSpanExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = new LoggingSpanExporter();
  }

  @AfterEach
  void tearDown() {
    exporter.shutdown();
  }

  @Test
  void returnCode() {
    long epochNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    SpanData spanData =
        TestSpanData.builder()
            .setHasEnded(true)
            .setTraceId(TraceId.fromLongs(1234L, 6789L))
            .setSpanId(SpanId.fromLong(9876L))
            .setStartEpochNanos(epochNanos)
            .setEndEpochNanos(epochNanos + 1000)
            .setStatus(Status.ok())
            .setName("testSpan")
            .setKind(Kind.INTERNAL)
            .setEvents(
                Collections.singletonList(
                    Event.create(
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
