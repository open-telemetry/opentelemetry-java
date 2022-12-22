/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  private PrometheusUnitsHelper() {
    // Prevent object creation for utility classes
  }

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

  public static String getEquivalentPrometheusUnit(String unit) {
    return PROMETHEUS_UNIT_MAP.getOrDefault(unit, unit);
  }

  //  @SuppressWarnings("SystemOut")
  //  public static void printMap() {
  //    System.out.println("Map is ");
  //    for (Map.Entry<String, String> entry : PROMETHEUS_UNIT_MAP.entrySet()) {
  //      System.out.println(entry.getKey() + ":" + entry.getValue());
  //    }
  //  }
}
