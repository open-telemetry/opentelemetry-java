/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/** A class that maps a raw metric name to Prometheus equivalent name. */
class PrometheusMetricNameMapper implements BiFunction<MetricData, PrometheusType, String> {

  static final PrometheusMetricNameMapper INSTANCE = new PrometheusMetricNameMapper();

  private final Map<String, String> cache = new ConcurrentHashMap<>();
  private final BiFunction<MetricData, PrometheusType, String> delegate;

  // private constructor - prevent external object initialization
  private PrometheusMetricNameMapper() {
    this(PrometheusMetricNameMapper::mapToPrometheusName);
  }

  // Visible for testing
  PrometheusMetricNameMapper(BiFunction<MetricData, PrometheusType, String> delegate) {
    this.delegate = delegate;
  }

  @Override
  public String apply(MetricData rawMetric, PrometheusType prometheusType) {
    return cache.computeIfAbsent(
        createKeyForCacheMapping(rawMetric, prometheusType),
        metricData -> delegate.apply(rawMetric, prometheusType));
  }

  private static String mapToPrometheusName(MetricData rawMetric, PrometheusType prometheusType) {
    String name = NameSanitizer.INSTANCE.apply(rawMetric.getName());
    String prometheusEquivalentUnit =
        PrometheusUnitsHelper.getEquivalentPrometheusUnit(rawMetric.getUnit());
    // append prometheus unit if not null or empty.
    if (!StringUtils.isNullOrEmpty(prometheusEquivalentUnit)
        && !name.contains(prometheusEquivalentUnit)) {
      name = name + "_" + prometheusEquivalentUnit;
    }

    // special case - counter
    if (prometheusType == PrometheusType.COUNTER && !name.contains("total")) {
      name = name + "_total";
    }
    // special case - gauge
    if (rawMetric.getUnit().equals("1")
        && prometheusType == PrometheusType.GAUGE
        && !name.contains("ratio")) {
      name = name + "_ratio";
    }
    return name;
  }

  /**
   * Create key from a combination of raw metric name, raw metric unit and the prometheus type since
   * all of them are used to compute the prometheus equivalent name.
   *
   * @param metricData the metric data for which the mapping is to be created.
   * @param prometheusType the prometheus type to which the metric is to be mapped.
   * @return a String that acts as the key for mapping between metric data and its prometheus
   *     equivalent name.
   */
  private static String createKeyForCacheMapping(
      MetricData metricData, PrometheusType prometheusType) {
    return metricData.getName() + metricData.getUnit() + prometheusType.name();
  }
}
