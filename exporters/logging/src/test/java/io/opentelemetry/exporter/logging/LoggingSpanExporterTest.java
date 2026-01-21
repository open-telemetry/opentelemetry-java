/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.api.common.AttributeKey.valueKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

@SuppressLogger(LoggingSpanExporter.class)
class LoggingSpanExporterTest {

  private static final SpanData SPAN1 =
      TestSpanData.builder()
          .setHasEnded(true)
          .setSpanContext(
              SpanContext.create(
                  "12345678876543211234567887654321",
                  "8765432112345678",
                  TraceFlags.getSampled(),
                  TraceState.getDefault()))
          .setStartEpochNanos(100)
          .setEndEpochNanos(100 + 1000)
          .setStatus(StatusData.ok())
          .setName("testSpan1")
          .setKind(SpanKind.INTERNAL)
          .setAttributes(
              Attributes.builder()
                  .put(stringKey("animal"), "cat")
                  .put(longKey("lives"), 9L)
                  .put(valueKey("bytes"), Value.of(new byte[] {1, 2, 3}))
                  .put(valueKey("map"), Value.of(KeyValue.of("nested", Value.of("value"))))
                  .put(valueKey("heterogeneousArray"), Value.of(Value.of("string"), Value.of(123L)))
                  .put(valueKey("empty"), Value.empty())
                  .build())
          .setEvents(
              Collections.singletonList(
                  EventData.create(
                      100 + 500,
                      "somethingHappenedHere",
                      Attributes.of(booleanKey("important"), true))))
          .setTotalRecordedEvents(1)
          .setTotalRecordedLinks(0)
          .setInstrumentationScopeInfo(InstrumentationScopeInfo.create("tracer1"))
          .build();

  private static final SpanData SPAN2 =
      TestSpanData.builder()
          .setHasEnded(false)
          .setSpanContext(
              SpanContext.create(
                  "12340000000043211234000000004321",
                  "8765000000005678",
                  TraceFlags.getSampled(),
                  TraceState.getDefault()))
          .setStartEpochNanos(500)
          .setEndEpochNanos(500 + 1001)
          .setStatus(StatusData.error())
          .setName("testSpan2")
          .setKind(SpanKind.CLIENT)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("tracer2").setVersion("1.0").build())
          .build();

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(LoggingSpanExporter.class);

  LoggingSpanExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = LoggingSpanExporter.create();
  }

  @Test
  void export() {
    assertThat(exporter.export(Arrays.asList(SPAN1, SPAN2)).isSuccess()).isTrue();

    assertThat(logs.getEvents())
        .hasSize(2)
        .allSatisfy(log -> assertThat(log.getLevel()).isEqualTo(Level.INFO));
    assertThat(logs.getEvents().get(0).getMessage())
        .isEqualTo(
            "'testSpan1' : 12345678876543211234567887654321 8765432112345678 "
                + "INTERNAL [tracer: tracer1:] "
                + "{animal=\"cat\", bytes=ValueBytes{AQID}, empty=ValueEmpty{}, "
                + "heterogeneousArray=ValueArray{[\"string\",123]}, lives=9, "
                + "map=KeyValueList{{\"nested\":\"value\"}}}");
    assertThat(logs.getEvents().get(1).getMessage())
        .isEqualTo(
            "'testSpan2' : 12340000000043211234000000004321 8765000000005678 "
                + "CLIENT [tracer: tracer2:1.0] {}");
  }

  @Test
  void flush() {
    AtomicBoolean flushed = new AtomicBoolean(false);
    Logger.getLogger(LoggingSpanExporter.class.getName())
        .addHandler(
            new StreamHandler(new PrintStream(new ByteArrayOutputStream()), new SimpleFormatter()) {
              @Override
              public synchronized void flush() {
                flushed.set(true);
              }
            });
    exporter.flush();
    assertThat(flushed.get()).isTrue();
  }

  @Test
  void shutdown() {
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    assertThat(
            exporter
                .export(Collections.singletonList(SPAN1))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    assertThat(logs.getEvents()).isEmpty();
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }

  @Test
  void stringRepresentation() {
    assertThat(LoggingSpanExporter.create().toString()).isEqualTo("LoggingSpanExporter{}");
  }
}
