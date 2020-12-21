/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlpjson;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class OtlpJsonLoggingMetricExporterTest {

  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("key", "value").build());

  private static final MetricData METRIC1 =
      MetricData.createDoubleSum(
          RESOURCE,
          InstrumentationLibraryInfo.create("instrumentation", "1"),
          "metric1",
          "metric1 description",
          "m",
          MetricData.DoubleSumData.create(
              true,
              MetricData.AggregationTemporality.CUMULATIVE,
              Arrays.asList(MetricData.DoublePoint.create(1, 2, Labels.of("cat", "meow"), 4))));

  private static final MetricData METRIC2 =
      MetricData.createDoubleSum(
          RESOURCE,
          InstrumentationLibraryInfo.create("instrumentation2", "2"),
          "metric2",
          "metric2 description",
          "s",
          MetricData.DoubleSumData.create(
              true,
              MetricData.AggregationTemporality.CUMULATIVE,
              Arrays.asList(MetricData.DoublePoint.create(1, 2, Labels.of("cat", "meow"), 4))));

  private MetricExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = OtlpJsonLoggingMetricExporter.create();
  }

  @Test
  void log() throws Exception {
    Logger logger = OtlpJsonLoggingMetricExporter.logger;
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
      exporter.export(Arrays.asList(METRIC1, METRIC2));

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
              + "  \"instrumentationLibraryMetrics\": [{\n"
              + "    \"instrumentationLibrary\": {\n"
              + "      \"name\": \"instrumentation2\",\n"
              + "      \"version\": \"2\"\n"
              + "    },\n"
              + "    \"metrics\": [{\n"
              + "      \"name\": \"metric2\",\n"
              + "      \"description\": \"metric2 description\",\n"
              + "      \"unit\": \"s\",\n"
              + "      \"doubleSum\": {\n"
              + "        \"dataPoints\": [{\n"
              + "          \"labels\": [{\n"
              + "            \"key\": \"cat\",\n"
              + "            \"value\": \"meow\"\n"
              + "          }],\n"
              + "          \"startTimeUnixNano\": \"1\",\n"
              + "          \"timeUnixNano\": \"2\",\n"
              + "          \"value\": 4.0\n"
              + "        }],\n"
              + "        \"aggregationTemporality\": \"AGGREGATION_TEMPORALITY_CUMULATIVE\",\n"
              + "        \"isMonotonic\": true\n"
              + "      }\n"
              + "    }]\n"
              + "  }, {\n"
              + "    \"instrumentationLibrary\": {\n"
              + "      \"name\": \"instrumentation\",\n"
              + "      \"version\": \"1\"\n"
              + "    },\n"
              + "    \"metrics\": [{\n"
              + "      \"name\": \"metric1\",\n"
              + "      \"description\": \"metric1 description\",\n"
              + "      \"unit\": \"m\",\n"
              + "      \"doubleSum\": {\n"
              + "        \"dataPoints\": [{\n"
              + "          \"labels\": [{\n"
              + "            \"key\": \"cat\",\n"
              + "            \"value\": \"meow\"\n"
              + "          }],\n"
              + "          \"startTimeUnixNano\": \"1\",\n"
              + "          \"timeUnixNano\": \"2\",\n"
              + "          \"value\": 4.0\n"
              + "        }],\n"
              + "        \"aggregationTemporality\": \"AGGREGATION_TEMPORALITY_CUMULATIVE\",\n"
              + "        \"isMonotonic\": true\n"
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
