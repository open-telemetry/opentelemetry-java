/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.api.internal.StringUtils;
import java.util.regex.Pattern;

/**
 * A utility class that contains helper function(s) to aid conversion from OTLP to Prometheus units.
 *
 * @see <a
 *     href="https://github.com/OpenObservability/OpenMetrics/blob/main/specification/OpenMetrics.md#units-and-base-units">OpenMetrics
 *     specification for units</a>
 * @see <a href="https://prometheus.io/docs/practices/naming/#base-units">Prometheus best practices
 *     for units</a>
 */
final class PrometheusUnitsHelper {

  private static final Pattern CHARACTERS_BETWEEN_BRACES_PATTERN = Pattern.compile("\\{(.*?)}");

  private PrometheusUnitsHelper() {
    // Prevent object creation for utility classes
  }

  /**
   * A utility function that returns the equivalent Prometheus name for the provided OTLP metric
   * unit. This function does not handle the unsupported characters that it may find within the
   * string.
   *
   * @param rawMetricUnitName The raw metric unit for which Prometheus metric unit needs to be
   *     computed.
   * @return the computed Prometheus metric unit equivalent of the OTLP metric unit.
   * @see <a
   *     href="https://prometheus.io/docs/concepts/data_model/#metric-names-and-labels">Prometheus
   *     metric names and labels</a> for supported characters.
   */
  public static String getEquivalentPrometheusUnit(String rawMetricUnitName) {
    if (StringUtils.isNullOrEmpty(rawMetricUnitName)) {
      return rawMetricUnitName;
    }
    // Drop units specified between curly braces
    String convertedMetricUnitName = removeUnitPortionInBraces(rawMetricUnitName);
    // Handling for the "per" unit(s), e.g. foo/bar -> foo_per_bar
    convertedMetricUnitName = convertRateExpressedToPrometheusUnit(convertedMetricUnitName);
    // Converting abbreviated unit names to full names
    return getPrometheusUnit(convertedMetricUnitName);
  }

  /**
   * This method is used to convert the units expressed as a rate via '/' symbol in their name to
   * their expanded text equivalent. For instance, km/h => km_per_hour. The method operates on the
   * input by splitting it in 2 parts - before and after '/' symbol and will attempt to expand any
   * known unit abbreviation in both parts. Unknown abbreviations & unsupported characters will
   * remain unchanged in the final output of this function.
   *
   * @param rateExpressedUnit The rate unit input that needs to be converted to its text equivalent.
   * @return The text equivalent of unit expressed as rate. If the input does not contain '/', the
   *     function returns it as-is.
   */
  private static String convertRateExpressedToPrometheusUnit(String rateExpressedUnit) {
    if (!rateExpressedUnit.contains("/")) {
      return rateExpressedUnit;
    }
    String[] rateEntities = rateExpressedUnit.split("/", 2);
    // Only convert rate expressed units if it's a valid expression
    if (rateEntities[1].equals("")) {
      return rateExpressedUnit;
    }
    return getPrometheusUnit(rateEntities[0]) + "_per_" + getPrometheusPerUnit(rateEntities[1]);
  }

  /**
   * This method drops all characters enclosed within '{}' (including the curly braces) by replacing
   * them with an empty string. Note that this method will not produce the intended effect if there
   * are nested curly braces within the outer enclosure of '{}'.
   *
   * <p>For instance, {packet{s}s} => s}.
   *
   * @param unit The input unit from which text within curly braces needs to be removed.
   * @return The resulting unit after removing the text within '{}'.
   */
  private static String removeUnitPortionInBraces(String unit) {
    return CHARACTERS_BETWEEN_BRACES_PATTERN.matcher(unit).replaceAll("");
  }

  /**
   * This method retrieves the expanded Prometheus unit name for known abbreviations. OTLP metrics
   * use the c/s notation as specified at <a href="https://ucum.org/ucum.html">UCUM</a>. The list of
   * mappings is adopted from <a
   * href="https://github.com/open-telemetry/opentelemetry-collector-contrib/blob/9a9d4778bbbf242dba233db28e2fbcfda3416959/pkg/translator/prometheus/normalize_name.go#L30">OpenTelemetry
   * Collector Contrib</a>.
   *
   * @param unitAbbreviation The unit that name that needs to be expanded/converted to Prometheus
   *     units.
   * @return The expanded/converted unit name if known, otherwise returns the input unit name as-is.
   */
  private static String getPrometheusUnit(String unitAbbreviation) {
    switch (unitAbbreviation) {
        // Time
      case "d":
        return "days";
      case "h":
        return "hours";
      case "min":
        return "minutes";
      case "s":
        return "seconds";
      case "ms":
        return "milliseconds";
      case "us":
        return "microseconds";
      case "ns":
        return "nanoseconds";
        // Bytes
      case "By":
        return "bytes";
      case "KiBy":
        return "kibibytes";
      case "MiBy":
        return "mebibytes";
      case "GiBy":
        return "gibibytes";
      case "TiBy":
        return "tibibytes";
      case "KBy":
        return "kilobytes";
      case "MBy":
        return "megabytes";
      case "GBy":
        return "gigabytes";
      case "TBy":
        return "terabytes";
      case "B":
        return "bytes";
      case "KB":
        return "kilobytes";
      case "MB":
        return "megabytes";
      case "GB":
        return "gigabytes";
      case "TB":
        return "terabytes";
        // SI
      case "m":
        return "meters";
      case "V":
        return "volts";
      case "A":
        return "amperes";
      case "J":
        return "joules";
      case "W":
        return "watts";
      case "g":
        return "grams";
        // Misc
      case "Cel":
        return "celsius";
      case "Hz":
        return "hertz";
      case "1":
        return "";
      case "%":
        return "percent";
      case "$":
        return "dollars";
      default:
        return unitAbbreviation;
    }
  }

  /**
   * This method retrieves the expanded Prometheus unit name to be used with "per" units for known
   * units. For example: s => per second (singular)
   *
   * @param perUnitAbbreviation The unit abbreviation used in a 'per' unit.
   * @return The expanded unit equivalent to be used in 'per' unit if the input is a known unit,
   *     otherwise returns the input as-is.
   */
  private static String getPrometheusPerUnit(String perUnitAbbreviation) {
    switch (perUnitAbbreviation) {
      case "s":
        return "second";
      case "m":
        return "minute";
      case "h":
        return "hour";
      case "d":
        return "day";
      case "w":
        return "week";
      case "mo":
        return "month";
      case "y":
        return "year";
      default:
        return perUnitAbbreviation;
    }
  }
}
