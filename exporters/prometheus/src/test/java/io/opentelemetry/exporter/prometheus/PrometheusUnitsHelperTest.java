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
  @MethodSource("providePrometheusOTLPUnitEquivalentPairs")
  public void testPrometheusUnitEquivalency(
      String otlpUnit, String prometheusUnit, PrometheusType metricType) {
    assertEquals(
        prometheusUnit, PrometheusUnitsHelper.getEquivalentPrometheusUnit(otlpUnit, metricType));
  }

  private static Stream<Arguments> providePrometheusOTLPUnitEquivalentPairs() {
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
        // Simple expansion - Time unit
        Arguments.of("s", "seconds", PrometheusType.GAUGE),
        Arguments.of("s", "seconds", PrometheusType.COUNTER),
        Arguments.of("s", "seconds", PrometheusType.SUMMARY),
        Arguments.of("s", "seconds", PrometheusType.HISTOGRAM),
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
        Arguments.of("{packets}m", "meters", PrometheusType.GAUGE),
        Arguments.of("{packets}m", "meters", PrometheusType.COUNTER),
        Arguments.of("{packets}m", "meters", PrometheusType.SUMMARY),
        Arguments.of("{packets}m", "meters", PrometheusType.HISTOGRAM),
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
        // Units expressing rate - 'per' units
        Arguments.of("km/h", "km_per_hour", PrometheusType.GAUGE),
        Arguments.of("km/h", "km_per_hour", PrometheusType.COUNTER),
        Arguments.of("km/h", "km_per_hour", PrometheusType.SUMMARY),
        Arguments.of("km/h", "km_per_hour", PrometheusType.HISTOGRAM),
        // Units expressing rate - 'per' units, both units expanded
        Arguments.of("m/s", "meters_per_second", PrometheusType.GAUGE),
        Arguments.of("m/s", "meters_per_second", PrometheusType.COUNTER),
        Arguments.of("m/s", "meters_per_second", PrometheusType.SUMMARY),
        Arguments.of("m/s", "meters_per_second", PrometheusType.HISTOGRAM),
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
        // Misc - Special supported symbols
        Arguments.of("$", "dollars", PrometheusType.GAUGE),
        Arguments.of("$", "dollars", PrometheusType.COUNTER),
        Arguments.of("$", "dollars", PrometheusType.SUMMARY),
        Arguments.of("$", "dollars", PrometheusType.HISTOGRAM),
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
