/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.exporters.logging;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.sdk.trace.data.EventImpl;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.test.TestSpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
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
        TestSpanData.newBuilder()
            .setHasEnded(true)
            .setTraceId(new TraceId(1234L, 6789L))
            .setSpanId(new SpanId(9876L))
            .setStartEpochNanos(epochNanos)
            .setEndEpochNanos(epochNanos + 1000)
            .setStatus(Status.OK)
            .setName("testSpan")
            .setKind(Kind.INTERNAL)
            .setEvents(
                Collections.singletonList(
                    EventImpl.create(
                        epochNanos + 500,
                        "somethingHappenedHere",
                        Attributes.of("important", AttributeValue.booleanAttributeValue(true)))))
            .setTotalRecordedEvents(1)
            .setTotalRecordedLinks(0)
            .build();
    ResultCode resultCode = exporter.export(singletonList(spanData));
    assertEquals(ResultCode.SUCCESS, resultCode);
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
