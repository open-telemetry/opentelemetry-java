/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PrometheusUnitsHelperTest {

  @ParameterizedTest
  @MethodSource("providePrometheusOTelUnitEquivalentPairs")
  public void testPrometheusUnitEquivalency(
      String otlpUnit, String prometheusUnit, PrometheusType metricType) {
    assertEquals(
        prometheusUnit, PrometheusUnitsHelper.getEquivalentPrometheusUnit(otlpUnit, metricType));
  }

  private static Stream<Arguments> providePrometheusOTelUnitEquivalentPairs() {
    return Stream.of(
        // Simple expansion - storage Bytes
        Arguments.of("By", "bytes", PrometheusType.GAUGE),
        Arguments.of("By", "bytes", PrometheusType.COUNTER),
        Arguments.of("By", "bytes", PrometheusType.SUMMARY),
        Arguments.of("By", "bytes", PrometheusType.HISTOGRAM),
        Arguments.of("B", "bytes", PrometheusType.GAUGE),
        Arguments.of("B", "bytes", PrometheusType.COUNTER),
        Arguments.of("B", "bytes", PrometheusType.SUMMARY),
        Arguments.of("B", "bytes", PrometheusType.HISTOGRAM),
        // Simple expansion - storage KB
        Arguments.of("KB", "kilobytes", PrometheusType.GAUGE),
        Arguments.of("KB", "kilobytes", PrometheusType.COUNTER),
        Arguments.of("KB", "kilobytes", PrometheusType.SUMMARY),
        Arguments.of("KB", "kilobytes", PrometheusType.HISTOGRAM),
        Arguments.of("KBy", "kilobytes", PrometheusType.GAUGE),
        Arguments.of("KBy", "kilobytes", PrometheusType.COUNTER),
        Arguments.of("KBy", "kilobytes", PrometheusType.SUMMARY),
        Arguments.of("KBy", "kilobytes", PrometheusType.HISTOGRAM),
        // Simple expansion - storage MB
        Arguments.of("MB", "megabytes", PrometheusType.GAUGE),
        Arguments.of("MB", "megabytes", PrometheusType.COUNTER),
        Arguments.of("MB", "megabytes", PrometheusType.SUMMARY),
        Arguments.of("MB", "megabytes", PrometheusType.HISTOGRAM),
        Arguments.of("MBy", "megabytes", PrometheusType.GAUGE),
        Arguments.of("MBy", "megabytes", PrometheusType.COUNTER),
        Arguments.of("MBy", "megabytes", PrometheusType.SUMMARY),
        Arguments.of("MBy", "megabytes", PrometheusType.HISTOGRAM),
        // Simple expansion - storage GB
        Arguments.of("GB", "gigabytes", PrometheusType.GAUGE),
        Arguments.of("GB", "gigabytes", PrometheusType.COUNTER),
        Arguments.of("GB", "gigabytes", PrometheusType.SUMMARY),
        Arguments.of("GB", "gigabytes", PrometheusType.HISTOGRAM),
        Arguments.of("GBy", "gigabytes", PrometheusType.GAUGE),
        Arguments.of("GBy", "gigabytes", PrometheusType.COUNTER),
        Arguments.of("GBy", "gigabytes", PrometheusType.SUMMARY),
        Arguments.of("GBy", "gigabytes", PrometheusType.HISTOGRAM),
        // Simple expansion - storage TB
        Arguments.of("TB", "terabytes", PrometheusType.GAUGE),
        Arguments.of("TB", "terabytes", PrometheusType.COUNTER),
        Arguments.of("TB", "terabytes", PrometheusType.SUMMARY),
        Arguments.of("TB", "terabytes", PrometheusType.HISTOGRAM),
        Arguments.of("TBy", "terabytes", PrometheusType.GAUGE),
        Arguments.of("TBy", "terabytes", PrometheusType.COUNTER),
        Arguments.of("TBy", "terabytes", PrometheusType.SUMMARY),
        Arguments.of("TBy", "terabytes", PrometheusType.HISTOGRAM),
        // Simple expansion - storage KiBy
        Arguments.of("KiBy", "kibibytes", PrometheusType.GAUGE),
        Arguments.of("KiBy", "kibibytes", PrometheusType.COUNTER),
        Arguments.of("KiBy", "kibibytes", PrometheusType.SUMMARY),
        Arguments.of("KiBy", "kibibytes", PrometheusType.HISTOGRAM),
        // Simple expansion - storage MiBy
        Arguments.of("MiBy", "mebibytes", PrometheusType.GAUGE),
        Arguments.of("MiBy", "mebibytes", PrometheusType.COUNTER),
        Arguments.of("MiBy", "mebibytes", PrometheusType.SUMMARY),
        Arguments.of("MiBy", "mebibytes", PrometheusType.HISTOGRAM),
        // Simple expansion - storage GiBy
        Arguments.of("GiBy", "gibibytes", PrometheusType.GAUGE),
        Arguments.of("GiBy", "gibibytes", PrometheusType.COUNTER),
        Arguments.of("GiBy", "gibibytes", PrometheusType.SUMMARY),
        Arguments.of("GiBy", "gibibytes", PrometheusType.HISTOGRAM),
        // Simple expansion - storage TiBy
        Arguments.of("TiBy", "tibibytes", PrometheusType.GAUGE),
        Arguments.of("TiBy", "tibibytes", PrometheusType.COUNTER),
        Arguments.of("TiBy", "tibibytes", PrometheusType.SUMMARY),
        Arguments.of("TiBy", "tibibytes", PrometheusType.HISTOGRAM),
        // Simple expansion - Time unit d
        Arguments.of("d", "days", PrometheusType.GAUGE),
        Arguments.of("d", "days", PrometheusType.COUNTER),
        Arguments.of("d", "days", PrometheusType.SUMMARY),
        Arguments.of("d", "days", PrometheusType.HISTOGRAM),
        // Simple expansion - Time unit h
        Arguments.of("h", "hours", PrometheusType.GAUGE),
        Arguments.of("h", "hours", PrometheusType.COUNTER),
        Arguments.of("h", "hours", PrometheusType.SUMMARY),
        Arguments.of("h", "hours", PrometheusType.HISTOGRAM),
        // Simple expansion - Time unit s
        Arguments.of("s", "seconds", PrometheusType.GAUGE),
        Arguments.of("s", "seconds", PrometheusType.COUNTER),
        Arguments.of("s", "seconds", PrometheusType.SUMMARY),
        Arguments.of("s", "seconds", PrometheusType.HISTOGRAM),
        // Simple expansion - Time unit ms
        Arguments.of("ms", "milliseconds", PrometheusType.GAUGE),
        Arguments.of("ms", "milliseconds", PrometheusType.COUNTER),
        Arguments.of("ms", "milliseconds", PrometheusType.SUMMARY),
        Arguments.of("ms", "milliseconds", PrometheusType.HISTOGRAM),
        // Simple expansion - Time unit us
        Arguments.of("us", "microseconds", PrometheusType.GAUGE),
        Arguments.of("us", "microseconds", PrometheusType.COUNTER),
        Arguments.of("us", "microseconds", PrometheusType.SUMMARY),
        Arguments.of("us", "microseconds", PrometheusType.HISTOGRAM),
        // Simple expansion - Time unit ns
        Arguments.of("ns", "nanoseconds", PrometheusType.GAUGE),
        Arguments.of("ns", "nanoseconds", PrometheusType.COUNTER),
        Arguments.of("ns", "nanoseconds", PrometheusType.SUMMARY),
        Arguments.of("ns", "nanoseconds", PrometheusType.HISTOGRAM),
        // Simple expansion - Time unit min
        Arguments.of("min", "minutes", PrometheusType.GAUGE),
        Arguments.of("min", "minutes", PrometheusType.COUNTER),
        Arguments.of("min", "minutes", PrometheusType.SUMMARY),
        Arguments.of("min", "minutes", PrometheusType.HISTOGRAM),
        // Simple expansion - special symbol - %
        Arguments.of("%", "percent", PrometheusType.GAUGE),
        Arguments.of("%", "percent", PrometheusType.COUNTER),
        Arguments.of("%", "percent", PrometheusType.SUMMARY),
        Arguments.of("%", "percent", PrometheusType.HISTOGRAM),
        // Simple expansion - special symbols - $
        Arguments.of("$", "dollars", PrometheusType.GAUGE),
        Arguments.of("$", "dollars", PrometheusType.COUNTER),
        Arguments.of("$", "dollars", PrometheusType.SUMMARY),
        Arguments.of("$", "dollars", PrometheusType.HISTOGRAM),
        // Simple expansion - frequency
        Arguments.of("Hz", "hertz", PrometheusType.GAUGE),
        Arguments.of("Hz", "hertz", PrometheusType.COUNTER),
        Arguments.of("Hz", "hertz", PrometheusType.SUMMARY),
        Arguments.of("Hz", "hertz", PrometheusType.HISTOGRAM),
        // Simple expansion - temperature
        Arguments.of("Cel", "celsius", PrometheusType.GAUGE),
        Arguments.of("Cel", "celsius", PrometheusType.COUNTER),
        Arguments.of("Cel", "celsius", PrometheusType.SUMMARY),
        Arguments.of("Cel", "celsius", PrometheusType.HISTOGRAM),
        // Unit not found - Case sensitive
        Arguments.of("S", "S", PrometheusType.GAUGE),
        Arguments.of("S", "S", PrometheusType.COUNTER),
        Arguments.of("S", "S", PrometheusType.SUMMARY),
        Arguments.of("S", "S", PrometheusType.HISTOGRAM),
        // Special case - 1
        Arguments.of("1", "ratio", PrometheusType.GAUGE),
        Arguments.of("1", "", PrometheusType.COUNTER),
        Arguments.of("1", "", PrometheusType.SUMMARY),
        Arguments.of("1", "", PrometheusType.HISTOGRAM),
        // Special Case - Drop metric units in {}
        Arguments.of("{packets}", "", PrometheusType.GAUGE),
        Arguments.of("{packets}", "", PrometheusType.COUNTER),
        Arguments.of("{packets}", "", PrometheusType.SUMMARY),
        Arguments.of("{packets}", "", PrometheusType.HISTOGRAM),
        // Special Case - Dropped metric units only in {}
        Arguments.of("{packets}V", "volts", PrometheusType.GAUGE),
        Arguments.of("{packets}V", "volts", PrometheusType.COUNTER),
        Arguments.of("{packets}V", "volts", PrometheusType.SUMMARY),
        Arguments.of("{packets}V", "volts", PrometheusType.HISTOGRAM),
        // Special Case - Dropped metric units with 'per' unit handling applicable
        Arguments.of("{scanned}/{returned}", "", PrometheusType.GAUGE),
        Arguments.of("{scanned}/{returned}", "", PrometheusType.COUNTER),
        Arguments.of("{scanned}/{returned}", "", PrometheusType.SUMMARY),
        Arguments.of("{scanned}/{returned}", "", PrometheusType.HISTOGRAM),
        // Special Case - Dropped metric units with 'per' unit handling applicable
        Arguments.of("{objects}/s", "per_second", PrometheusType.GAUGE),
        Arguments.of("{objects}/s", "per_second", PrometheusType.COUNTER),
        Arguments.of("{objects}/s", "per_second", PrometheusType.SUMMARY),
        Arguments.of("{objects}/s", "per_second", PrometheusType.HISTOGRAM),
        // Units expressing rate - 'per' units, both units expanded
        Arguments.of("m/s", "meters_per_second", PrometheusType.GAUGE),
        Arguments.of("m/s", "meters_per_second", PrometheusType.COUNTER),
        Arguments.of("m/s", "meters_per_second", PrometheusType.SUMMARY),
        Arguments.of("m/s", "meters_per_second", PrometheusType.HISTOGRAM),
        // Units expressing rate - per minute
        Arguments.of("m/m", "meters_per_minute", PrometheusType.GAUGE),
        Arguments.of("m/m", "meters_per_minute", PrometheusType.COUNTER),
        Arguments.of("m/m", "meters_per_minute", PrometheusType.SUMMARY),
        Arguments.of("m/m", "meters_per_minute", PrometheusType.HISTOGRAM),
        // Units expressing rate - per day
        Arguments.of("A/d", "amperes_per_day", PrometheusType.GAUGE),
        Arguments.of("A/d", "amperes_per_day", PrometheusType.COUNTER),
        Arguments.of("A/d", "amperes_per_day", PrometheusType.SUMMARY),
        Arguments.of("A/d", "amperes_per_day", PrometheusType.HISTOGRAM),
        // Units expressing rate - per week
        Arguments.of("W/w", "watts_per_week", PrometheusType.GAUGE),
        Arguments.of("W/w", "watts_per_week", PrometheusType.COUNTER),
        Arguments.of("W/w", "watts_per_week", PrometheusType.SUMMARY),
        Arguments.of("W/w", "watts_per_week", PrometheusType.HISTOGRAM),
        // Units expressing rate - per month
        Arguments.of("J/mo", "joules_per_month", PrometheusType.GAUGE),
        Arguments.of("J/mo", "joules_per_month", PrometheusType.COUNTER),
        Arguments.of("J/mo", "joules_per_month", PrometheusType.SUMMARY),
        Arguments.of("J/mo", "joules_per_month", PrometheusType.HISTOGRAM),
        // Units expressing rate - per year
        Arguments.of("TB/y", "terabytes_per_year", PrometheusType.GAUGE),
        Arguments.of("TB/y", "terabytes_per_year", PrometheusType.COUNTER),
        Arguments.of("TB/y", "terabytes_per_year", PrometheusType.SUMMARY),
        Arguments.of("TB/y", "terabytes_per_year", PrometheusType.HISTOGRAM),
        // Units expressing rate - 'per' units, both units unknown
        Arguments.of("v/v", "v_per_v", PrometheusType.GAUGE),
        Arguments.of("v/v", "v_per_v", PrometheusType.COUNTER),
        Arguments.of("v/v", "v_per_v", PrometheusType.SUMMARY),
        Arguments.of("v/v", "v_per_v", PrometheusType.HISTOGRAM),
        // Units expressing rate - 'per' units, first unit unknown
        Arguments.of("km/h", "km_per_hour", PrometheusType.GAUGE),
        Arguments.of("km/h", "km_per_hour", PrometheusType.COUNTER),
        Arguments.of("km/h", "km_per_hour", PrometheusType.SUMMARY),
        Arguments.of("km/h", "km_per_hour", PrometheusType.HISTOGRAM),
        // Units expressing rate - 'per' units, 'per' unit unknown
        Arguments.of("g/g", "grams_per_g", PrometheusType.GAUGE),
        Arguments.of("g/g", "grams_per_g", PrometheusType.COUNTER),
        Arguments.of("g/g", "grams_per_g", PrometheusType.SUMMARY),
        Arguments.of("g/g", "grams_per_g", PrometheusType.HISTOGRAM),
        // Misc - unit containing known abbreviations improperly formatted
        Arguments.of("watts_W", "watts_W", PrometheusType.GAUGE),
        Arguments.of("watts_W", "watts_W", PrometheusType.COUNTER),
        Arguments.of("watts_W", "watts_W", PrometheusType.SUMMARY),
        Arguments.of("watts_W", "watts_W", PrometheusType.HISTOGRAM),
        // Misc - unsupported symbols are replaced with _
        Arguments.of("°F", "F", PrometheusType.GAUGE),
        Arguments.of("°F", "F", PrometheusType.COUNTER),
        Arguments.of("°F", "F", PrometheusType.SUMMARY),
        Arguments.of("°F", "F", PrometheusType.HISTOGRAM),
        // Misc - multiple unsupported symbols are replaced with single _
        Arguments.of("unit+=.:,!* & #unused", "unit_unused", PrometheusType.GAUGE),
        Arguments.of("unit+=.:,!* & #unused", "unit_unused", PrometheusType.COUNTER),
        Arguments.of("unit+=.:,!* & #unused", "unit_unused", PrometheusType.SUMMARY),
        Arguments.of("unit+=.:,!* & #unused", "unit_unused", PrometheusType.HISTOGRAM),
        // Misc - unsupported runes in 'per' units
        Arguments.of("__test $/°C", "test_per_C", PrometheusType.GAUGE),
        Arguments.of("__test $/°C", "test_per_C", PrometheusType.COUNTER),
        Arguments.of("__test $/°C", "test_per_C", PrometheusType.SUMMARY),
        Arguments.of("__test $/°C", "test_per_C", PrometheusType.HISTOGRAM),
        // Empty Units - whitespace
        Arguments.of("\t", "", PrometheusType.GAUGE),
        Arguments.of("\t", "", PrometheusType.COUNTER),
        Arguments.of("\t", "", PrometheusType.SUMMARY),
        Arguments.of("\t", "", PrometheusType.HISTOGRAM),
        // Null unit
        Arguments.of(null, null, PrometheusType.GAUGE),
        Arguments.of(null, null, PrometheusType.COUNTER),
        Arguments.of(null, null, PrometheusType.SUMMARY),
        Arguments.of(null, null, PrometheusType.HISTOGRAM),
        // Misc - unit cleanup - no case match special char
        Arguments.of("$1000", "1000", PrometheusType.GAUGE),
        Arguments.of("$1000", "1000", PrometheusType.COUNTER),
        Arguments.of("$1000", "1000", PrometheusType.SUMMARY),
        Arguments.of("$1000", "1000", PrometheusType.HISTOGRAM),
        // Misc - unit cleanup - no case match whitespace
        Arguments.of("a b !!", "a_b", PrometheusType.GAUGE),
        Arguments.of("a b !!", "a_b", PrometheusType.COUNTER),
        Arguments.of("a b !!", "a_b", PrometheusType.SUMMARY),
        Arguments.of("a b !!", "a_b", PrometheusType.HISTOGRAM));
  }
}
