/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.prometheus.metrics.model.snapshots.Unit;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PrometheusUnitsHelperTest {

  @ParameterizedTest
  @MethodSource("providePrometheusOTelUnitEquivalentPairs")
  public void testPrometheusUnitEquivalency(String otlpUnit, String expectedPrometheusUnit) {
    Unit actualPrometheusUnit = PrometheusUnitsHelper.convertUnit(otlpUnit);
    if (expectedPrometheusUnit == null) {
      assertNull(actualPrometheusUnit);
    } else {
      assertEquals(expectedPrometheusUnit, actualPrometheusUnit.toString());
    }
  }

  private static Stream<Arguments> providePrometheusOTelUnitEquivalentPairs() {
    return Stream.of(
        // Simple expansion - storage Bytes
        Arguments.of("By", "bytes"),
        // Simple expansion - storage KBy
        Arguments.of("KBy", "kilobytes"),
        // Simple expansion - storage MBy
        Arguments.of("MBy", "megabytes"),
        // Simple expansion - storage GBy
        Arguments.of("GBy", "gigabytes"),
        // Simple expansion - storage TBy
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
        // Simple expansion - frequency
        Arguments.of("Hz", "hertz"),
        // Simple expansion - temperature
        Arguments.of("Cel", "celsius"),
        // Unit not found - Case sensitive
        Arguments.of("S", "S"),
        // Special case - 1
        Arguments.of("1", "ratio"),
        // Special Case - Drop metric units in {}
        Arguments.of("{packets}", null),
        // Special Case - Dropped metric units only in {}
        Arguments.of("{packets}V", "volts"),
        // Special Case - Dropped metric units with 'per' unit handling applicable
        Arguments.of("{scanned}/{returned}", null),
        // Special Case - Dropped metric units with 'per' unit handling applicable
        Arguments.of("{objects}/s", "per_second"),
        // Units expressing rate - 'per' units, both units expanded
        Arguments.of("m/s", "meters_per_second"),
        // Units expressing rate - per minute
        Arguments.of("m/min", "meters_per_minute"),
        // Units expressing rate - per day
        Arguments.of("A/d", "amperes_per_day"),
        // Units expressing rate - per week
        Arguments.of("W/wk", "watts_per_week"),
        // Units expressing rate - per month
        Arguments.of("J/mo", "joules_per_month"),
        // Units expressing rate - per year
        Arguments.of("TBy/a", "terabytes_per_year"),
        // Units expressing rate - 'per' units, both units unknown
        Arguments.of("v/v", "v_per_v"),
        // Units expressing rate - 'per' units, first unit unknown
        Arguments.of("km/h", "km_per_hour"),
        // Units expressing rate - 'per' units, 'per' unit unknown
        Arguments.of("g/x", "grams_per_x"),
        // Misc - unit containing known abbreviations improperly formatted
        Arguments.of("watts_W", "watts_W"));
  }
}
