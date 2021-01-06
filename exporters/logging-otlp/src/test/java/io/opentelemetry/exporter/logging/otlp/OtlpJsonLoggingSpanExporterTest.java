/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.event.Level;

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

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OtlpJsonLoggingSpanExporter.class);

  SpanExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = OtlpJsonLoggingSpanExporter.create();
  }

  @Test
  void log() throws Exception {
    exporter.export(Arrays.asList(SPAN1, SPAN2));

    assertThat(logs.getEvents())
        .hasSize(1)
        .allSatisfy(log -> assertThat(log.getLevel()).isEqualTo(Level.INFO));
    JSONAssert.assertEquals(
        "{"
            + "  \"resource\": {"
            + "    \"attributes\": [{"
            + "      \"key\": \"key\","
            + "      \"value\": {"
            + "        \"stringValue\": \"value\""
            + "      }"
            + "    }]"
            + "  },"
            + "  \"instrumentationLibrarySpans\": [{"
            + "    \"instrumentationLibrary\": {"
            + "      \"name\": \"instrumentation2\","
            + "      \"version\": \"2\""
            + "    },"
            + "    \"spans\": [{"
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
            + "    }]"
            + "  }, {"
            + "    \"instrumentationLibrary\": {"
            + "      \"name\": \"instrumentation\","
            + "      \"version\": \"1\""
            + "    },"
            + "    \"spans\": [{"
            + "      \"traceId\": \"00000000000004d20000000000001a85\","
            + "      \"spanId\": \"0000000000002694\","
            + "      \"name\": \"testSpan1\","
            + "      \"kind\": \"SPAN_KIND_INTERNAL\","
            + "      \"startTimeUnixNano\": \"100\","
            + "      \"endTimeUnixNano\": \"1100\","
            + "      \"attributes\": [{"
            + "        \"key\": \"animal\","
            + "        \"value\": {"
            + "          \"stringValue\": \"cat\""
            + "        }"
            + "      }, {"
            + "        \"key\": \"lives\","
            + "        \"value\": {"
            + "          \"intValue\": \"9\""
            + "        }"
            + "      }],"
            + "      \"events\": [{"
            + "        \"timeUnixNano\": \"600\","
            + "        \"name\": \"somethingHappenedHere\","
            + "        \"attributes\": [{"
            + "          \"key\": \"important\","
            + "          \"value\": {"
            + "            \"boolValue\": true"
            + "          }"
            + "        }]"
            + "      }],"
            + "      \"status\": {"
            + "        \"code\": \"STATUS_CODE_OK\""
            + "      }"
            + "    }]"
            + "  }]"
            + "}",
        logs.getEvents().get(0).getMessage(),
        /* strict= */ true);
    assertThat(logs.getEvents().get(0).getMessage()).doesNotContain("\n");
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
