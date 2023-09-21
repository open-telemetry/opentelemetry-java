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
import static io.opentelemetry.exporter.prometheus.TestConstants.DOUBLE_GAUGE_COLLIDING_ATTRIBUTES;
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

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class SerializerTest {

  @RegisterExtension
  private final LogCapturer logCapturer =
      LogCapturer.create().captureForLogger(Serializer.class.getName());

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
                DOUBLE_GAUGE_MULTIPLE_ATTRIBUTES,
                DOUBLE_GAUGE_COLLIDING_ATTRIBUTES))
        .isEqualTo(
            "# TYPE target info\n"
                + "# HELP target Target metadata\n"
                + "target_info{kr=\"vr\"} 1\n"
                + "# TYPE otel_scope_info info\n"
                + "# HELP otel_scope_info Scope metadata\n"
                + "otel_scope_info{otel_scope_name=\"full\",otel_scope_version=\"version\",ks=\"vs\"} 1\n"
                + "# TYPE monotonic_cumulative_double_sum_seconds_total counter\n"
                + "# HELP monotonic_cumulative_double_sum_seconds_total description\n"
                + "monotonic_cumulative_double_sum_seconds_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcds\"} 5.0 1633950672000\n"
                + "# TYPE monotonic_cumulative_double_sum_suffix_seconds_total counter\n"
                + "# HELP monotonic_cumulative_double_sum_suffix_seconds_total description\n"
                + "monotonic_cumulative_double_sum_suffix_seconds_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcds\"} 5.0 1633950672000\n"
                + "# TYPE non_monotonic_cumulative_double_sum_seconds gauge\n"
                + "# HELP non_monotonic_cumulative_double_sum_seconds description\n"
                + "non_monotonic_cumulative_double_sum_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"nmcds\"} 5.0 1633950672000\n"
                + "# TYPE monotonic_cumulative_long_sum_seconds_total counter\n"
                + "# HELP monotonic_cumulative_long_sum_seconds_total unused\n"
                + "monotonic_cumulative_long_sum_seconds_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcls\"} 5.0 1633950672000\n"
                + "# TYPE non_monotonic_cumulative_long_sum_seconds gauge\n"
                + "# HELP non_monotonic_cumulative_long_sum_seconds unused\n"
                + "non_monotonic_cumulative_long_sum_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"nmcls\"} 5.0 1633950672000\n"
                + "# TYPE double_gauge_seconds gauge\n"
                + "# HELP double_gauge_seconds unused\n"
                + "double_gauge_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"dg\"} 5.0 1633950672000\n"
                + "# TYPE long_gauge_seconds gauge\n"
                + "# HELP long_gauge_seconds unused\n"
                + "long_gauge_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"lg\"} 5.0 1633950672000\n"
                + "# TYPE summary_seconds summary\n"
                + "# HELP summary_seconds unused\n"
                + "summary_seconds_count{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\"} 5.0 1633950672000\n"
                + "summary_seconds_sum{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\"} 7.0 1633950672000\n"
                + "summary_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\",quantile=\"0.9\"} 0.1 1633950672000\n"
                + "summary_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\",quantile=\"0.99\"} 0.3 1633950672000\n"
                + "# TYPE cumulative_histogram_no_attributes_seconds histogram\n"
                + "# HELP cumulative_histogram_no_attributes_seconds unused\n"
                + "cumulative_histogram_no_attributes_seconds_count{otel_scope_name=\"full\",otel_scope_version=\"version\"} 2.0 1633950672000\n"
                + "cumulative_histogram_no_attributes_seconds_sum{otel_scope_name=\"full\",otel_scope_version=\"version\"} 1.0 1633950672000\n"
                + "cumulative_histogram_no_attributes_seconds_bucket{otel_scope_name=\"full\",otel_scope_version=\"version\",le=\"+Inf\"} 2.0 1633950672000\n"
                + "# TYPE cumulative_histogram_single_attribute_seconds histogram\n"
                + "# HELP cumulative_histogram_single_attribute_seconds unused\n"
                + "cumulative_histogram_single_attribute_seconds_count{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\"} 2.0 1633950672000\n"
                + "cumulative_histogram_single_attribute_seconds_sum{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\"} 1.0 1633950672000\n"
                + "cumulative_histogram_single_attribute_seconds_bucket{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\",le=\"+Inf\"} 2.0 1633950672000\n"
                + "# TYPE double_gauge_no_attributes_seconds gauge\n"
                + "# HELP double_gauge_no_attributes_seconds unused\n"
                + "double_gauge_no_attributes_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\"} 7.0 1633950672000\n"
                + "# TYPE double_gauge_multiple_attributes_seconds gauge\n"
                + "# HELP double_gauge_multiple_attributes_seconds unused\n"
                + "double_gauge_multiple_attributes_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",animal=\"bear\",type=\"dgma\"} 8.0 1633950672000\n"
                + "# TYPE double_gauge_colliding_attributes_seconds gauge\n"
                + "# HELP double_gauge_colliding_attributes_seconds unused\n"
                + "double_gauge_colliding_attributes_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",foo_bar=\"a;b\",type=\"dgma\"} 8.0 1633950672000\n");
    assertThat(logCapturer.size()).isZero();
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
                DOUBLE_GAUGE_MULTIPLE_ATTRIBUTES,
                DOUBLE_GAUGE_COLLIDING_ATTRIBUTES))
        .isEqualTo(
            "# TYPE target info\n"
                + "# HELP target Target metadata\n"
                + "target_info{kr=\"vr\"} 1\n"
                + "# TYPE otel_scope_info info\n"
                + "# HELP otel_scope_info Scope metadata\n"
                + "otel_scope_info{otel_scope_name=\"full\",otel_scope_version=\"version\",ks=\"vs\"} 1\n"
                + "# TYPE monotonic_cumulative_double_sum_seconds counter\n"
                + "# HELP monotonic_cumulative_double_sum_seconds description\n"
                + "monotonic_cumulative_double_sum_seconds_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcds\"} 5.0 1633950672.000\n"
                + "# TYPE monotonic_cumulative_double_sum_suffix_seconds_total counter\n"
                + "# HELP monotonic_cumulative_double_sum_suffix_seconds_total description\n"
                + "monotonic_cumulative_double_sum_suffix_seconds_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcds\"} 5.0 1633950672.000\n"
                + "# TYPE non_monotonic_cumulative_double_sum_seconds gauge\n"
                + "# HELP non_monotonic_cumulative_double_sum_seconds description\n"
                + "non_monotonic_cumulative_double_sum_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"nmcds\"} 5.0 1633950672.000\n"
                + "# TYPE monotonic_cumulative_long_sum_seconds counter\n"
                + "# HELP monotonic_cumulative_long_sum_seconds unused\n"
                + "monotonic_cumulative_long_sum_seconds_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcls\"} 5.0 1633950672.000\n"
                + "# TYPE non_monotonic_cumulative_long_sum_seconds gauge\n"
                + "# HELP non_monotonic_cumulative_long_sum_seconds unused\n"
                + "non_monotonic_cumulative_long_sum_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"nmcls\"} 5.0 1633950672.000\n"
                + "# TYPE double_gauge_seconds gauge\n"
                + "# HELP double_gauge_seconds unused\n"
                + "double_gauge_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"dg\"} 5.0 1633950672.000\n"
                + "# TYPE long_gauge_seconds gauge\n"
                + "# HELP long_gauge_seconds unused\n"
                + "long_gauge_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"lg\"} 5.0 1633950672.000\n"
                + "# TYPE summary_seconds summary\n"
                + "# HELP summary_seconds unused\n"
                + "summary_seconds_count{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\"} 5.0 1633950672.000\n"
                + "summary_seconds_sum{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\"} 7.0 1633950672.000\n"
                + "summary_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\",quantile=\"0.9\"} 0.1 1633950672.000\n"
                + "summary_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\",quantile=\"0.99\"} 0.3 1633950672.000\n"
                + "# TYPE cumulative_histogram_no_attributes_seconds histogram\n"
                + "# HELP cumulative_histogram_no_attributes_seconds unused\n"
                + "cumulative_histogram_no_attributes_seconds_count{otel_scope_name=\"full\",otel_scope_version=\"version\"} 2.0 1633950672.000\n"
                + "cumulative_histogram_no_attributes_seconds_sum{otel_scope_name=\"full\",otel_scope_version=\"version\"} 1.0 1633950672.000\n"
                + "cumulative_histogram_no_attributes_seconds_bucket{otel_scope_name=\"full\",otel_scope_version=\"version\",le=\"+Inf\"} 2.0 1633950672.000 # {span_id=\"0000000000000002\",trace_id=\"00000000000000000000000000000001\"} 4.0 0.001\n"
                + "# TYPE cumulative_histogram_single_attribute_seconds histogram\n"
                + "# HELP cumulative_histogram_single_attribute_seconds unused\n"
                + "cumulative_histogram_single_attribute_seconds_count{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\"} 2.0 1633950672.000\n"
                + "cumulative_histogram_single_attribute_seconds_sum{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\"} 1.0 1633950672.000\n"
                + "cumulative_histogram_single_attribute_seconds_bucket{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\",le=\"+Inf\"} 2.0 1633950672.000 # {span_id=\"0000000000000002\",trace_id=\"00000000000000000000000000000001\"} 4.0 0.001\n"
                + "# TYPE double_gauge_no_attributes_seconds gauge\n"
                + "# HELP double_gauge_no_attributes_seconds unused\n"
                + "double_gauge_no_attributes_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\"} 7.0 1633950672.000\n"
                + "# TYPE double_gauge_multiple_attributes_seconds gauge\n"
                + "# HELP double_gauge_multiple_attributes_seconds unused\n"
                + "double_gauge_multiple_attributes_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",animal=\"bear\",type=\"dgma\"} 8.0 1633950672.000\n"
                + "# TYPE double_gauge_colliding_attributes_seconds gauge\n"
                + "# HELP double_gauge_colliding_attributes_seconds unused\n"
                + "double_gauge_colliding_attributes_seconds{otel_scope_name=\"full\",otel_scope_version=\"version\",foo_bar=\"a;b\",type=\"dgma\"} 8.0 1633950672.000\n"
                + "# EOF\n");
    assertThat(logCapturer.size()).isZero();
  }

  @Test
  @SuppressLogger(Serializer.class)
  void outOfOrderedAttributes() {
    // Alternative attributes implementation which sorts entries by the order they were added rather
    // than lexicographically
    // all attributes are retained, we log a warning, and b_key and b.key are not be merged
    LinkedHashMap<AttributeKey<?>, Object> attributesMap = new LinkedHashMap<>();
    attributesMap.put(AttributeKey.stringKey("b_key"), "val1");
    attributesMap.put(AttributeKey.stringKey("a_key"), "val2");
    attributesMap.put(AttributeKey.stringKey("b.key"), "val3");
    Attributes attributes = new MapAttributes(attributesMap);

    MetricData metricData =
        ImmutableMetricData.createDoubleSum(
            Resource.builder().put("kr", "vr").build(),
            InstrumentationScopeInfo.builder("scope").setVersion("1.0.0").build(),
            "sum",
            "description",
            "s",
            ImmutableSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    ImmutableDoublePointData.create(
                        1633947011000000000L, 1633950672000000000L, attributes, 5))));

    assertThat(serialize004(metricData))
        .isEqualTo(
            "# TYPE target info\n"
                + "# HELP target Target metadata\n"
                + "target_info{kr=\"vr\"} 1\n"
                + "# TYPE otel_scope_info info\n"
                + "# HELP otel_scope_info Scope metadata\n"
                + "otel_scope_info{otel_scope_name=\"scope\",otel_scope_version=\"1.0.0\"} 1\n"
                + "# TYPE sum_seconds_total counter\n"
                + "# HELP sum_seconds_total description\n"
                + "sum_seconds_total{otel_scope_name=\"scope\",otel_scope_version=\"1.0.0\",b_key=\"val1\",a_key=\"val2\",b_key=\"val3\"} 5.0 1633950672000\n");
    logCapturer.assertContains(
        "Dropping out-of-order attribute a_key=val2, which occurred after b_key. This can occur when an alternative Attribute implementation is used.");
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

  @SuppressWarnings("unchecked")
  private static class MapAttributes implements Attributes {

    private final LinkedHashMap<AttributeKey<?>, Object> map;

    @SuppressWarnings("NonApiType")
    private MapAttributes(LinkedHashMap<AttributeKey<?>, Object> map) {
      this.map = map;
    }

    @Nullable
    @Override
    public <T> T get(AttributeKey<T> key) {
      return (T) map.get(key);
    }

    @Override
    public void forEach(BiConsumer<? super AttributeKey<?>, ? super Object> consumer) {
      map.forEach(consumer);
    }

    @Override
    public int size() {
      return map.size();
    }

    @Override
    public boolean isEmpty() {
      return map.isEmpty();
    }

    @Override
    public Map<AttributeKey<?>, Object> asMap() {
      return map;
    }

    @Override
    public AttributesBuilder toBuilder() {
      throw new UnsupportedOperationException("not supported");
    }
  }
}
