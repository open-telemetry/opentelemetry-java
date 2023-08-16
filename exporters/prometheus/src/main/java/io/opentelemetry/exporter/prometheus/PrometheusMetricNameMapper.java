/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import javax.annotation.concurrent.Immutable;

/** A class that maps a raw metric name to Prometheus equivalent name. */
class PrometheusMetricNameMapper implements BiFunction<MetricData, PrometheusType, String> {

  static final PrometheusMetricNameMapper INSTANCE = new PrometheusMetricNameMapper();

  private final Map<ImmutableMappingKey, String> cache = new ConcurrentHashMap<>();
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
    boolean shouldAppendUnit =
        !StringUtils.isNullOrEmpty(prometheusEquivalentUnit)
            && !name.contains(prometheusEquivalentUnit);
    // trim counter's _total suffix so the unit is placed before it.
    if (prometheusType == PrometheusType.COUNTER && name.endsWith("_total")) {
      name = name.substring(0, name.length() - 6);
    }
    // append prometheus unit if not null or empty.
    if (shouldAppendUnit) {
      name = name + "_" + prometheusEquivalentUnit;
    }

    // replace _total suffix, or add if it wasn't already present.
    if (prometheusType == PrometheusType.COUNTER) {
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
   * Creates a suitable mapping key to be used for maintaining mapping between raw metric and its
   * equivalent Prometheus name.
   *
   * @param metricData the metric data for which the mapping is to be created.
   * @param prometheusType the prometheus type to which the metric is to be mapped.
   * @return an {@link ImmutableMappingKey} that can be used as a key for mapping between metric
   *     data and its prometheus equivalent name.
   */
  private static ImmutableMappingKey createKeyForCacheMapping(
      MetricData metricData, PrometheusType prometheusType) {
    return ImmutableMappingKey.create(
        metricData.getName(), metricData.getUnit(), prometheusType.name());
  }

  /**
   * Objects of this class acts as mapping keys for Prometheus metric mapping cache used in {@link
   * PrometheusMetricNameMapper}.
   */
  @Immutable
  @AutoValue
  abstract static class ImmutableMappingKey {
    static ImmutableMappingKey create(
        String rawMetricName, String rawMetricUnit, String prometheusType) {
      return new AutoValue_PrometheusMetricNameMapper_ImmutableMappingKey(
          rawMetricName, rawMetricUnit, prometheusType);
    }

    abstract String rawMetricName();

    abstract String rawMetricUnit();

    abstract String prometheusType();
  }
}
