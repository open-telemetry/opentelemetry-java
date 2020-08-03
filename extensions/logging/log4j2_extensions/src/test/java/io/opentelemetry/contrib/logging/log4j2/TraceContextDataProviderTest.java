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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.junit.LoggerContextRule;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.junit.Rule;
import org.junit.Test;

public class TraceContextDataProviderTest {
  @Rule public LoggerContextRule init = new LoggerContextRule("ContextDataProviderTestConfig.xml");

  @Test
  public void testLayoutWrapperSync() {
    final Logger logger = init.getLogger("SyncDataProviderTest");
    final ListAppender appender = init.getListAppender("SyncList");
    logger.warn("Test message");
    Tracer tracer = OpenTelemetry.getTracerProvider().get("ot_trace_dataprovider_test");
    Span span = tracer.spanBuilder("dataprovider_test").startSpan();
    String traceId = span.getContext().getTraceId().toLowerBase16();
    try (Scope scope = tracer.withSpan(span)) {
      logger.warn("hello");
    }
    final List<String> events = appender.getMessages();
    assertThat(events.size()).isEqualTo(2);
    String withTrace = events.get(1);
    assertThat(withTrace).contains(traceId);

    String withoutTrace = events.get(0);
    assertThat(withoutTrace).contains("traceid=''");
  }

  @Test
  public void testLayoutWrapperAsync() throws InterruptedException {
    final Logger logger = init.getLogger("AsyncContextDataProviderTest");
    final ListAppender appender = init.getListAppender("AsyncList");
    Tracer tracer = OpenTelemetry.getTracerProvider().get("ot_trace_lookup_test");
    Span span = tracer.spanBuilder("lookup_test").startSpan();
    String traceId = span.getContext().getTraceId().toLowerBase16();
    try (Scope scope = tracer.withSpan(span)) {
      logger.warn("hello");
    }
    Thread.sleep(15); // Default wait for log4j is 10ms
    final List<String> events = appender.getMessages();
    assertThat(events.size()).isEqualTo(1);
    String withTrace = events.get(0);

    assertThat(withTrace).contains(String.format("traceid='%s'", traceId));
  }

  @Test
  public void testLayoutWrapperJson() {
    final Logger logger = init.getLogger("JsonContextDataProviderTest");
    final ListAppender appender = init.getListAppender("JsonList");
    Tracer tracer = OpenTelemetry.getTracerProvider().get("ot_trace_lookup_test");
    Span span = tracer.spanBuilder("lookup_test").startSpan();
    String traceId = span.getContext().getTraceId().toLowerBase16();
    String spanId = span.getContext().getSpanId().toLowerBase16();
    try (Scope scope = tracer.withSpan(span)) {
      logger.warn("hello");
    }
    final List<String> events = appender.getMessages();
    assertThat(events.size()).isEqualTo(1);
    String withTrace = events.get(0);

    Gson gson = new Gson();
    @SuppressWarnings("unchecked")
    Map<String, String> parsed = (Map<String, String>) gson.fromJson(withTrace, Map.class);
    assertThat(parsed).containsEntry("traceid", traceId);
    assertThat(parsed).containsEntry("spanid", spanId);
    assertThat(parsed).containsEntry("traceflags", "01");
  }
}
