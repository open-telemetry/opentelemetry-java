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

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Event;
import io.opentelemetry.sdk.trace.data.SpanData.Status;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the {@link LoggingSpanExporter}. */
class LoggingSpanExporterTest {

  private static final SpanData SPAN1 =
      TestSpanData.builder()
          .setHasEnded(true)
          .setTraceId(TraceId.fromLongs(1234L, 6789L))
          .setSpanId(SpanId.fromLong(9876L))
          .setStartEpochNanos(100)
          .setEndEpochNanos(100 + 1000)
          .setStatus(Status.ok())
          .setName("testSpan1")
          .setKind(Kind.INTERNAL)
          .setAttributes(Attributes.of(stringKey("animal"), "cat", longKey("lives"), 9L))
          .setEvents(
              Collections.singletonList(
                  Event.create(
                      100 + 500,
                      "somethingHappenedHere",
                      Attributes.of(booleanKey("important"), true))))
          .setTotalRecordedEvents(1)
          .setTotalRecordedLinks(0)
          .build();

  private static final SpanData SPAN2 =
      TestSpanData.builder()
          .setHasEnded(false)
          .setTraceId(TraceId.fromLongs(20L, 30L))
          .setSpanId(SpanId.fromLong(15L))
          .setStartEpochNanos(500)
          .setEndEpochNanos(500 + 1001)
          .setStatus(Status.error())
          .setName("testSpan2")
          .setKind(Kind.CLIENT)
          .build();

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
  void log() {
    Logger logger = LoggingSpanExporter.logger;
    List<LogRecord> logged = new ArrayList<>();
    Handler handler =
        new Handler() {
          @Override
          public void publish(LogRecord record) {
            logged.add(record);
          }

          @Override
          public void flush() {}

          @Override
          public void close() {}
        };
    logger.addHandler(handler);
    logger.setUseParentHandlers(false);
    try {
      exporter.export(Arrays.asList(SPAN1, SPAN2));

      assertThat(logged)
          .hasSize(2)
          .allSatisfy(log -> assertThat(log.getLevel()).isEqualTo(Level.INFO));
      assertThat(logged.get(0).getMessage())
          .isEqualTo(
              "testSpan1 00000000000004d20000000000001a85 0000000000002694 "
                  + "{animal=\"cat\", lives=9}");
      assertThat(logged.get(1).getMessage())
          .isEqualTo("testSpan2 0000000000000014000000000000001e 000000000000000f {}");
    } finally {
      logger.removeHandler(handler);
      logger.setUseParentHandlers(true);
    }
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
