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
        Arguments.argumentSet("bytes", "By", "bytes"),
        Arguments.argumentSet("kilobytes", "KBy", "kilobytes"),
        Arguments.argumentSet("megabytes", "MBy", "megabytes"),
        Arguments.argumentSet("gigabytes", "GBy", "gigabytes"),
        Arguments.argumentSet("terabytes", "TBy", "terabytes"),
        Arguments.argumentSet("kibibytes", "KiBy", "kibibytes"),
        Arguments.argumentSet("mebibytes", "MiBy", "mebibytes"),
        Arguments.argumentSet("gibibytes", "GiBy", "gibibytes"),
        Arguments.argumentSet("tibibytes", "TiBy", "tibibytes"),
        Arguments.argumentSet("days", "d", "days"),
        Arguments.argumentSet("hours", "h", "hours"),
        Arguments.argumentSet("seconds", "s", "seconds"),
        Arguments.argumentSet("milliseconds", "ms", "milliseconds"),
        Arguments.argumentSet("microseconds", "us", "microseconds"),
        Arguments.argumentSet("nanoseconds", "ns", "nanoseconds"),
        Arguments.argumentSet("minutes", "min", "minutes"),
        Arguments.argumentSet("percent", "%", "percent"),
        Arguments.argumentSet("hertz", "Hz", "hertz"),
        Arguments.argumentSet("celsius", "Cel", "celsius"),
        Arguments.argumentSet("unknown unit S (case sensitive)", "S", "S"),
        Arguments.argumentSet("unitless 1", "1", null),
        Arguments.argumentSet("curly braces dropped", "{packets}", null),
        Arguments.argumentSet("curly braces with suffix", "{packets}V", "volts"),
        Arguments.argumentSet("both units in curly braces", "{scanned}/{returned}", null),
        Arguments.argumentSet("curly braces per second", "{objects}/s", "per_second"),
        Arguments.argumentSet("meters per second", "m/s", "meters_per_second"),
        Arguments.argumentSet("meters per minute", "m/min", "meters_per_minute"),
        Arguments.argumentSet("amperes per day", "A/d", "amperes_per_day"),
        Arguments.argumentSet("watts per week", "W/wk", "watts_per_week"),
        Arguments.argumentSet("joules per month", "J/mo", "joules_per_month"),
        Arguments.argumentSet("terabytes per year", "TBy/a", "terabytes_per_year"),
        Arguments.argumentSet("unknown per unknown", "v/v", "v_per_v"),
        Arguments.argumentSet("km per hour (first unit unknown)", "km/h", "km_per_hour"),
        Arguments.argumentSet("grams per unknown", "g/x", "grams_per_x"),
        Arguments.argumentSet("improperly formatted", "watts_W", "watts_W"));
  }
}
