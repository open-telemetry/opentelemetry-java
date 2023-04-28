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
  public void testPrometheusUnitEquivalency(String otlpUnit, String prometheusUnit) {
    assertEquals(prometheusUnit, PrometheusUnitsHelper.getEquivalentPrometheusUnit(otlpUnit));
  }

  private static Stream<Arguments> providePrometheusOTelUnitEquivalentPairs() {
    return Stream.of(
        // Simple expansion - storage Bytes
        Arguments.of("By", "bytes"),
        Arguments.of("B", "bytes"),
        // Simple expansion - storage KB
        Arguments.of("KB", "kilobytes"),
        Arguments.of("KBy", "kilobytes"),
        // Simple expansion - storage MB
        Arguments.of("MB", "megabytes"),
        Arguments.of("MBy", "megabytes"),
        // Simple expansion - storage GB
        Arguments.of("GB", "gigabytes"),
        Arguments.of("GBy", "gigabytes"),
        // Simple expansion - storage TB
        Arguments.of("TB", "terabytes"),
        Arguments.of("TBy", "terabytes"),
        // Simple expansion - storage KiBy
        Arguments.of("KiBy", "kibibytes"),
        // Simple expansion - storage MiBy
        Arguments.of("MiBy", "mebibytes"),
        // Simple expansion - storage GiBy
        Arguments.of("GiBy", "gibibytes"),
        // Simple expansion - storage TiBy
        Arguments.of("TiBy", "tibibytes"),
        // Simple expansion - Time unit d
        Arguments.of("d", "days"),
        // Simple expansion - Time unit h
        Arguments.of("h", "hours"),
        // Simple expansion - Time unit s
        Arguments.of("s", "seconds"),
        // Simple expansion - Time unit ms
        Arguments.of("ms", "milliseconds"),
        // Simple expansion - Time unit us
        Arguments.of("us", "microseconds"),
        // Simple expansion - Time unit ns
        Arguments.of("ns", "nanoseconds"),
        // Simple expansion - Time unit min
        Arguments.of("min", "minutes"),
        // Simple expansion - special symbol - %
        Arguments.of("%", "percent"),
        // Simple expansion - special symbols - $
        Arguments.of("$", "dollars"),
        // Simple expansion - frequency
        Arguments.of("Hz", "hertz"),
        // Simple expansion - temperature
        Arguments.of("Cel", "celsius"),
        // Unit not found - Case sensitive
        Arguments.of("S", "S"),
        // Special case - 1
        Arguments.of("1", ""),
        // Special Case - Drop metric units in {}
        Arguments.of("{packets}", ""),
        // Special Case - Dropped metric units only in {}
        Arguments.of("{packets}V", "volts"),
        // Special Case - Dropped metric units with 'per' unit handling applicable
        Arguments.of("{scanned}/{returned}", "/"),
        // Special Case - Dropped metric units with 'per' unit handling applicable
        Arguments.of("{objects}/s", "_per_second"),
        // Units expressing rate - 'per' units, both units expanded
        Arguments.of("m/s", "meters_per_second"),
        // Units expressing rate - per minute
        Arguments.of("m/m", "meters_per_minute"),
        // Units expressing rate - per day
        Arguments.of("A/d", "amperes_per_day"),
        // Units expressing rate - per week
        Arguments.of("W/w", "watts_per_week"),
        // Units expressing rate - per month
        Arguments.of("J/mo", "joules_per_month"),
        // Units expressing rate - per year
        Arguments.of("TB/y", "terabytes_per_year"),
        // Units expressing rate - 'per' units, both units unknown
        Arguments.of("v/v", "v_per_v"),
        // Units expressing rate - 'per' units, first unit unknown
        Arguments.of("km/h", "km_per_hour"),
        // Units expressing rate - 'per' units, 'per' unit unknown
        Arguments.of("g/g", "grams_per_g"),
        // Misc - unit containing known abbreviations improperly formatted
        Arguments.of("watts_W", "watts_W"),
        // Unsupported symbols
        Arguments.of("°F", "°F"),
        // Unsupported symbols - multiple
        Arguments.of("unit+=.:,!* & #unused", "unit+=.:,!* & #unused"),
        // Unsupported symbols - 'per' units
        Arguments.of("__test $/°C", "__test $_per_°C"),
        // Unsupported symbols - whitespace
        Arguments.of("\t", "\t"),
        // Null unit
        Arguments.of(null, null),
        // Misc - unit cleanup - no case match special char
        Arguments.of("$1000", "$1000"));
  }
}
