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
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogData;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.event.Level;

@SuppressLogger(OtlpJsonLoggingLogExporter.class)
class OtlpJsonLoggingLogExporterTest {

  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("key", "value").build());

  private static final LogData LOG1 =
      TestLogData.builder()
          .setResource(RESOURCE)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation")
                  .setVersion("1")
                  .setAttributes(Attributes.builder().put("key", "value").build())
                  .build())
          .setBody("body1")
          .setSeverity(Severity.INFO)
          .setSeverityText("INFO")
          .setEpoch(1631533710L, TimeUnit.MILLISECONDS)
          .setAttributes(Attributes.of(stringKey("animal"), "cat", longKey("lives"), 9L))
          .setSpanContext(
              SpanContext.create(
                  "12345678876543211234567887654322",
                  "8765432112345876",
                  TraceFlags.getDefault(),
                  TraceState.getDefault()))
          .build();

  private static final LogData LOG2 =
      TestLogData.builder()
          .setResource(RESOURCE)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation2").setVersion("2").build())
          .setBody("body2")
          .setSeverity(Severity.INFO)
          .setSeverityText("INFO")
          .setEpoch(1631533710L, TimeUnit.MILLISECONDS)
          .setAttributes(Attributes.of(booleanKey("important"), true))
          .setSpanContext(
              SpanContext.create(
                  "12345678876543211234567887654322",
                  "8765432112345875",
                  TraceFlags.getDefault(),
                  TraceState.getDefault()))
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
    String message = logs.getEvents().get(0).getMessage();
    JSONAssert.assertEquals(
        "{"
            + "   \"resource\": {"
            + "     \"attributes\": [{"
            + "       \"key\":\"key\","
            + "       \"value\": {"
            + "          \"stringValue\":\"value\""
            + "       }"
            + "     }]"
            + "   },"
            + "   \"scopeLogs\": [{"
            + "     \"scope\":{"
            + "       \"name\":\"instrumentation2\","
            + "       \"version\":\"2\""
            + "     },"
            + "     \"logRecords\": [{"
            + "       \"timeUnixNano\":\"1631533710000000\","
            + "       \"severityNumber\":\"SEVERITY_NUMBER_INFO\","
            + "       \"severityText\":\"INFO\","
            + "       \"body\": {"
            + "         \"stringValue\":\"body2\""
            + "       },"
            + "       \"attributes\": [{"
            + "         \"key\":\"important\","
            + "         \"value\": {"
            + "           \"boolValue\":true"
            + "         }"
            + "       }],"
            + "       \"traceId\":\"12345678876543211234567887654322\","
            + "       \"spanId\":\"8765432112345875\""
            + "     }]"
            + "     }, {"
            + "     \"scope\": {"
            + "       \"name\":\"instrumentation\","
            + "       \"version\":\"1\","
            + "       \"attributes\": [{"
            + "         \"key\":\"key\","
            + "         \"value\": {"
            + "           \"stringValue\":\"value\""
            + "         }"
            + "       }]"
            + "     },"
            + "     \"logRecords\": [{"
            + "       \"timeUnixNano\":\"1631533710000000\","
            + "       \"severityNumber\":\"SEVERITY_NUMBER_INFO\","
            + "       \"severityText\":\"INFO\","
            + "       \"body\": {"
            + "         \"stringValue\":\"body1\""
            + "       },"
            + "       \"attributes\": [{"
            + "         \"key\":\"animal\","
            + "         \"value\": {"
            + "           \"stringValue\":\"cat\""
            + "         }"
            + "       }, {"
            + "         \"key\":\"lives\","
            + "         \"value\":{"
            + "           \"intValue\":\"9\""
            + "         }"
            + "       }],"
            + "       \"traceId\":\"12345678876543211234567887654322\","
            + "       \"spanId\":\"8765432112345876\""
            + "     }]"
            + "   }]"
            + "}",
        message,
        /* strict= */ false);
    assertThat(message).doesNotContain("\n");
  }

  @Test
  void shutdown() {
    assertThat(exporter.shutdown().isSuccess()).isTrue();
  }
}
