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

package io.opentelemetry.contrib.logging.log4j2;

import com.google.gson.Gson;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.junit.LoggerContextRule;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class OpenTelemetryJsonLayoutTest {
  @Rule
  public LoggerContextRule init = new LoggerContextRule("OpenTelemetryJsonLayoutConfig.xml");
  private final Gson gson = new Gson();

  @Test
  public void testOTJsonLayoutDefaults() {
    Logger logger = init.getLogger("DefaultJsonLogger");
    ListAppender appender = init.getListAppender("Defaults");
    double logTime = System.currentTimeMillis();

    // First log outside a span
    logger.warn("test");

    // Now with an open span
    Tracer tracer = OpenTelemetry.getTracer("JsonLayoutTest");
    Span span = tracer.spanBuilder("a_span").startSpan();
    try (Scope scope = tracer.withSpan(span)) {
      logger.error("test 2");
    }

    List<String> messages = appender.getMessages();
    String first = messages.get(0);
    Map<?, ?> data = gson.fromJson(first, Map.class);
    assertEquals("test", data.get("body"));
    assertEquals("DefaultJsonLogger", data.get("name"));
    Map<?, ?> time = (Map<?,?>) data.get("timestamp");
    double eventTime = (Double) time.get("millis");
    assertTrue(eventTime - logTime < 100);
    assertEquals("WARN", data.get("severitytext"));
    assertEquals(13, ((Double) data.get("severitynumber")).intValue());
    assertNull(data.get("traceid"));


    String second = messages.get(1);
    data = gson.fromJson(second, Map.class);
    assertEquals(span.getContext().getTraceId().toLowerBase16(), data.get("traceid"));
    assertEquals(span.getContext().getSpanId().toLowerBase16(), data.get("spanid"));
    assertEquals(span.getContext().getTraceFlags().toLowerBase16(), data.get("traceflags"));

  }

}