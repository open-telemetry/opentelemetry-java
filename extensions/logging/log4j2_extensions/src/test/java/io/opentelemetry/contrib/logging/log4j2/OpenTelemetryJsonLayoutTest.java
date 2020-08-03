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

public class OpenTelemetryJsonLayoutTest {
  @Rule public LoggerContextRule init = new LoggerContextRule("OpenTelemetryJsonLayoutConfig.xml");
  private final Gson gson = new Gson();

  @Test
  public void testOpenTelemetryJsonLayoutDefaults() {
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
    assertThat(data.get("body")).isEqualTo("test");
    assertThat(data.get("name")).isEqualTo("DefaultJsonLogger");
    Map<?, ?> time = (Map<?, ?>) data.get("timestamp");
    double eventTime = (Double) time.get("millis");
    assertThat(eventTime - logTime).isLessThanOrEqualTo(100);
    assertThat(data.get("severitytext")).isEqualTo("WARN");
    assertThat(data.get("severitynumber")).isEqualTo(13.0);

    assertThat(data.get("traceid")).isNull();

    String second = messages.get(1);
    data = gson.fromJson(second, Map.class);
    assertThat(span.getContext().getTraceId().toLowerBase16()).isEqualTo(data.get("traceid"));
    assertThat(span.getContext().getSpanId().toLowerBase16()).isEqualTo(data.get("spanid"));
    assertThat(span.getContext().getTraceFlags().toLowerBase16()).isEqualTo(data.get("traceflags"));
  }
}
