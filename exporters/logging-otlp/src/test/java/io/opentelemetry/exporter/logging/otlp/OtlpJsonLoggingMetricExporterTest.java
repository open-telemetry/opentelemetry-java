/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.event.Level;

@SuppressLogger(OtlpJsonLoggingMetricExporter.class)
class OtlpJsonLoggingMetricExporterTest {

  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("key", "value").build());

  private static final MetricData METRIC1 =
      ImmutableMetricData.createDoubleSum(
          RESOURCE,
          InstrumentationScopeInfo.builder("instrumentation")
              .setVersion("1")
              .setAttributes(Attributes.builder().put("key", "value").build())
              .build(),
          "metric1",
          "metric1 description",
          "m",
          ImmutableSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              Arrays.asList(
                  ImmutableDoublePointData.create(
                      1, 2, Attributes.of(stringKey("cat"), "meow"), 4))));

  private static final MetricData METRIC2 =
      ImmutableMetricData.createDoubleSum(
          RESOURCE,
          InstrumentationScopeInfo.builder("instrumentation2").setVersion("2").build(),
          "metric2",
          "metric2 description",
          "s",
          ImmutableSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              Arrays.asList(
                  ImmutableDoublePointData.create(
                      1, 2, Attributes.of(stringKey("cat"), "meow"), 4))));

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OtlpJsonLoggingMetricExporter.class);

  private MetricExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = OtlpJsonLoggingMetricExporter.create();
  }

  @Test
  void getAggregationTemporality() {
    assertThat(
            OtlpJsonLoggingMetricExporter.create()
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(
            OtlpJsonLoggingMetricExporter.create(AggregationTemporality.DELTA)
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void log() throws Exception {
    exporter.export(Arrays.asList(METRIC1, METRIC2));

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
            + "  \"scopeMetrics\": [{"
            + "    \"scope\": {"
            + "      \"name\": \"instrumentation2\","
            + "      \"version\": \"2\""
            + "    },"
            + "    \"metrics\": [{"
            + "      \"name\": \"metric2\","
            + "      \"description\": \"metric2 description\","
            + "      \"unit\": \"s\","
            + "      \"sum\": {"
            + "        \"dataPoints\": [{"
            + "          \"attributes\": [{"
            + "            \"key\": \"cat\","
            + "            \"value\": {\"stringValue\": \"meow\"}"
            + "          }],"
            + "          \"startTimeUnixNano\": \"1\","
            + "          \"timeUnixNano\": \"2\","
            + "          \"asDouble\": 4.0"
            + "        }],"
            + "        \"aggregationTemporality\": 2,"
            + "        \"isMonotonic\": true"
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
            + "    \"metrics\": [{"
            + "      \"name\": \"metric1\","
            + "      \"description\": \"metric1 description\","
            + "      \"unit\": \"m\","
            + "      \"sum\": {"
            + "        \"dataPoints\": [{"
            + "          \"attributes\": [{"
            + "            \"key\": \"cat\","
            + "            \"value\": {\"stringValue\": \"meow\"}"
            + "          }],"
            + "          \"startTimeUnixNano\": \"1\","
            + "          \"timeUnixNano\": \"2\","
            + "          \"asDouble\": 4.0"
            + "        }],"
            + "        \"aggregationTemporality\": 2,"
            + "        \"isMonotonic\": true"
            + "      }"
            + "    }]"
            + "  }]"
            + "}",
        logs.getEvents().get(0).getMessage(),
        /* strict= */ false);
    assertThat(logs.getEvents().get(0).getMessage()).doesNotContain("\n");
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
                .export(Collections.singletonList(METRIC1))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    assertThat(logs.getEvents()).isEmpty();
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }
}
