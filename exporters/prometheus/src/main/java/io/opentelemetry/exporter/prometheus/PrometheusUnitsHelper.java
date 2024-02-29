/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.prometheus.metrics.model.snapshots.Unit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

/** Convert OpenTelemetry unit names to Prometheus units. */
class PrometheusUnitsHelper {

  private static final Map<String, String> pluralNames = new ConcurrentHashMap<>();
  private static final Map<String, String> singularNames = new ConcurrentHashMap<>();
  private static final Map<String, Unit> predefinedUnits = new ConcurrentHashMap<>();

  // See
  // https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/c3b2997563106e11d39f66eec629fde25dce2bdd/pkg/translator/prometheus/normalize_name.go#L19-L19
  static {
    // Time
    initUnit("a", "years", "year");
    initUnit("mo", "months", "month");
    initUnit("wk", "weeks", "week");
    initUnit("d", "days", "day");
    initUnit("h", "hours", "hour");
    initUnit("min", "minutes", "minute");
    initUnit("s", "seconds", "second");
    initUnit("ms", "milliseconds", "millisecond");
    initUnit("us", "microseconds", "microsecond");
    initUnit("ns", "nanoseconds", "nanosecond");
    // Bytes
    initUnit("By", "bytes", "byte");
    initUnit("KiBy", "kibibytes", "kibibyte");
    initUnit("MiBy", "mebibytes", "mebibyte");
    initUnit("GiBy", "gibibytes", "gibibyte");
    initUnit("TiBy", "tibibytes", "tibibyte");
    initUnit("KBy", "kilobytes", "kilobyte");
    initUnit("MBy", "megabytes", "megabyte");
    initUnit("GBy", "gigabytes", "gigabyte");
    initUnit("TBy", "terabytes", "terabyte");
    // SI
    initUnit("m", "meters", "meter");
    initUnit("V", "volts", "volt");
    initUnit("A", "amperes", "ampere");
    initUnit("J", "joules", "joule");
    initUnit("W", "watts", "watt");
    initUnit("g", "grams", "gram");
    // Misc
    initUnit("Cel", "celsius");
    initUnit("Hz", "hertz");
    initUnit("%", "percent");
    initUnit("1", "ratio");
  }

  private PrometheusUnitsHelper() {}

  private static void initUnit(String otelName, String pluralName) {
    pluralNames.put(otelName, pluralName);
    predefinedUnits.put(otelName, new Unit(pluralName));
  }

  private static void initUnit(String otelName, String pluralName, String singularName) {
    initUnit(otelName, pluralName);
    singularNames.put(otelName, singularName);
  }

  @Nullable
  static Unit convertUnit(String otelUnit) {
    if (otelUnit.isEmpty()) {
      return null;
    }
    if (otelUnit.contains("{")) {
      otelUnit = otelUnit.replaceAll("\\{[^}]*}", "").trim();
      if (otelUnit.isEmpty() || otelUnit.equals("/")) {
        return null;
      }
    }
    if (predefinedUnits.containsKey(otelUnit)) {
      return predefinedUnits.get(otelUnit);
    }
    if (otelUnit.contains("/")) {
      String[] parts = otelUnit.split("/", 2);
      String part1 = pluralNames.getOrDefault(parts[0], parts[0]).trim();
      String part2 = singularNames.getOrDefault(parts[1], parts[1]).trim();
      if (part1.isEmpty()) {
        return new Unit("per_" + part2);
      } else {
        return new Unit(part1 + "_per_" + part2);
      }
    }
    return new Unit(otelUnit);
  }
}
