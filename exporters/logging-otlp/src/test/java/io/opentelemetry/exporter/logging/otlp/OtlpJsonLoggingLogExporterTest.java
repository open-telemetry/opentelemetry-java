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
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.event.Level;

class OtlpJsonLoggingLogExporterTest {

  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("key", "value").build());

  private static final LogData LOG1 =
      LogData.builder(RESOURCE, InstrumentationLibraryInfo.create("instrumentation", "1"))
          .setName("testLog1")
          .setBody("body1")
          .setTraceFlags(TraceFlags.getDefault())
          .setSeverity(Severity.INFO)
          .setSeverityText("INFO")
          .setSpanId("8765432112345876")
          .setTraceId("12345678876543211234567887654322")
          .setEpoch(1631533710L, TimeUnit.MILLISECONDS)
          .setAttributes(Attributes.of(stringKey("animal"), "cat", longKey("lives"), 9L))
          .build();

  private static final LogData LOG2 =
      LogData.builder(RESOURCE, InstrumentationLibraryInfo.create("instrumentation2", "2"))
          .setName("testLog2")
          .setBody("body2")
          .setTraceFlags(TraceFlags.getDefault())
          .setSeverity(Severity.INFO)
          .setSeverityText("INFO")
          .setSpanId("8765432112345875")
          .setTraceId("12345678876543211234567887654322")
          .setEpoch(1631533710L, TimeUnit.MILLISECONDS)
          .setAttributes(Attributes.of(booleanKey("important"), true))
          .build();

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OtlpJsonLoggingLogExporter.class);

  LogExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = OtlpJsonLoggingLogExporter.create();
  }

  @Test
  void log() throws Exception {
    exporter.export(Arrays.asList(LOG1, LOG2));

    assertThat(logs.getEvents())
        .hasSize(1)
        .allSatisfy(log -> assertThat(log.getLevel()).isEqualTo(Level.INFO));
    JSONAssert.assertEquals(
        "{\n"
            + "   \"resource\":{\n"
            + "      \"attributes\":[\n"
            + "         {\n"
            + "            \"key\":\"key\",\n"
            + "            \"value\":{\n"
            + "               \"stringValue\":\"value\"\n"
            + "            }\n"
            + "         }\n"
            + "      ]\n"
            + "   },\n"
            + "   \"instrumentationLibraryLogs\":[\n"
            + "      {\n"
            + "         \"instrumentationLibrary\":{\n"
            + "            \"name\":\"instrumentation2\",\n"
            + "            \"version\":\"2\"\n"
            + "         },\n"
            + "         \"logs\":[\n"
            + "            {\n"
            + "               \"timeUnixNano\":\"1631533710000000\",\n"
            + "               \"severityNumber\":\"SEVERITY_NUMBER_INFO\",\n"
            + "               \"severityText\":\"INFO\",\n"
            + "               \"name\":\"testLog2\",\n"
            + "               \"body\":{\n"
            + "                  \"stringValue\":\"body2\"\n"
            + "               },\n"
            + "               \"attributes\":[\n"
            + "                  {\n"
            + "                     \"key\":\"important\",\n"
            + "                     \"value\":{\n"
            + "                        \"boolValue\":true\n"
            + "                     }\n"
            + "                  }\n"
            + "               ],\n"
            + "               \"traceId\":\"12345678876543211234567887654322\",\n"
            + "               \"spanId\":\"8765432112345875\"\n"
            + "            }\n"
            + "         ]\n"
            + "      },\n"
            + "      {\n"
            + "         \"instrumentationLibrary\":{\n"
            + "            \"name\":\"instrumentation\",\n"
            + "            \"version\":\"1\"\n"
            + "         },\n"
            + "         \"logs\":[\n"
            + "            {\n"
            + "               \"timeUnixNano\":\"1631533710000000\",\n"
            + "               \"severityNumber\":\"SEVERITY_NUMBER_INFO\",\n"
            + "               \"severityText\":\"INFO\",\n"
            + "               \"name\":\"testLog1\",\n"
            + "               \"body\":{\n"
            + "                  \"stringValue\":\"body1\"\n"
            + "               },\n"
            + "               \"attributes\":[\n"
            + "                  {\n"
            + "                     \"key\":\"animal\",\n"
            + "                     \"value\":{\n"
            + "                        \"stringValue\":\"cat\"\n"
            + "                     }\n"
            + "                  },\n"
            + "                  {\n"
            + "                     \"key\":\"lives\",\n"
            + "                     \"value\":{\n"
            + "                        \"intValue\":\"9\"\n"
            + "                     }\n"
            + "                  }\n"
            + "               ],\n"
            + "               \"traceId\":\"12345678876543211234567887654322\",\n"
            + "               \"spanId\":\"8765432112345876\"\n"
            + "            }\n"
            + "         ]\n"
            + "      }\n"
            + "   ]\n"
            + "}",
        logs.getEvents().get(0).getMessage(),
        /* strict= */ false);
    assertThat(logs.getEvents().get(0).getMessage()).doesNotContain("\n");
  }

  @Test
  void shutdown() {
    assertThat(exporter.shutdown().isSuccess()).isTrue();
  }
}
