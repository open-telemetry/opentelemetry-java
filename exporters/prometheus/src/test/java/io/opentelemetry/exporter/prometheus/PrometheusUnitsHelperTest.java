/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.common.base.Strings;
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

  @ParameterizedTest
  @MethodSource("reservedSuffixUnitArgs")
  void convertUnit_reservedSuffixHandling(String otlpUnit, String expectedPrometheusUnit) {
    Unit actualPrometheusUnit = PrometheusUnitsHelper.convertUnit(otlpUnit);
    if (expectedPrometheusUnit == null) {
      assertNull(actualPrometheusUnit);
    } else {
      assertEquals(expectedPrometheusUnit, actualPrometheusUnit.toString());
    }
  }

  @ParameterizedTest
  @MethodSource("annotationUnitArgs")
  void convertUnit_annotations(String otlpUnit, String expectedPrometheusUnit) {
    Unit actualPrometheusUnit = PrometheusUnitsHelper.convertUnit(otlpUnit);
    if (expectedPrometheusUnit == null) {
      assertThat(actualPrometheusUnit).isNull();
    } else {
      assertThat(actualPrometheusUnit).hasToString(expectedPrometheusUnit);
    }
  }

  private static Stream<Arguments> annotationUnitArgs() {
    String openingBraces = Strings.repeat("{", 8000);
    return Stream.of(
        Arguments.argumentSet("empty annotation", "s{}", "seconds"),
        Arguments.argumentSet("annotation only", "{objects}", null),
        Arguments.argumentSet("unitless after annotation removal", "1{objects}", null),
        Arguments.argumentSet("multiple annotations", "{first}s{second}", "seconds"),
        Arguments.argumentSet("trim after annotation removal", " \ts{objects} \n", "seconds"),
        Arguments.argumentSet("annotation containing slash", "s{objects/s}", "seconds"),
        Arguments.argumentSet("annotation before denominator", "m{objects}/s", "meters_per_second"),
        Arguments.argumentSet("nested opening braces", "s{outer{inner}", "seconds"),
        Arguments.argumentSet(
            "first closing brace ends annotation", "s{outer{inner}tail}", "stail"),
        Arguments.argumentSet("unmatched closing brace retained", "s}m{objects}", "s_m"),
        Arguments.argumentSet("unterminated annotation retained", "s{tail", "s_tail"),
        Arguments.argumentSet("valid then unterminated annotation", "s{objects}{tail", "s_tail"),
        Arguments.argumentSet(
            "repeated unterminated opening braces",
            "s" + openingBraces + "s",
            "s" + Strings.repeat("_", 8000) + "s"),
        Arguments.argumentSet(
            "repeated opening braces with closing brace", "s" + openingBraces + "}", "seconds"),
        Arguments.argumentSet(
            "repeated complete annotations", Strings.repeat("{objects}", 1000) + "s", "seconds"));
  }

  private static Stream<Arguments> reservedSuffixUnitArgs() {
    return Stream.of(
        Arguments.argumentSet("reserved suffix only", "total", null),
        Arguments.argumentSet("reserved created suffix only", "created", null),
        Arguments.argumentSet("reserved bucket suffix only", "bucket", null),
        Arguments.argumentSet("reserved info suffix only", "info", null),
        Arguments.argumentSet("reserved suffix stripped", "widgets_total", "widgets"),
        Arguments.argumentSet(
            "repeated reserved suffixes stripped", "widgets_total_info", "widgets"),
        Arguments.argumentSet(
            "trailing punctuation removed after suffix stripping", "widgets_total_", "widgets"),
        Arguments.argumentSet("leading and trailing punctuation trimmed", "._widgets_.", "widgets"),
        Arguments.argumentSet("only punctuation becomes null", "._", null));
  }

  private static Stream<Arguments> providePrometheusOTelUnitEquivalentPairs() {
    return Stream.of(
        Arguments.argumentSet("bytes", "By", "bytes"),
        Arguments.argumentSet("upper case KBy not converted", "KBy", "KBy"),
        Arguments.argumentSet("kilobytes lowercase k", "kBy", "kilobytes"),
        Arguments.argumentSet("megabytes", "MBy", "megabytes"),
        Arguments.argumentSet("gigabytes", "GBy", "gigabytes"),
        Arguments.argumentSet("terabytes", "TBy", "terabytes"),
        Arguments.argumentSet("kibibytes", "KiBy", "kibibytes"),
        Arguments.argumentSet("mebibytes", "MiBy", "mebibytes"),
        Arguments.argumentSet("gibibytes", "GiBy", "gibibytes"),
        Arguments.argumentSet("tebibytes", "TiBy", "tebibytes"),
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
