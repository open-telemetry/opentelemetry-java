/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * A utility class that is used to maintain mappings between OTLP unit and Prometheus units. The
 * list of mappings is adopted from <a
 * href="https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/main/pkg/translator/prometheus/normalize_name.go#L30">OpenTelemetry
 * Collector Contrib</a>.
 *
 * @see <a
 *     href="https://github.com/OpenObservability/OpenMetrics/blob/main/specification/OpenMetrics.md#units-and-base-units">OpenMetrics
 *     specification for units</a>
 * @see <a href="https://prometheus.io/docs/practices/naming/#base-units">Prometheus best practices
 *     for units</a>
 */
public final class PrometheusUnitsHelper {

  private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9_:]");
  private static final String CHARACTERS_BETWEEN_BRACES_REGEX =
      "\\{(.*?)}"; // matches all characters between {}

  private static final Map<String, String> PROMETHEUS_UNIT_MAP =
      Stream.of(
              new String[][] {
                // Time
                {"d", "days"},
                {"h", "hours"},
                {"min", "minutes"},
                {"s", "seconds"},
                {"ms", "milliseconds"},
                {"us", "microseconds"},
                {"ns", "nanoseconds"},
                // Bytes
                {"By", "bytes"},
                {"KiBy", "kibibytes"},
                {"MiBy", "mebibytes"},
                {"GiBy", "gibibytes"},
                {"TiBy", "tibibytes"},
                {"KBy", "kilobytes"},
                {"MBy", "megabytes"},
                {"GBy", "gigabytes"},
                {"TBy", "terabytes"},
                {"B", "bytes"},
                {"KB", "kilobytes"},
                {"MB", "megabytes"},
                {"GB", "gigabytes"},
                {"TB", "terabytes"},
                // SI
                {"m", "meters"},
                {"V", "volts"},
                {"A", "amperes"},
                {"J", "joules"},
                {"W", "watts"},
                {"g", "grams"},
                // Misc
                {"Cel", "celsius"},
                {"Hz", "hertz"},
                {"1", ""},
                {"%", "percent"},
                {"$", "dollars"}
              })
          .collect(
              Collectors.collectingAndThen(
                  Collectors.toMap(
                      keyValuePair -> keyValuePair[0], keyValuePair -> keyValuePair[1]),
                  Collections::unmodifiableMap));

  // The map that translates the "per" unit
  // Example: s => per second (singular)
  private static final Map<String, String> PROMETHEUS_PER_UNIT_MAP =
      Stream.of(
              new String[][] {
                {"s", "second"},
                {"m", "minute"},
                {"h", "hour"},
                {"d", "day"},
                {"w", "week"},
                {"mo", "month"},
                {"y", "year"}
              })
          .collect(
              Collectors.collectingAndThen(
                  Collectors.toMap(
                      keyValuePair -> keyValuePair[0], keyValuePair -> keyValuePair[1]),
                  Collections::unmodifiableMap));

  private PrometheusUnitsHelper() {
    // Prevent object creation for utility classes
  }

  /**
   * A utility function that returns the equivalent Prometheus name for the provided metric's unit.
   *
   * @param metric The raw {@link MetricData} for which Prometheus metric unit needs to be computed.
   * @return the computed Prometheus metric unit equivalent.
   */
  public static String getEquivalentPrometheusUnit(MetricData metric) {
    String unit = metric.getUnit();
    if (StringUtils.isNullOrEmpty(unit)) {
      return unit;
    }
    unit = INVALID_CHARACTERS_PATTERN.matcher(unit).replaceAll("_").replaceAll("[_]{2,}", "_");

    // special case
    if (unit.equals("1")
        && (metric.getType() == MetricDataType.DOUBLE_GAUGE
            || metric.getType() == MetricDataType.LONG_GAUGE)) {
      return "ratio";
    }

    // Drop units specified between curly braces
    if (unit.matches(CHARACTERS_BETWEEN_BRACES_REGEX)) {
      return removeUnitPortionInBrackets(unit);
    }

    // Handling for the "per" unit(s), e.g. foo/bar -> foo_per_bar
    if (unit.contains("/")) {
      return convertRateExpressedToPrometheusUnit(unit);
    }

    // Converting abbreviated unit names to full names
    return PROMETHEUS_UNIT_MAP.getOrDefault(unit, unit);
  }

  private static String convertRateExpressedToPrometheusUnit(String rateExpressedUnit) {
    String[] rateEntities = rateExpressedUnit.split("/", 2);
    // Only convert rate expressed units if it's a valid expression
    if (rateEntities[1].equals("")) {
      return rateExpressedUnit;
    }
    return PROMETHEUS_PER_UNIT_MAP.getOrDefault(rateEntities[0], rateEntities[0])
        + "_per_"
        + PROMETHEUS_PER_UNIT_MAP.getOrDefault(rateEntities[1], rateEntities[1]);
  }

  private static String removeUnitPortionInBrackets(@Nonnull String unit) {
    // This does not handle nested braces
    return unit.replaceAll(CHARACTERS_BETWEEN_BRACES_REGEX, "");
  }
}
