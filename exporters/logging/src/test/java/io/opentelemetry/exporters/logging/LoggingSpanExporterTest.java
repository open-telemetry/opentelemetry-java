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

import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Tests for the {@link LoggingSpanExporter}. */
public class LoggingSpanExporterTest {

  LoggingSpanExporter exporter;

  @Before
  public void setUp() throws Exception {
    exporter = new LoggingSpanExporter();
  }

  @After
  public void tearDown() throws Exception {
    exporter.shutdown();
  }

  @Test
  public void returnCode() {
    long epochNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    SpanData spanData =
        SpanData.newBuilder()
            .setHasEnded(true)
            .setTraceId(new TraceId(1234L, 6789L))
            .setSpanId(new SpanId(9876L))
            .setStartEpochNanos(epochNanos)
            .setEndEpochNanos(epochNanos + 1000)
            .setStatus(Status.OK)
            .setName("testSpan")
            .setKind(Kind.INTERNAL)
            .setTimedEvents(
                singletonList(
                    SpanData.TimedEvent.create(
                        epochNanos + 500,
                        "somethingHappenedHere",
                        singletonMap("important", AttributeValue.booleanAttributeValue(true)))))
            .setTotalRecordedEvents(1)
            .setTotalRecordedLinks(0)
            .build();
    ResultCode resultCode = exporter.export(singletonList(spanData));
    assertEquals(ResultCode.SUCCESS, resultCode);
  }

  @Test
  public void testFlush() {
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
