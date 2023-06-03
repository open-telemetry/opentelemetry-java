/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.exporter.prometheus.TestConstants.CUMULATIVE_HISTOGRAM_NO_ATTRIBUTES;
import static io.opentelemetry.exporter.prometheus.TestConstants.CUMULATIVE_HISTOGRAM_SINGLE_ATTRIBUTE;
import static io.opentelemetry.exporter.prometheus.TestConstants.DELTA_DOUBLE_SUM;
import static io.opentelemetry.exporter.prometheus.TestConstants.DELTA_HISTOGRAM;
import static io.opentelemetry.exporter.prometheus.TestConstants.DELTA_LONG_SUM;
import static io.opentelemetry.exporter.prometheus.TestConstants.DOUBLE_GAUGE;
import static io.opentelemetry.exporter.prometheus.TestConstants.DOUBLE_GAUGE_MULTIPLE_ATTRIBUTES;
import static io.opentelemetry.exporter.prometheus.TestConstants.DOUBLE_GAUGE_NO_ATTRIBUTES;
import static io.opentelemetry.exporter.prometheus.TestConstants.LONG_GAUGE;
import static io.opentelemetry.exporter.prometheus.TestConstants.MONOTONIC_CUMULATIVE_DOUBLE_SUM;
import static io.opentelemetry.exporter.prometheus.TestConstants.MONOTONIC_CUMULATIVE_DOUBLE_SUM_WITH_SUFFIX_TOTAL;
import static io.opentelemetry.exporter.prometheus.TestConstants.MONOTONIC_CUMULATIVE_LONG_SUM;
import static io.opentelemetry.exporter.prometheus.TestConstants.NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM;
import static io.opentelemetry.exporter.prometheus.TestConstants.NON_MONOTONIC_CUMULATIVE_LONG_SUM;
import static io.opentelemetry.exporter.prometheus.TestConstants.SUMMARY;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.MetricData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SerializerTest {

  @Test
  void prometheus004() {
    // Same output as prometheus client library except for these changes which are compatible with
    // Prometheus
    // TYPE / HELP line order reversed
    // Attributes do not end in trailing comma
    assertThat(
            serialize004(
                MONOTONIC_CUMULATIVE_DOUBLE_SUM,
                MONOTONIC_CUMULATIVE_DOUBLE_SUM_WITH_SUFFIX_TOTAL,
                NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM,
                DELTA_DOUBLE_SUM, // Deltas are dropped
                MONOTONIC_CUMULATIVE_LONG_SUM,
                NON_MONOTONIC_CUMULATIVE_LONG_SUM,
                DELTA_LONG_SUM, // Deltas are dropped
                DOUBLE_GAUGE,
                LONG_GAUGE,
                SUMMARY,
                DELTA_HISTOGRAM, // Deltas are dropped
                CUMULATIVE_HISTOGRAM_NO_ATTRIBUTES,
                CUMULATIVE_HISTOGRAM_SINGLE_ATTRIBUTE,
                DOUBLE_GAUGE_NO_ATTRIBUTES,
                DOUBLE_GAUGE_MULTIPLE_ATTRIBUTES))
        .isEqualTo(
            "# TYPE target info\n"
                + "# HELP target Target metadata\n"
                + "target_info{kr=\"vr\"} 1\n"
                + "# TYPE otel_scope_info info\n"
                + "# HELP otel_scope_info Scope metadata\n"
                + "otel_scope_info{otel_scope_name=\"full\",otel_scope_version=\"version\",ks=\"vs\"} 1\n"
                + "# TYPE monotonic_cumulative_double_sum_total counter\n"
                + "# HELP monotonic_cumulative_double_sum_total description\n"
                + "monotonic_cumulative_double_sum_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcds\"} 5.0 1633950672000\n"
                + "# TYPE monotonic_cumulative_double_sum_suffix_total counter\n"
                + "# HELP monotonic_cumulative_double_sum_suffix_total description\n"
                + "monotonic_cumulative_double_sum_suffix_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcds\"} 5.0 1633950672000\n"
                + "# TYPE non_monotonic_cumulative_double_sum gauge\n"
                + "# HELP non_monotonic_cumulative_double_sum description\n"
                + "non_monotonic_cumulative_double_sum_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"nmcds\"} 5.0 1633950672000\n"
                + "# TYPE monotonic_cumulative_long_sum_total counter\n"
                + "# HELP monotonic_cumulative_long_sum_total unused\n"
                + "monotonic_cumulative_long_sum_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcls\"} 5.0 1633950672000\n"
                + "# TYPE non_monotonic_cumulative_long_sum gauge\n"
                + "# HELP non_monotonic_cumulative_long_sum unused\n"
                + "non_monotonic_cumulative_long_sum_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"nmcls\"} 5.0 1633950672000\n"
                + "# TYPE double_gauge gauge\n"
                + "# HELP double_gauge unused\n"
                + "double_gauge_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"dg\"} 5.0 1633950672000\n"
                + "# TYPE long_gauge gauge\n"
                + "# HELP long_gauge unused\n"
                + "long_gauge_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"lg\"} 5.0 1633950672000\n"
                + "# TYPE summary summary\n"
                + "# HELP summary unused\n"
                + "summary_count{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\"} 5.0 1633950672000\n"
                + "summary_sum{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\"} 7.0 1633950672000\n"
                + "summary{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\",quantile=\"0.9\"} 0.1 1633950672000\n"
                + "summary{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\",quantile=\"0.99\"} 0.3 1633950672000\n"
                + "# TYPE cumulative_histogram_no_attributes histogram\n"
                + "# HELP cumulative_histogram_no_attributes unused\n"
                + "cumulative_histogram_no_attributes_count{otel_scope_name=\"full\",otel_scope_version=\"version\"} 2.0 1633950672000\n"
                + "cumulative_histogram_no_attributes_sum{otel_scope_name=\"full\",otel_scope_version=\"version\"} 1.0 1633950672000\n"
                + "cumulative_histogram_no_attributes_bucket{otel_scope_name=\"full\",otel_scope_version=\"version\",le=\"+Inf\"} 2.0 1633950672000\n"
                + "# TYPE cumulative_histogram_single_attribute histogram\n"
                + "# HELP cumulative_histogram_single_attribute unused\n"
                + "cumulative_histogram_single_attribute_count{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\"} 2.0 1633950672000\n"
                + "cumulative_histogram_single_attribute_sum{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\"} 1.0 1633950672000\n"
                + "cumulative_histogram_single_attribute_bucket{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\",le=\"+Inf\"} 2.0 1633950672000\n"
                + "# TYPE double_gauge_no_attributes gauge\n"
                + "# HELP double_gauge_no_attributes unused\n"
                + "double_gauge_no_attributes_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\"} 7.0 1633950672000\n"
                + "# TYPE double_gauge_multiple_attributes gauge\n"
                + "# HELP double_gauge_multiple_attributes unused\n"
                + "double_gauge_multiple_attributes_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",animal=\"bear\",type=\"dgma\"} 8.0 1633950672000\n");
  }

  @Test
  void openMetrics() {
    assertThat(
            serializeOpenMetrics(
                MONOTONIC_CUMULATIVE_DOUBLE_SUM,
                MONOTONIC_CUMULATIVE_DOUBLE_SUM_WITH_SUFFIX_TOTAL,
                NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM,
                DELTA_DOUBLE_SUM, // Deltas are dropped
                MONOTONIC_CUMULATIVE_LONG_SUM,
                NON_MONOTONIC_CUMULATIVE_LONG_SUM,
                DELTA_LONG_SUM, // Deltas are dropped
                DOUBLE_GAUGE,
                LONG_GAUGE,
                SUMMARY,
                DELTA_HISTOGRAM, // Deltas are dropped
                CUMULATIVE_HISTOGRAM_NO_ATTRIBUTES,
                CUMULATIVE_HISTOGRAM_SINGLE_ATTRIBUTE,
                DOUBLE_GAUGE_NO_ATTRIBUTES,
                DOUBLE_GAUGE_MULTIPLE_ATTRIBUTES))
        .isEqualTo(
            "# TYPE target info\n"
                + "# HELP target Target metadata\n"
                + "target_info{kr=\"vr\"} 1\n"
                + "# TYPE otel_scope_info info\n"
                + "# HELP otel_scope_info Scope metadata\n"
                + "otel_scope_info{otel_scope_name=\"full\",otel_scope_version=\"version\",ks=\"vs\"} 1\n"
                + "# TYPE monotonic_cumulative_double_sum counter\n"
                + "# HELP monotonic_cumulative_double_sum description\n"
                + "monotonic_cumulative_double_sum_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcds\"} 5.0 1633950672.000\n"
                + "# TYPE monotonic_cumulative_double_sum_suffix_total counter\n"
                + "# HELP monotonic_cumulative_double_sum_suffix_total description\n"
                + "monotonic_cumulative_double_sum_suffix_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcds\"} 5.0 1633950672.000\n"
                + "# TYPE non_monotonic_cumulative_double_sum gauge\n"
                + "# HELP non_monotonic_cumulative_double_sum description\n"
                + "non_monotonic_cumulative_double_sum_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"nmcds\"} 5.0 1633950672.000\n"
                + "# TYPE monotonic_cumulative_long_sum counter\n"
                + "# HELP monotonic_cumulative_long_sum unused\n"
                + "monotonic_cumulative_long_sum_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcls\"} 5.0 1633950672.000\n"
                + "# TYPE non_monotonic_cumulative_long_sum gauge\n"
                + "# HELP non_monotonic_cumulative_long_sum unused\n"
                + "non_monotonic_cumulative_long_sum_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"nmcls\"} 5.0 1633950672.000\n"
                + "# TYPE double_gauge gauge\n"
                + "# HELP double_gauge unused\n"
                + "double_gauge_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"dg\"} 5.0 1633950672.000\n"
                + "# TYPE long_gauge gauge\n"
                + "# HELP long_gauge unused\n"
                + "long_gauge_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"lg\"} 5.0 1633950672.000\n"
                + "# TYPE summary summary\n"
                + "# HELP summary unused\n"
                + "summary_count{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\"} 5.0 1633950672.000\n"
                + "summary_sum{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\"} 7.0 1633950672.000\n"
                + "summary{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\",quantile=\"0.9\"} 0.1 1633950672.000\n"
                + "summary{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\",quantile=\"0.99\"} 0.3 1633950672.000\n"
                + "# TYPE cumulative_histogram_no_attributes histogram\n"
                + "# HELP cumulative_histogram_no_attributes unused\n"
                + "cumulative_histogram_no_attributes_count{otel_scope_name=\"full\",otel_scope_version=\"version\"} 2.0 1633950672.000\n"
                + "cumulative_histogram_no_attributes_sum{otel_scope_name=\"full\",otel_scope_version=\"version\"} 1.0 1633950672.000\n"
                + "cumulative_histogram_no_attributes_bucket{otel_scope_name=\"full\",otel_scope_version=\"version\",le=\"+Inf\"} 2.0 1633950672.000 # {span_id=\"0000000000000002\",trace_id=\"00000000000000000000000000000001\"} 4.0 0.001\n"
                + "# TYPE cumulative_histogram_single_attribute histogram\n"
                + "# HELP cumulative_histogram_single_attribute unused\n"
                + "cumulative_histogram_single_attribute_count{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\"} 2.0 1633950672.000\n"
                + "cumulative_histogram_single_attribute_sum{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\"} 1.0 1633950672.000\n"
                + "cumulative_histogram_single_attribute_bucket{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\",le=\"+Inf\"} 2.0 1633950672.000 # {span_id=\"0000000000000002\",trace_id=\"00000000000000000000000000000001\"} 4.0 0.001\n"
                + "# TYPE double_gauge_no_attributes gauge\n"
                + "# HELP double_gauge_no_attributes unused\n"
                + "double_gauge_no_attributes_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\"} 7.0 1633950672.000\n"
                + "# TYPE double_gauge_multiple_attributes gauge\n"
                + "# HELP double_gauge_multiple_attributes unused\n"
                + "double_gauge_multiple_attributes_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",animal=\"bear\",type=\"dgma\"} 8.0 1633950672.000\n"
                + "# EOF\n");
  }

  private static String serialize004(MetricData... metrics) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      new Serializer.Prometheus004Serializer(unused -> true).write(Arrays.asList(metrics), bos);
      return bos.toString("UTF-8");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String serializeOpenMetrics(MetricData... metrics) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      new Serializer.OpenMetrics100Serializer(unused -> true).write(Arrays.asList(metrics), bos);
      return bos.toString("UTF-8");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
