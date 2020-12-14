/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class OtlpLoggingSpanExporterTest {
  private static final SpanData SPAN1 =
      TestSpanData.builder()
          .setHasEnded(true)
          .setTraceId(TraceId.fromLongs(1234L, 6789L))
          .setSpanId(SpanId.fromLong(9876L))
          .setStartEpochNanos(100)
          .setEndEpochNanos(100 + 1000)
          .setStatus(SpanData.Status.ok())
          .setName("testSpan1")
          .setKind(Span.Kind.INTERNAL)
          .setAttributes(Attributes.of(stringKey("animal"), "cat", longKey("lives"), 9L))
          .setEvents(
              Collections.singletonList(
                  SpanData.Event.create(
                      100 + 500,
                      "somethingHappenedHere",
                      Attributes.of(booleanKey("important"), true))))
          .setTotalAttributeCount(2)
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
          .setStatus(SpanData.Status.error())
          .setName("testSpan2")
          .setKind(Span.Kind.CLIENT)
          .build();

  OtlpLoggingSpanExporter exporter = new OtlpLoggingSpanExporter();

  @Test
  void log() throws Exception {
    Logger logger = OtlpLoggingSpanExporter.logger;
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
      JSONAssert.assertEquals(
          "{"
              + "  \"traceId\": \"00000000000004d20000000000001a85\","
              + "  \"spanId\": \"0000000000002694\","
              + "  \"name\": \"testSpan1\","
              + "  \"kind\": \"SPAN_KIND_INTERNAL\","
              + "  \"startTimeUnixNano\": \"100\","
              + "  \"endTimeUnixNano\": \"1100\","
              + "  \"attributes\": [{"
              + "    \"key\": \"animal\","
              + "    \"value\": {"
              + "      \"stringValue\": \"cat\""
              + "    }"
              + "  }, {"
              + "    \"key\": \"lives\","
              + "    \"value\": {"
              + "      \"intValue\": \"9\""
              + "    }"
              + "  }],"
              + "  \"events\": [{"
              + "    \"timeUnixNano\": \"600\","
              + "    \"name\": \"somethingHappenedHere\","
              + "    \"attributes\": [{"
              + "      \"key\": \"important\","
              + "      \"value\": {"
              + "        \"boolValue\": true"
              + "      }"
              + "    }]"
              + "  }],"
              + "  \"status\": {"
              + "    \"code\": \"STATUS_CODE_OK\""
              + "  }"
              + "}",
          logged.get(0).getMessage(),
          /* strict= */ true);
      JSONAssert.assertEquals(
          "{"
              + "      \"traceId\": \"0000000000000014000000000000001e\","
              + "      \"spanId\": \"000000000000000f\","
              + "      \"name\": \"testSpan2\","
              + "      \"kind\": \"SPAN_KIND_CLIENT\","
              + "      \"startTimeUnixNano\": \"500\","
              + "      \"endTimeUnixNano\": \"1501\","
              + "      \"status\": {"
              + "        \"deprecatedCode\": \"DEPRECATED_STATUS_CODE_UNKNOWN_ERROR\","
              + "        \"code\": \"STATUS_CODE_ERROR\""
              + "      }"
              + "    }",
          logged.get(1).getMessage(),
          /* strict= */ true);
    } finally {
      logger.removeHandler(handler);
      logger.setUseParentHandlers(true);
    }
  }
}
