/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlpjson;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class OtlpJsonLoggingSpanExporterTest {

  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("key", "value").build());

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
          .setInstrumentationLibraryInfo(InstrumentationLibraryInfo.create("instrumentation", "1"))
          .setResource(RESOURCE)
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
          .setResource(RESOURCE)
          .setInstrumentationLibraryInfo(InstrumentationLibraryInfo.create("instrumentation2", "2"))
          .build();

  SpanExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = OtlpJsonLoggingSpanExporter.create();
  }

  @Test
  void log() throws Exception {
    Logger logger = OtlpJsonLoggingSpanExporter.logger;
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
          .hasSize(1)
          .allSatisfy(log -> assertThat(log.getLevel()).isEqualTo(Level.INFO));
      JSONAssert.assertEquals(
          "{\n"
              + "  \"resource\": {\n"
              + "    \"attributes\": [{\n"
              + "      \"key\": \"key\",\n"
              + "      \"value\": {\n"
              + "        \"stringValue\": \"value\"\n"
              + "      }\n"
              + "    }]\n"
              + "  },\n"
              + "  \"instrumentationLibrarySpans\": [{\n"
              + "    \"instrumentationLibrary\": {\n"
              + "      \"name\": \"instrumentation2\",\n"
              + "      \"version\": \"2\"\n"
              + "    },\n"
              + "    \"spans\": [{\n"
              + "      \"traceId\": \"0000000000000014000000000000001e\",\n"
              + "      \"spanId\": \"000000000000000f\",\n"
              + "      \"name\": \"testSpan2\",\n"
              + "      \"kind\": \"SPAN_KIND_CLIENT\",\n"
              + "      \"startTimeUnixNano\": \"500\",\n"
              + "      \"endTimeUnixNano\": \"1501\",\n"
              + "      \"status\": {\n"
              + "        \"deprecatedCode\": \"DEPRECATED_STATUS_CODE_UNKNOWN_ERROR\",\n"
              + "        \"code\": \"STATUS_CODE_ERROR\"\n"
              + "      }\n"
              + "    }]\n"
              + "  }, {\n"
              + "    \"instrumentationLibrary\": {\n"
              + "      \"name\": \"instrumentation\",\n"
              + "      \"version\": \"1\"\n"
              + "    },\n"
              + "    \"spans\": [{\n"
              + "      \"traceId\": \"00000000000004d20000000000001a85\",\n"
              + "      \"spanId\": \"0000000000002694\",\n"
              + "      \"name\": \"testSpan1\",\n"
              + "      \"kind\": \"SPAN_KIND_INTERNAL\",\n"
              + "      \"startTimeUnixNano\": \"100\",\n"
              + "      \"endTimeUnixNano\": \"1100\",\n"
              + "      \"attributes\": [{\n"
              + "        \"key\": \"animal\",\n"
              + "        \"value\": {\n"
              + "          \"stringValue\": \"cat\"\n"
              + "        }\n"
              + "      }, {\n"
              + "        \"key\": \"lives\",\n"
              + "        \"value\": {\n"
              + "          \"intValue\": \"9\"\n"
              + "        }\n"
              + "      }],\n"
              + "      \"events\": [{\n"
              + "        \"timeUnixNano\": \"600\",\n"
              + "        \"name\": \"somethingHappenedHere\",\n"
              + "        \"attributes\": [{\n"
              + "          \"key\": \"important\",\n"
              + "          \"value\": {\n"
              + "            \"boolValue\": true\n"
              + "          }\n"
              + "        }]\n"
              + "      }],\n"
              + "      \"status\": {\n"
              + "        \"code\": \"STATUS_CODE_OK\"\n"
              + "      }\n"
              + "    }]\n"
              + "  }]\n"
              + "}",
          logged.get(0).getMessage(),
          /* strict= */ true);
    } finally {
      logger.removeHandler(handler);
      logger.setUseParentHandlers(true);
    }
  }

  @Test
  void flush() {
    assertThat(exporter.flush().isSuccess()).isTrue();
  }

  @Test
  void shutdown() {
    assertThat(exporter.shutdown().isSuccess()).isTrue();
  }
}
