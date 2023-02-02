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
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.event.Level;

@SuppressLogger(OtlpJsonLoggingSpanExporter.class)
class OtlpJsonLoggingSpanExporterTest {

  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("key", "value").build());

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
          .setAttributes(Attributes.of(stringKey("animal"), "cat", longKey("lives"), 9L))
          .setEvents(
              Collections.singletonList(
                  EventData.create(
                      100 + 500,
                      "somethingHappenedHere",
                      Attributes.of(booleanKey("important"), true))))
          .setTotalAttributeCount(2)
          .setTotalRecordedEvents(1)
          .setTotalRecordedLinks(0)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation")
                  .setVersion("1")
                  .setAttributes(Attributes.builder().put("key", "value").build())
                  .build())
          .setResource(RESOURCE)
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
          .setResource(RESOURCE)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation2").setVersion("2").build())
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
    String message = logs.getEvents().get(0).getMessage();
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
            + "  \"scopeSpans\": [{"
            + "    \"scope\": {"
            + "      \"name\": \"instrumentation2\","
            + "      \"version\": \"2\""
            + "    },"
            + "    \"spans\": [{"
            + "      \"traceId\": \"12340000000043211234000000004321\","
            + "      \"spanId\": \"8765000000005678\","
            + "      \"name\": \"testSpan2\","
            + "      \"kind\": 3,"
            + "      \"startTimeUnixNano\": \"500\","
            + "      \"endTimeUnixNano\": \"1501\","
            + "      \"status\": {"
            + "        \"code\": 2"
            + "      }"
            + "    }]"
            + "  }, {"
            + "    \"scope\": {"
            + "      \"name\": \"instrumentation\","
            + "      \"version\": \"1\","
            + "      \"attributes\":[{"
            + "        \"key\":\"key\","
            + "        \"value\":{"
            + "          \"stringValue\":\"value\""
            + "        }"
            + "      }]"
            + "    },"
            + "    \"spans\": [{"
            + "      \"traceId\": \"12345678876543211234567887654321\","
            + "      \"spanId\": \"8765432112345678\","
            + "      \"name\": \"testSpan1\","
            + "      \"kind\": 1,"
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
            + "        \"code\": 1"
            + "      }"
            + "    }]"
            + "  }]"
            + "}",
        message,
        /* strict= */ false);
    assertThat(message).doesNotContain("\n");
  }

  @Test
  void flush() {
    assertThat(exporter.flush().isSuccess()).isTrue();
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
}
